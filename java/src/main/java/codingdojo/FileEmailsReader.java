package codingdojo;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class FileEmailsReader {

    public InternetAddress[] retrieveInternetAddresses(String emailListFile) {
        try {
            return Files.lines(Path.of(emailListFile)).map(s -> {
                try {
                    return new InternetAddress(s);
                } catch (AddressException e) {
                    throw new RuntimeException(e);
                }
            }).toArray(InternetAddress[]::new);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}