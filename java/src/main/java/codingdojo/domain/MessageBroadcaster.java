package codingdojo.domain;

import codingdojo.infrastructure.ServerConfig;
import io.vavr.control.Try;

import javax.mail.Flags;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;


public class MessageBroadcaster {
    private final ServerConfig serverConfig;
    private final MailSession mailSession;
    private final MessageBroadcastBuilder messageBroadcastBuilder;

    public MessageBroadcaster(ServerConfig serverConfig, MailSession mailSession) {
        this.serverConfig = serverConfig;
        this.mailSession = mailSession;
        this.messageBroadcastBuilder = new MessageBroadcastBuilder(serverConfig, mailSession);
    }

    public void processEmails() {
        Session session = mailSession.getReadingSession();
        try (InboxFolder inboxFolder = InboxFolder.open(session, serverConfig)) {
            if (inboxFolder.isEmpty()) return;
            inboxFolder.fetchMessages()
                    .stream()
                    .map(this::broadcastMessage)
                    .forEach(result -> result.onFailure(failure -> {
                        System.out.println("Impossible to broadcast message due to " + failure);
                    }));
        } catch (MessagingException e) {
            throw new RuntimeException("Impossible to fetch message", e);
        }
    }

    private Try<Message> broadcastMessage(Message message) {
        return Try.of(() -> {
            if (!message.isSet(Flags.Flag.SEEN)) {
                Try<Message> messageToSend = this.messageBroadcastBuilder.build(message);
                mailSession.getSmtpTransport().sendMessage(messageToSend.get(), serverConfig.emailAddresses);
            }
            message.setFlag(Flags.Flag.DELETED, true);
            return message;
        });
    }

}
