package de.mediathekview.mlib.messages;

import de.mediathekview.mlib.messages.listener.MessageListener;

import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.ConcurrentSkipListSet;

public abstract class MessageCreator
{
    private Collection<MessageListener> messageListeners;

    public MessageCreator()
    {
        super();
        messageListeners = new ConcurrentSkipListSet<>();
    }

    public MessageCreator(MessageListener... aListeners)
    {
        this();
        messageListeners.addAll(Arrays.asList(aListeners));
    }

    public boolean addMessageListener(MessageListener aMessageListener)
    {
        return messageListeners.add(aMessageListener);
    }

    public boolean removeMessageListener(MessageListener aMessageListener)
    {
        return messageListeners.remove(aMessageListener);
    }

    public boolean addAllMessageListener(Collection<MessageListener> aMessageListeners)
    {
        return messageListeners.addAll(aMessageListeners);
    }

    public boolean removeAllMessageListener(Collection<MessageListener> aMessageListeners)
    {
        return messageListeners.removeAll(aMessageListeners);
    }

    protected void publishMessage(Message aMessage, Object... aParams)
    {
        messageListeners.parallelStream().forEach(l -> l.consumeMessage(aMessage,aParams));
    }

}
