package codingdojo;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;
import java.util.Vector;

import javax.mail.*;
import javax.mail.internet.*;


public class Server {
    private final ServerConfig serverConfig;

    public Server(ServerConfig serverConfig) {
        this.serverConfig = serverConfig;
    }

    /**
     * main() is used to start an instance of the Server
     */
    public static void main(String args[]) throws Exception {
        ServerConfig serverConfig = ServerConfig.parseArgs(args);
        Server server = new Server(serverConfig);


        // Process every "checkPeriod" minutes
        //

        while (true) {
            // Read in email list file into java.util.Vector
            //
            server.processEmails();
            Thread.sleep(server.serverConfig.checkPeriod * 1000 * 60);
        }
    }

    private void processEmails() throws IOException, MessagingException {
        Vector vList = new Vector(10);
        BufferedReader listFile = new BufferedReader(new FileReader(serverConfig.emailListFile));
        String line;
        while ((line = listFile.readLine()) != null) {
            vList.addElement(new InternetAddress(line));
        }
        listFile.close();

        InternetAddress[] toList = new InternetAddress[vList.size()];
        vList.copyInto(toList);

        //
        // Get individual emails and broadcast them to all email ids
        //
        Session session = getSession();

        // Connect to host
        //
        Store store = session.getStore("pop3");
        store.connect(serverConfig.pop3Host, -1, serverConfig.user, serverConfig.password);

        // Open the default folder
        //
        Folder folder = store.getDefaultFolder();
        if (folder == null)
            throw new NullPointerException("No default mail folder");

        folder = folder.getFolder("INBOX");
        if (folder == null)
            throw new NullPointerException("Unable to get folder: " + folder);

        boolean done = false;
        // Get message count
        //
        folder.open(Folder.READ_WRITE);
        int totalMessages = folder.getMessageCount();
        if (totalMessages == 0) {
            folder.close(false);
            store.close();
            done = true;
        }

        if (!done) {
            // Get attributes & flags for all messages
            //
            Message[] messages = folder.getMessages();
            FetchProfile fp = new FetchProfile();
            fp.add(FetchProfile.Item.ENVELOPE);
            fp.add(FetchProfile.Item.FLAGS);
            fp.add("X-Mailer");
            folder.fetch(messages, fp);

            // Process each message
            //
            for (Message message : messages) {
                sendMessage(message, toList);
            }

            folder.close(true);
            store.close();
        }
    }

    protected Session getSession() {
        Properties sysProperties = System.getProperties();
        Session session = Session.getDefaultInstance(sysProperties, null);
        session.setDebug(false);
        return session;
    }

    private void sendMessage(Message message, InternetAddress[] toList) throws MessagingException, IOException {
        if (!message.isSet(Flags.Flag.SEEN)) {

            // Get Headers (from, to, subject, date, etc.)
            //
            String replyTo = serverConfig.user;
            Address[] a = message.getFrom();
            if (a != null) {
                replyTo = a[0].toString();
            }

            String from = serverConfig.user;

            // Send message
            //
            // create some properties and get the default Session
            //
            Properties props = new Properties();
            props.put("mail.smtp.host", serverConfig.smtpHost);
            Session session1 = Session.getDefaultInstance(props, null);

            // create a message
            //
            Address[] replyToList = {new InternetAddress(replyTo)};
            Message newMessage = new MimeMessage(session1);
            if (serverConfig.fromName != null)
                newMessage.setFrom(new InternetAddress(from, serverConfig.fromName
                        + " on behalf of " + replyTo));
            else
                newMessage.setFrom(new InternetAddress(from));
            newMessage.setReplyTo(replyToList);
            newMessage.setRecipients(Message.RecipientType.BCC, toList);
            newMessage.setSubject(message.getSubject());
            newMessage.setSentDate(message.getSentDate());

            // Set message contents
            //
            if (message.getContent() instanceof Multipart) {
                newMessage.setContent((Multipart) message.getContent());
            } else {
                newMessage.setText((String) message.getContent());
            }


            // Send newMessage
            //
            Transport transport = session1.getTransport("smtp");
            transport.connect(serverConfig.smtpHost, serverConfig.user, serverConfig.password);
            transport.sendMessage(newMessage, toList);
        }
        message.setFlag(Flags.Flag.DELETED, true);
    }


    public static class ServerConfig {
        public final String smtpHost;
        public final String pop3Host;
        public final String user;
        public final String password;
        public final String fromName;
        public final int checkPeriod;
        public final String emailListFile;

        public ServerConfig(String smtpHost, String pop3Host, String user, String password, String fromName, int checkPeriod, String emailListFile) {
            this.smtpHost = smtpHost;
            this.pop3Host = pop3Host;
            this.user = user;
            this.password = password;
            this.fromName = fromName;
            this.checkPeriod = checkPeriod;
            this.emailListFile = emailListFile;
        }

        public static ServerConfig parseArgs(String[] args) {
            // check usage
            //
            if (args.length < 6) {
                System.err.println("Usage: java Server SMTPHost POP3Host user password EmailListFile CheckPeriodFromName");
                System.exit(1);
            }

            // Assign command line arguments to meaningful variable names
            //
            String smtpHost = args[0], pop3Host = args[1], user = args[2], password = args[3], emailListFile = args[4], fromName = null;

            int checkPeriod = Integer.parseInt(args[5]);

            if (args.length > 6)
                fromName = args[6];
            return new ServerConfig(smtpHost, pop3Host, user, password, fromName, checkPeriod, emailListFile);
        }


    }
}
