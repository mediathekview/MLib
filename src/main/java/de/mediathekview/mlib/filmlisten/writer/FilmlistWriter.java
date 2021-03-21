package de.mediathekview.mlib.filmlisten.writer;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.time.Duration;
import java.time.LocalDateTime;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import de.mediathekview.mlib.daten.Filmlist;
import de.mediathekview.mlib.daten.GsonDurationAdapter;
import de.mediathekview.mlib.daten.GsonLocalDateTimeAdapter;
import de.mediathekview.mlib.messages.LibMessages;
import de.mediathekview.mlib.messages.listener.MessageListener;

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

    @Override
    public boolean write(final Filmlist aFilmlist, final Path aSavePath)
    {
      GsonBuilder gsonBuilder = new GsonBuilder();
      gsonBuilder.registerTypeAdapter(LocalDateTime.class, new GsonLocalDateTimeAdapter());
      gsonBuilder.registerTypeAdapter(Duration.class, new GsonDurationAdapter());
      Gson gson = gsonBuilder.create();
      try (final BufferedWriter fileWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(aSavePath.toFile()),StandardCharsets.UTF_8),512000))
        {
           gson.toJson(aFilmlist, fileWriter);
           fileWriter.flush();
        }
        catch (final IOException ioException)
        {
            LOG.debug("Something went wrong on writing the film list.", ioException);
            publishMessage(LibMessages.FILMLIST_WRITE_ERROR, aSavePath.toAbsolutePath().toString());
            return false;
        }

        return true;
    }
}