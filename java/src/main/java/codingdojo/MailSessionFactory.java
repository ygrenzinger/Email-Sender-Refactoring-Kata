package codingdojo;

import javax.mail.Session;
import java.util.Properties;

public class MailSessionFactory {
    public Session getReadingSession() {
        return Session.getDefaultInstance(new Properties(), null);
    }
    public Session getSendingSession(String smtpHost) {
        Properties props = new Properties();
        props.put("mail.smtp.host", smtpHost);
        return Session.getDefaultInstance(props, null);
    }
}