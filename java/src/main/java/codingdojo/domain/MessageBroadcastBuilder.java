package codingdojo.domain;

import codingdojo.infrastructure.ServerConfig;
import io.vavr.control.Try;

import javax.mail.Address;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.UnsupportedEncodingException;
import java.util.Optional;

public class MessageBroadcastBuilder {

    private final ServerConfig serverConfig;
    private final MailSession mailSession;

    public MessageBroadcastBuilder(ServerConfig serverConfig, MailSession mailSession) {
        this.serverConfig = serverConfig;
        this.mailSession = mailSession;
    }

    public Try<Message> build(Message currentMessage) {
        return Try.of(() -> {
            Message newMessage = new MimeMessage(this.mailSession.getSendingSession());
            String replyTo = buildReplyToField(currentMessage);
            newMessage.setFrom(buildFromField(replyTo));
            newMessage.setReplyTo(new Address[]{new InternetAddress(replyTo)});
            newMessage.setRecipients(Message.RecipientType.BCC, this.serverConfig.emailAddresses);
            newMessage.setSubject(currentMessage.getSubject());
            newMessage.setSentDate(currentMessage.getSentDate());
            if (currentMessage.getContent() instanceof Multipart) {
                newMessage.setContent((Multipart) currentMessage.getContent());
            } else {
                newMessage.setText((String) currentMessage.getContent());
            }
            return newMessage;

        });
    }

    private InternetAddress buildFromField(String replyTo) throws UnsupportedEncodingException, AddressException {
        InternetAddress address;
        if (this.serverConfig.fromName != null) {
            address = new InternetAddress(this.serverConfig.user, this.serverConfig.fromName
                    + " on behalf of " + replyTo);
        } else
            address = new InternetAddress(this.serverConfig.user);
        return address;
    }

    private String buildReplyToField(Message currentMessage) throws MessagingException {
        return Optional.ofNullable(currentMessage.getFrom())
                .map(addresses -> addresses[0].toString())
                .orElse(this.serverConfig.user);
    }
}