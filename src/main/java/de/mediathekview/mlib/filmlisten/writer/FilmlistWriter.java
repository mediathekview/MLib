package de.mediathekview.mlib.filmlisten.writer;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonWriter;
import de.mediathekview.mlib.daten.Filmlist;
import de.mediathekview.mlib.messages.LibMessages;
import de.mediathekview.mlib.messages.MessageCreator;
import de.mediathekview.mlib.messages.listener.MessageListener;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class FilmlistWriter extends AbstractFilmlistWriter
{
    private static final Logger LOG = LogManager.getLogger(FilmlistWriter.class);

    public FilmlistWriter()
    {
        super();
    }

    public FilmlistWriter(final MessageListener... aListeners)
    {
        super(aListeners);
    }

    public boolean write(Filmlist aFilmlist, Path aSavePath)
    {
        Gson gson = new Gson();
        try(BufferedWriter fileWriter = Files.newBufferedWriter(aSavePath))
        {
            gson.toJson(aFilmlist, fileWriter);
        } catch (IOException ioException)
        {
            LOG.debug("Something went wrong on writing the film list.", ioException);
            publishMessage(LibMessages.FILMLIST_WRITE_ERROR, aSavePath.toAbsolutePath().toString());
            return false;
        }

        return true;
    }
}