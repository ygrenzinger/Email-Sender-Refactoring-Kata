package codingdojo;

import codingdojo.domain.MessageBroadcaster;
import codingdojo.domain.MailSession;
import codingdojo.infrastructure.DefaultMailSession;
import codingdojo.infrastructure.ServerConfig;

public class BroadcastServer {

    private final ServerConfig serverConfig;
    private final MessageBroadcaster messageBroadcaster;
    private boolean running = true;

    public BroadcastServer(String[] args) {
        serverConfig = ServerConfig.parseArgs(args);
        MailSession mailSession = new DefaultMailSession(serverConfig);
        messageBroadcaster = new MessageBroadcaster(serverConfig, mailSession);
    }

    public void run() {
        try {
            while (running) {
                messageBroadcaster.processEmails();
                // Process every "checkPeriod" minutes
                Thread.sleep(serverConfig.checkPeriod * 1000 * 60);
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public void stop() {
        this.running = false;
    }

    public static void main(String[] args) {
        new BroadcastServer(args).run();
    }
}
