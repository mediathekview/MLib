package de.mediathekview.mlib.filmlisten.reader;

import de.mediathekview.mlib.daten.Filmlist;
import de.mediathekview.mlib.messages.MessageCreator;
import de.mediathekview.mlib.messages.listener.MessageListener;

import java.io.InputStream;
import java.nio.file.Path;

public abstract class AbstractFilmlistReader extends MessageCreator
{
    public AbstractFilmlistReader()
    {
        super();
    }

    public AbstractFilmlistReader(final MessageListener... aListeners)
    {
        super(aListeners);
    }

    public abstract Filmlist read(InputStream aInputStream);
}