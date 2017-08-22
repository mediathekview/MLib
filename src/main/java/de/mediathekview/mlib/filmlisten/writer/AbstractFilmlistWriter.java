package de.mediathekview.mlib.filmlisten.writer;

import de.mediathekview.mlib.daten.Filmlist;
import de.mediathekview.mlib.messages.MessageCreator;
import de.mediathekview.mlib.messages.listener.MessageListener;

import java.nio.file.Path;

public abstract class AbstractFilmlistWriter extends MessageCreator
{
    public AbstractFilmlistWriter()
    {
        super();
    }

    public AbstractFilmlistWriter(final MessageListener... aListeners)
    {
        super(aListeners);
    }

    public abstract boolean write(Filmlist aFilmlist, Path aSavePath);
}