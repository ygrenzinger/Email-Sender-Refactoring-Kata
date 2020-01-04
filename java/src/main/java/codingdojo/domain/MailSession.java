package codingdojo.domain;

import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;

public interface MailSession {
    Session getReadingSession();

    Session getSendingSession();

    Transport getSmtpTransport() throws MessagingException;
}
