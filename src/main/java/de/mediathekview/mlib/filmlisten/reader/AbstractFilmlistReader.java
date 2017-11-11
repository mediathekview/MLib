package de.mediathekview.mlib.filmlisten.reader;

import java.io.InputStream;
import java.util.Optional;

import de.mediathekview.mlib.daten.Filmlist;
import de.mediathekview.mlib.messages.MessageCreator;
import de.mediathekview.mlib.messages.listener.MessageListener;

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

    public abstract Optional<Filmlist> read(InputStream aInputStream);
}