package codingdojo;

import org.junit.jupiter.api.Test;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import java.io.IOException;
import java.net.URISyntaxException;

import static org.assertj.core.api.Assertions.assertThat;

class FileEmailsReaderTest {

    @Test
    void should_retrieve_internet_address_from_file() throws AddressException, URISyntaxException {
        String filePath = FileEmailsReader.class.getResource("emails.txt").toURI().getPath();
        assertThat(new FileEmailsReader().retrieveInternetAddresses(filePath)).containsExactly(
                new InternetAddress("test@gmail.com"),
                new InternetAddress("old@yahoo.com")
        );
    }
}