package midp.tapeline.midlegram.events;

public interface Subscriber {

    void onReceive(MessageBus bus, Message message);

}
