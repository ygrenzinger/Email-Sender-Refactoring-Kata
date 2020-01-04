package codingdojo;

import codingdojo.domain.MessageBroadcaster;
import codingdojo.domain.MailSession;
import codingdojo.infrastructure.DefaultMailSession;
import codingdojo.infrastructure.ServerConfig;
import com.icegreen.greenmail.store.FolderException;
import com.icegreen.greenmail.store.MailFolder;
import com.icegreen.greenmail.store.StoredMessage;
import com.icegreen.greenmail.user.GreenMailUser;
import com.icegreen.greenmail.util.GreenMail;
import com.icegreen.greenmail.util.ServerSetupTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.IOException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

class MessageBroadcasterIntegrationTest {

    MailSession mailSession;
    ServerConfig serverConfig;

    GreenMail pop3mail = new GreenMail(ServerSetupTest.POP3);
    GreenMail smtpMail = new GreenMail(ServerSetupTest.SMTP);


    @BeforeEach
    public void beforeEach() {
        serverConfig = ServerConfigFake.getDefaultServerConfigForTest();
        mailSession = new DefaultMailSession(serverConfig);
        pop3mail.start();
        smtpMail.start();

    }

    @AfterEach
    public void afterEach() {
        pop3mail.stop();
        smtpMail.stop();
    }


    @Test
    public void should_do_nothing_if_no_message() throws Exception {
        pop3mail.setUser(serverConfig.fromName, serverConfig.user, serverConfig.password);
        smtpMail.setUser(serverConfig.fromName, serverConfig.user, serverConfig.password);

        MessageBroadcaster broadcaster = new MessageBroadcaster(serverConfig, mailSession);
        broadcaster.processEmails();

        GreenMailUser n = smtpMail.setUser("roger@localhost.com", null);
        MailFolder inbox = smtpMail.getManagers().getImapHostManager().getInbox(n);
        List<StoredMessage> messages = inbox.getMessages();
        assertThat(messages.isEmpty()).isTrue();
    }

    @Test
    public void should_broadcast_message_in_INBOX_folder() throws Exception {
        MimeMessage messageToSend = new MimeMessage((Session) null);
        messageToSend.setFrom(new InternetAddress("mike@mail.com"));
        messageToSend.addRecipient(Message.RecipientType.TO, new InternetAddress(
                "john@localhost.com"));
        messageToSend.setSubject("test");
        messageToSend.setText("long description");

        GreenMailUser pop3user = pop3mail.setUser(serverConfig.fromName, serverConfig.user, serverConfig.password);
        pop3user.deliver(messageToSend);

        smtpMail.setUser(serverConfig.fromName, serverConfig.user, serverConfig.password);

        MessageBroadcaster broadcaster = new MessageBroadcaster(serverConfig, mailSession);
        broadcaster.processEmails();

        assertTrue(smtpMail.waitForIncomingEmail(
                5000, // timeout in ms
                2     // expected emails = TO + CC + BCC
        ));

        assertThatUserReceivesEmail("roger@localhost.com");
        assertThatUserReceivesEmail("arnold@localhost.com");
    }

    private void assertThatUserReceivesEmail(String user) throws FolderException, MessagingException, IOException {
        GreenMailUser n = smtpMail.setUser(user, null);
        MailFolder inbox = smtpMail.getManagers().getImapHostManager().getInbox(n);
        List<StoredMessage> messages = inbox.getMessages();
        if (!messages.isEmpty()) {
            assertThat(messages).hasSize(1);
            MimeMessage expectedMessage = messages.get(0).getMimeMessage();
            assertThat(expectedMessage.getSubject()).isEqualTo("test");
            assertThat(expectedMessage.getFrom()[0].toString()).isEqualTo("\"john@localhost.com on behalf of mike@mail.com\" <johndoe@localhost.com>");
            assertThat(expectedMessage.getContent().toString()).contains("long description");
        } else {
            fail("No email for user " + user + " arrived");
        }
    }
}