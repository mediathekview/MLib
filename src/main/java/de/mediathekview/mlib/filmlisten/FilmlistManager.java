package de.mediathekview.mlib.filmlisten;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;

import de.mediathekview.mlib.daten.Filmlist;
import de.mediathekview.mlib.filmlisten.writer.FilmlistOldFormatWriter;
import de.mediathekview.mlib.filmlisten.writer.FilmlistWriter;
import de.mediathekview.mlib.messages.LibMessages;
import de.mediathekview.mlib.messages.MessageCreator;
import de.mediathekview.mlib.tool.XZManager;

public class FilmlistManager extends MessageCreator
{
    private static FilmlistManager instance;
    private final FilmlistOldFormatWriter filmlistOldFormatWriter;
    private final FilmlistWriter filmlistWriter;

    public static FilmlistManager getInstance()
    {
        if (instance == null)
        {
            instance = new FilmlistManager();
        }
        return instance;
    }

    private FilmlistManager()
    {
        super();
        filmlistOldFormatWriter = new FilmlistOldFormatWriter();
        filmlistWriter = new FilmlistWriter();
    }

    public boolean save(FilmlistOutputFormats aFormat, Filmlist aFilmlist, Path aSavePath)
    {
        switch (aFormat)
        {
            case JSON:
                return filmlistWriter.write(aFilmlist, aSavePath);

            case JSON_COMPRESSED:
                return filmlistWriter.write(aFilmlist, aSavePath) &&
                        compress(aSavePath);

            case OLD_JSON:
                return filmlistOldFormatWriter.write(aFilmlist, aSavePath);

            case OLD_JSON_COMPRESSED:
                return filmlistOldFormatWriter.write(aFilmlist, aSavePath) &&
                        compress(aSavePath);

            default:
                return false;
        }
    }

    private boolean compress(final Path aSavePath)
    {
        try
        {
            XZManager.getInstance().compress(aSavePath);
            return true;
        } catch (IOException ioException)
        {
            publishMessage(LibMessages.FILMLIST_COMPRESS_ERROR, aSavePath.toAbsolutePath().toString());
            return false;
        }
    }

    private boolean decompress(final Path aSavePath)
    {
        try
        {
            XZManager.getInstance().decompress(aSavePath);
            return true;
        } catch (IOException ioException)
        {
            publishMessage(LibMessages.FILMLIST_DECOMPRESS_ERROR, aSavePath.toAbsolutePath().toString());
            return false;
        }
    }

    public Filmlist importList(Path aFilePath)
    {
        //TODO
        return null;
    }

    public Filmlist importList(URL aUrl)
    {
        //TODO
        return null;
    }


}
