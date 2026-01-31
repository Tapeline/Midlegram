package midp.tapeline.midlegram;

import midp.tapeline.midlegram.client.AsyncClient;
import midp.tapeline.midlegram.client.TelegramClient;
import midp.tapeline.midlegram.events.BackgroundMessageDeliverer;
import midp.tapeline.midlegram.events.MessageBus;
import midp.tapeline.midlegram.logging.Logger;
import midp.tapeline.midlegram.state.TelegramStore;
import midp.tapeline.midlegram.ui.base.UI;

public class G {

    public static UI ui;
    public static Logger logger;
    public static TelegramStore store = new TelegramStore();
    public static TelegramClient client;
    public static AsyncClient asyncClient;
    public static Midlegram midlet;
    public static MessageBus messageBus = new MessageBus();
    public static BackgroundMessageDeliverer deliverer =
        new BackgroundMessageDeliverer(messageBus, 100);

}
