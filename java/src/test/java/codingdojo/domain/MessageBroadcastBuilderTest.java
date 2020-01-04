package codingdojo.domain;

import codingdojo.infrastructure.DefaultMailSession;
import codingdojo.infrastructure.ServerConfig;
import codingdojo.ServerConfigFake;
import io.vavr.control.Try;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.io.IOException;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;

class MessageBroadcastBuilderTest {
    ServerConfig serverConfig;
    MailSession mailSession;
    MessageBroadcastBuilder messageBroadcastBuilder;

    @BeforeEach
    public void beforeEach() {
        this.serverConfig = ServerConfigFake.getDefaultServerConfigForTest();
        this.mailSession = new DefaultMailSession(serverConfig);
        this.messageBroadcastBuilder = new MessageBroadcastBuilder(serverConfig, mailSession);
    }

    @Test
    public void should_build_typical_message_to_forward() throws MessagingException, IOException {
        Message mimeMessage = createDefaultMimeMessage(mailSession);
        mimeMessage.setFrom(new InternetAddress("donald@localhost.com"));

        Try<Message> result = messageBroadcastBuilder.build(mimeMessage);

        assertThat(result.isSuccess()).isTrue();
        Message expectedMessage = result.get();
        assertThat(expectedMessage.getSubject()).isEqualTo(mimeMessage.getSubject());
        assertThat(expectedMessage.getContent()).isEqualTo(mimeMessage.getContent());
        assertThat(expectedMessage.getSentDate()).isEqualTo(mimeMessage.getSentDate());
        Address[] expectedFrom = {new InternetAddress("\"john@localhost.com on behalf of donald@localhost.com\" <johndoe@localhost.com>")};
        assertThat(expectedMessage.getFrom()).isEqualTo(expectedFrom);
        assertThat(expectedMessage.getReplyTo()).isEqualTo(mimeMessage.getFrom());
        assertThat(expectedMessage.getRecipients(Message.RecipientType.BCC)).isEqualTo(serverConfig.emailAddresses);
    }

    @Test
    public void should_assign_user_in_reply_to_when_from_is_absent() throws MessagingException {
        Message mimeMessage = createDefaultMimeMessage(mailSession);

        Try<Message> result = messageBroadcastBuilder.build(mimeMessage);

        assertThat(result.isSuccess()).isTrue();
        Message expectedMessage = result.get();
        assertThat(expectedMessage.getReplyTo()).containsExactly(new InternetAddress(serverConfig.user));
    }

    @Test
    public void should_assign_user_config_in_from_when_from_name_is_absent_of_config() throws MessagingException {
        this.messageBroadcastBuilder = new MessageBroadcastBuilder(ServerConfigFake.getServerConfig(null), mailSession);
        Message mimeMessage = createDefaultMimeMessage(mailSession);

        Try<Message> result = messageBroadcastBuilder.build(mimeMessage);

        assertThat(result.isSuccess()).isTrue();
        Message expectedMessage = result.get();
        assertThat(expectedMessage.getFrom()).containsExactly(new InternetAddress(serverConfig.user));
    }

    @Test
    public void should_manage_multipart_content() throws MessagingException, IOException {
        Message mimeMessage = createDefaultMimeMessage(mailSession);
        Multipart multipart = new MimeMultipart();
        BodyPart messageBodyPart = new MimeBodyPart();
        messageBodyPart.setText("description");
        multipart.addBodyPart(messageBodyPart);
        mimeMessage.setContent(multipart);

        Try<Message> result = messageBroadcastBuilder.build(mimeMessage);

        assertThat(result.isSuccess()).isTrue();
        Message expectedMessage = result.get();
        assertThat(expectedMessage.getContent()).isEqualTo(multipart);
    }

    private Message createDefaultMimeMessage(MailSession mailSession) throws MessagingException {
        Message mimeMessage = new MimeMessage(mailSession.getSendingSession());
        mimeMessage.setSubject("test");
        mimeMessage.setText("description");
        mimeMessage.setSentDate(new Date());
        return mimeMessage;
    }

}