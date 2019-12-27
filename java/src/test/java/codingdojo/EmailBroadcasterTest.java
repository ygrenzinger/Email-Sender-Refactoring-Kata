package codingdojo;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

class EmailBroadcasterTest {

    MailSessionFactory mailSessionFactory;
    ServerConfig serverConfig;

    @BeforeEach
    public void beforeEach() throws AddressException {
        mailSessionFactory = Mockito.mock(MailSessionFactory.class);
        serverConfig = new ServerConfig(
                "smtpHost",
                "pop3host",
                "john",
                "azerty1234",
                "mick",
                100,
                InternetAddress.parse("new@gmail.com;old@yahoo.com"));
    }

    @Test
    public void shoudl_send_emails() throws Exception {
        EmailBroadcaster broadcaster = new EmailBroadcaster(serverConfig, mailSessionFactory, inboxFolder);
        broadcaster.processEmails();
    }
}