package codingdojo;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.IOException;


public class EmailBroadcaster {
    private final ServerConfig serverConfig;
    private final MailSessionFactory sessionFactory;

    public EmailBroadcaster(ServerConfig serverConfig, MailSessionFactory sessionFactory) {
        this.serverConfig = serverConfig;
        this.sessionFactory = sessionFactory;
    }

    public void processEmails() throws IOException, MessagingException {

        //
        // Get individual emails and broadcast them to all email ids
        //
        Session session = sessionFactory.getReadingSession();

        // Connect to host
        //
        Store store = session.getStore("pop3");
        store.connect(serverConfig.pop3Host, 3110, serverConfig.user, serverConfig.password);

        // Open the default folder
        //
        Folder folder = openInboxFolder(store);

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
            Message[] messages = fetchMessages(folder);

            // Process each message
            //
            for (Message message : messages) {
                sendMessage(message);
            }

            folder.close(true);
            store.close();
        }
    }

    private Message[] fetchMessages(Folder folder) throws MessagingException {
        Message[] messages = folder.getMessages();
        FetchProfile fp = new FetchProfile();
        fp.add(FetchProfile.Item.ENVELOPE);
        fp.add(FetchProfile.Item.FLAGS);
        fp.add("X-Mailer");
        folder.fetch(messages, fp);
        return messages;
    }

    private Folder openInboxFolder(Store store) throws MessagingException {
        Folder folder = store.getDefaultFolder();
        if (folder == null)
            throw new NullPointerException("No default mail folder");

        folder = folder.getFolder("INBOX");
        if (folder == null)
            throw new NullPointerException("Unable to get folder: " + folder);
        return folder;
    }

    private void sendMessage(Message message) throws MessagingException, IOException {
        if (!message.isSet(Flags.Flag.SEEN)) {
            Session session1 = sessionFactory.getSendingSession(serverConfig.smtpHost);

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
            newMessage.setRecipients(Message.RecipientType.BCC, serverConfig.emailAddresses);
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
            transport.connect(serverConfig.smtpHost, 3025, serverConfig.user, serverConfig.password);
            transport.sendMessage(newMessage, serverConfig.emailAddresses);
        }
        message.setFlag(Flags.Flag.DELETED, true);
    }


}
