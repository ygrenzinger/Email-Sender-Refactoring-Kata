package codingdojo;

public class Server {

    public static void main(String[] args) throws Exception {
        ServerConfig serverConfig = ServerConfig.parseArgs(args);
        EmailBroadcaster broadcaster = new EmailBroadcaster(serverConfig, new MailSessionFactory());

        while (true) {
            broadcaster.processEmails();
            // Process every "checkPeriod" minutes
            Thread.sleep(serverConfig.checkPeriod * 1000 * 60);
        }
    }
}
