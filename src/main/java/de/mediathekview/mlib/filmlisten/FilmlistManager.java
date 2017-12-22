package de.mediathekview.mlib.filmlisten;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import de.mediathekview.mlib.daten.Filmlist;
import de.mediathekview.mlib.filmlisten.reader.FilmlistOldFormatReader;
import de.mediathekview.mlib.filmlisten.reader.FilmlistReader;
import de.mediathekview.mlib.filmlisten.writer.AbstractFilmlistWriter;
import de.mediathekview.mlib.filmlisten.writer.FilmlistOldFormatWriter;
import de.mediathekview.mlib.filmlisten.writer.FilmlistWriter;
import de.mediathekview.mlib.messages.LibMessages;
import de.mediathekview.mlib.messages.MessageCreator;
import de.mediathekview.mlib.tool.MVHttpClient;
import de.mediathekview.mlib.tool.XZManager;
import okhttp3.OkHttpClient;
import okhttp3.Request;

public class FilmlistManager extends MessageCreator {

  private static final Logger LOG = LogManager.getLogger(FilmlistManager.class);
  private static final String TEMP_ENDING = "_TEMP";
  private static FilmlistManager instance;
  private final FilmlistOldFormatWriter filmlistOldFormatWriter;
  private final FilmlistWriter filmlistWriter;

  private FilmlistManager() {
    super();
    filmlistOldFormatWriter = new FilmlistOldFormatWriter();
    filmlistWriter = new FilmlistWriter();

  }

  public static FilmlistManager getInstance() {
    if (instance == null) {
      instance = new FilmlistManager();
    }
    return instance;
  }

  public Optional<Filmlist> importList(final FilmlistFormats aFormat,
      final InputStream aInputStream) throws IOException {
    publishMessage(LibMessages.FILMLIST_IMPORT_STARTED);
    InputStream input;
    if (FilmlistFormats.JSON_COMPRESSED.equals(aFormat)
        || FilmlistFormats.OLD_JSON_COMPRESSED.equals(aFormat)) {
      input = XZManager.getInstance().decompress(aInputStream);
    } else {
      input = aInputStream;
    }

    try {
      if (FilmlistFormats.JSON.equals(aFormat) || FilmlistFormats.JSON_COMPRESSED.equals(aFormat)) {
        return new FilmlistReader().read(input);
      } else {
        return new FilmlistOldFormatReader().read(input);
      }
    } finally {
      publishMessage(LibMessages.FILMLIST_IMPORT_FINISHED);
    }
  }

  public Optional<Filmlist> importList(final FilmlistFormats aFormat, final Path aFilePath)
      throws IOException {
    try (InputStream fileInputStream = Files.newInputStream(aFilePath)) {
      return importList(aFormat, fileInputStream);
    }
  }

  public Optional<Filmlist> importList(final FilmlistFormats aFormat, final URL aUrl)
      throws IOException {
    final Request request = new Request.Builder().url(aUrl).build();
    final OkHttpClient httpClient = MVHttpClient.getInstance().getHttpClient();
    try (InputStream fileInputStream = httpClient.newCall(request).execute().body().byteStream()) {
      return importList(aFormat, fileInputStream);
    }
  }

  public boolean save(final FilmlistFormats aFormat, final Filmlist aFilmlist,
      final Path aSavePath) {
    try {
      publishMessage(LibMessages.FILMLIST_WRITE_STARTED, aSavePath);
      filmlistWriter.addAllMessageListener(messageListeners);
      filmlistOldFormatWriter.addAllMessageListener(messageListeners);
      switch (aFormat) {
        case JSON:
          return filmlistWriter.write(aFilmlist, aSavePath);

        case JSON_COMPRESSED:
          return compressFile(filmlistWriter, aSavePath, aFilmlist);

        case OLD_JSON:
          return filmlistOldFormatWriter.write(aFilmlist, aSavePath);

        case OLD_JSON_COMPRESSED:
          return compressFile(filmlistOldFormatWriter, aSavePath, aFilmlist);

        default:
          return false;
      }
    } finally {
      publishMessage(LibMessages.FILMLIST_WRITE_FINISHED, aSavePath);
    }
  }

  private boolean compress(final Path aSourcePath, final Path aTargetPath) {
    try {
      XZManager.getInstance().compress(aSourcePath, aTargetPath);
      return true;
    } catch (final IOException ioException) {
      publishMessage(LibMessages.FILMLIST_COMPRESS_ERROR, aTargetPath.toAbsolutePath().toString());
      return false;
    }
  }

  private boolean compressFile(final AbstractFilmlistWriter aWriter, final Path aSavePath,
      final Filmlist aFilmlist) {
    final Path tempPath = aSavePath.resolveSibling(aSavePath.toString() + TEMP_ENDING);
    try {
      return aWriter.write(aFilmlist, tempPath) && compress(tempPath, aSavePath);
    } finally {
      try {
        Files.deleteIfExists(tempPath);
      } catch (final IOException ioException) {
        LOG.error(String.format("Can't delete temp file \"%s\".", tempPath.toString()));
      }
    }

  }

}
