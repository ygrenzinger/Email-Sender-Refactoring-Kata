package codingdojo;

import codingdojo.infrastructure.ServerConfig;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

public class ServerConfigFake {
    public static ServerConfig getDefaultServerConfigForTest() {
        try {
            return getServerConfig("john@localhost.com");
        } catch (AddressException e) {
            throw new RuntimeException(e);
        }
    }

    public static ServerConfig getServerConfig(String fromName) throws AddressException {
        return new ServerConfig(
                "127.0.0.1",
                "127.0.0.1",
                "johndoe@localhost.com",
                "soooosecret",
                fromName,
                100,
                InternetAddress.parse("roger@localhost.com,arnold@localhost.com"));
    }
}
