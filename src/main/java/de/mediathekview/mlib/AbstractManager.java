package de.mediathekview.mlib;

import de.mediathekview.mlib.messages.listener.MessageListener;
import de.mediathekview.mlib.progress.ProgressListener;

import java.util.ArrayList;
import java.util.Collection;

public abstract class AbstractManager
{
    protected final Collection<ProgressListener> progressListeners;
    protected final Collection<MessageListener> messageListeners;

    public AbstractManager()
    {
        progressListeners = new ArrayList<>();
        messageListeners = new ArrayList<>();
    }

    public boolean addProgressListener(final ProgressListener aCrawlerProgressListener)
    {
        return progressListeners.add(aCrawlerProgressListener);
    }

    public boolean addMessageListener(final MessageListener aMessageListener)
    {
        return messageListeners.add(aMessageListener);
    }

    public boolean addAllProgressListener(final Collection<? extends ProgressListener> c)
    {
        return progressListeners.addAll(c);
    }

    public boolean addAllMessageListener(final Collection<? extends MessageListener> c)
    {
        return messageListeners.addAll(c);
    }
}
