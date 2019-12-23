package codingdojo;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Date;
import java.util.Properties;
import java.util.Vector;

import javax.mail.*;
import javax.mail.internet.*;


public class Server {
    private final ServerConfig serverConfig = new ServerConfig();

    /**
     * main() is used to start an instance of the Server
     */
    public static void main(String args[]) throws Exception {
        run(args);
    }

    private static void run(String[] args) throws IOException, MessagingException, InterruptedException {
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

        // Process every "checkPeriod" minutes
        //
        Server ls = new Server();

        while (true) {
            if (false)
                System.out.println(new Date() + "> " + "SESSION START");
            ls.serverConfig._smtpHost = smtpHost;
            ls.serverConfig._user = user;
            ls.serverConfig._password = password;

            if (fromName != null)
                ls.serverConfig._fromName = fromName;

            // Read in email list file into java.util.Vector
            //
            Vector vList = new Vector(10);
            BufferedReader listFile = new BufferedReader(new FileReader(
                    emailListFile));
            String line = null;
            while ((line = listFile.readLine()) != null) {
                vList.addElement(new InternetAddress(line));
            }
            listFile.close();

            ls.serverConfig.toList = new InternetAddress[vList.size()];
            vList.copyInto(ls.serverConfig.toList);

            //
            // Get individual emails and broadcast them to all email ids
            //

            // Get a Session object
            //
            Properties sysProperties = System.getProperties();
            Session session = Session.getDefaultInstance(sysProperties, null);
            session.setDebug(false);

            // Connect to host
            //
            Store store = session.getStore("pop3");
            store.connect(pop3Host, -1, ls.serverConfig._user, ls.serverConfig._password);

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
                    sendMessage(ls, message);
                }

                folder.close(true);
                store.close();
            }
            Thread.sleep(checkPeriod * 1000 * 60);
        }
    }

    private static void sendMessage(Server ls, Message message) throws MessagingException, IOException {
            ServerConfig serverConfig = ls.serverConfig;
        if (!message.isSet(Flags.Flag.SEEN)) {

            // Get Headers (from, to, subject, date, etc.)
            //
            String replyTo = serverConfig._user;
            Address[] a = message.getFrom();
            if (a != null) {
                replyTo = a[0].toString();
            }

            String from = serverConfig._user;

            // Send message
            //
            // create some properties and get the default Session
            //
            Properties props = new Properties();
            props.put("mail.smtp.host", serverConfig._smtpHost);
            Session session1 = Session.getDefaultInstance(props, null);

            // create a message
            //
            Address[] replyToList = {new InternetAddress(replyTo)};
            Message newMessage = new MimeMessage(session1);
            if (serverConfig._fromName != null)
                newMessage.setFrom(new InternetAddress(from, serverConfig._fromName
                        + " on behalf of " + replyTo));
            else
                newMessage.setFrom(new InternetAddress(from));
            newMessage.setReplyTo(replyToList);
            newMessage.setRecipients(Message.RecipientType.BCC, serverConfig.toList);
            newMessage.setSubject(message.getSubject());
            newMessage.setSentDate(message.getSentDate());

            // Set message contents
            //
            Object content = message.getContent();
            String debugText = "Subject: " + message.getSubject() + ", Sent date: " + message.getSentDate();
            if (content instanceof Multipart) {
                newMessage.setContent((Multipart) message.getContent());
            } else {
                newMessage.setText((String) content);
            }


            // Send newMessage
            //
            Transport transport = session1.getTransport("smtp");
        transport.connect(serverConfig._smtpHost, serverConfig._user, serverConfig._password);
            sendMessage(newMessage, serverConfig, transport);
        }
        message.setFlag(Flags.Flag.DELETED, true);
    }

    private static void sendMessage(Message newMessage, ServerConfig serverConfig, Transport transport) throws MessagingException {
        transport.sendMessage(newMessage, serverConfig.toList);
    }


    public static class ServerConfig {
        private String _smtpHost = null;
        private String _user = null;
        private String _password = null;
        private String _fromName = null;
        private InternetAddress[] toList = null;

        public ServerConfig() {
        }
    }
}
