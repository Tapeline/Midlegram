package midp.tapeline.midlegram.testing;

import midp.tapeline.midlegram.ArrayUtils;
import midp.tapeline.midlegram.StringUtils;
import midp.tapeline.midlegram.client.TelegramClient;
import midp.tapeline.midlegram.state.data.Chat;
import midp.tapeline.midlegram.state.data.ChatFolder;
import midp.tapeline.midlegram.state.data.PaginatedChats;

import java.io.IOException;
import java.util.Vector;

public class FakeTelegramClient implements TelegramClient {

    private static Vector folders = ArrayUtils.vectorOf(new ChatFolder[] {
        new ChatFolder(
            0, "All",
            ArrayUtils.vectorOfLongs(new long[] {1, 2, 3, 4, 5, 6, 7, 8, 9})
        ),
        new ChatFolder(
            1, "Test 1",
            ArrayUtils.vectorOfLongs(new long[] {1, 2})
        ),
        new ChatFolder(
            2, "Test 2",
            ArrayUtils.vectorOfLongs(new long[] {3})
        ),
        new ChatFolder(
            3, "Test 3",
            ArrayUtils.vectorOfLongs(new long[] {4, 5, 6, 7, 8, 9})
        )
    });
    private static Chat[] allChats = {
        new Chat(0, "Chat 0", 123, "Last msg"),
        new Chat(1, "Chat 1", 0, "Last msg"),
        new Chat(2, "Chat 2", 34, "Last msg"),
        new Chat(3, "Chat 3", 23, "Very long last msg lorem ipsum"),
        new Chat(4, "Chat 4", 123, "Last msg"),
        new Chat(5, "Chat 5", 12223, "Last msg"),
        new Chat(6, "Chat 6", 123, "Last msg"),
        new Chat(7, "Chat 7", 123, "Last msg"),
        new Chat(8, "Chat 8", 123, "Last msg"),
        new Chat(9, "Chat 9", 123, "Last msg"),
    };

    private void delay() {
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {}
    }

    public String getSessionKey() {
        return "aabbccddeeffgg";
    }

    public void startAuth(String phone) throws IOException {
        delay();
        System.out.println("Fake auth using phone " + phone);
    }

    public void confirmCode(String code) throws IOException {
        delay();
        System.out.println("Confirming code " + code);
    }

    public Vector getFolders() throws IOException {
        delay();
        return folders;
    }

    public Vector getChatsIds(long folderId) throws IOException {
        delay();
        for (int i = 0; i < folders.size(); i++)
            if (((ChatFolder) folders.elementAt(i)).id == folderId)
                return ((ChatFolder) folders.elementAt(i)).chatIds;
        return new Vector();
    }

    public PaginatedChats getChats(long folderId, int page) throws IOException {
        delay();
        Vector chatsIds = getChatsIds(folderId);
        Vector chats = new Vector();
        for (int i = 5 * page; i < Math.min(chatsIds.size(), 5 * page + 5); i++)
            chats.addElement(allChats[(int) ((Long) chatsIds.elementAt(i)).longValue()]);
        return new PaginatedChats(
            chats, chatsIds.size() / 5 + (chatsIds.size() % 5 != 0? 1 : 0));
    }

    public Chat getChat(long chatId) throws IOException {
        delay();
        return allChats[(int) chatId];
    }

    public Vector getMessages(long chatId, long fromMsgId, int count) throws IOException {
        delay();
        return new Vector();
    }

    public void sendTextMessage(long chatId, long replyTo, String message) throws IOException {
        delay();
        System.out.println("Sent message " + message);
    }

    public Vector searchChats(String query, int limit) throws IOException {
        delay();
        Vector v = new Vector();
        for (int i = 0; i < allChats.length; i++)
            if (StringUtils.contains(query, allChats[i].title))
                v.addElement(allChats[i]);
        return v;
    }

}
