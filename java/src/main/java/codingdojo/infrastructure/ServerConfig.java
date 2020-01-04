package codingdojo.infrastructure;

import javax.mail.internet.InternetAddress;

public class ServerConfig {
    public final String smtpHost;
    public final String pop3Host;
    public final String user;
    public final String password;
    public final String fromName;
    public final int checkPeriod;
    public final InternetAddress[] emailAddresses;

    public ServerConfig(String smtpHost, String pop3Host, String user, String password, String fromName, int checkPeriod, InternetAddress[] emailAddresses) {
        this.smtpHost = smtpHost;
        this.pop3Host = pop3Host;
        this.user = user;
        this.password = password;
        this.fromName = fromName;
        this.checkPeriod = checkPeriod;
        this.emailAddresses = emailAddresses;
    }

    public static ServerConfig parseArgs(String[] args) {
        // check usage
        //
        if (args.length < 6) {
            System.err.println("Usage: java Server SMTPHost POP3Host user password EmailListFile CheckPeriodFromName");
            System.exit(1);
        }

        // Assign command line arguments to meaningful variable names
        //
        String smtpHost = args[0], pop3Host = args[1], user = args[2], password = args[3], emailListFile = args[4], fromName = null;

        int checkPeriod = Integer.parseInt(args[5]);

        if (args.length > 6)
            fromName = args[6];
        InternetAddress[] emailAddresses = new FileEmailsReader().retrieveInternetAddresses(emailListFile);

        return new ServerConfig(smtpHost, pop3Host, user, password, fromName, checkPeriod, emailAddresses);
    }


}
