package codingdojo.infrastructure;

import codingdojo.domain.MailSession;

import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import java.util.Properties;

public class DefaultMailSession implements MailSession {
    private final ServerConfig serverConfig;
    private Transport transport;

    public DefaultMailSession(ServerConfig serverConfig) {
        this.serverConfig = serverConfig;
    }

    @Override
    public Session getReadingSession() {
        return Session.getDefaultInstance(new Properties(), null);
    }
    @Override
    public Session getSendingSession() {
        Properties props = new Properties();
        props.put("mail.smtp.host", this.serverConfig.smtpHost);
        return Session.getDefaultInstance(props, null);
    }
    @Override
    public Transport getSmtpTransport() throws MessagingException {
        if (this.transport == null) {
            Transport transport = getSendingSession().getTransport("smtp");
            transport.connect(serverConfig.smtpHost, 3025, serverConfig.user, serverConfig.password);
            this.transport = transport;
        }
        return this.transport;
    }
}