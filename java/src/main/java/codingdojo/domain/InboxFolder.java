package codingdojo.domain;

import codingdojo.infrastructure.ServerConfig;

import javax.mail.*;
import java.util.Arrays;
import java.util.List;

public class InboxFolder implements AutoCloseable {

    private final boolean empty;
    private final Store store;
    private final Folder folder;

    public InboxFolder(Store store, Folder folder, boolean empty) {
        this.store = store;
        this.folder = folder;
        this.empty = empty;
    }

    public static InboxFolder open(Session session, ServerConfig serverConfig) throws MessagingException {
        Store store = session.getStore("pop3");
        store.connect(serverConfig.pop3Host, 3110, serverConfig.user, serverConfig.password);

        Folder folder = store.getDefaultFolder();
        if (folder == null)
            throw new RuntimeException("No default mail folder");

        folder = folder.getFolder("INBOX");
        if (folder == null)
            throw new RuntimeException("Unable to get folder: " + folder);
        folder.open(Folder.READ_WRITE);

        return new InboxFolder(store, folder, folder.getMessageCount() == 0);

    }

    public boolean isEmpty() {
        return empty;
    }

    @Override
    public void close() throws MessagingException {
        folder.close(!empty);
        store.close();
    }

    public List<Message> fetchMessages() throws MessagingException {
        FetchProfile fetchProfile = buildFetchProfile();
        folder.fetch(folder.getMessages(), fetchProfile);
        return Arrays.asList(folder.getMessages());
    }

    private FetchProfile buildFetchProfile() {
        FetchProfile fp = new FetchProfile();
        fp.add(FetchProfile.Item.ENVELOPE);
        fp.add(FetchProfile.Item.FLAGS);
        fp.add("X-Mailer");
        return fp;
    }
}
