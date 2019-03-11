package de.mediathekview.mlib.filmlisten;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import de.mediathekview.mlib.compression.CompressionManager;
import de.mediathekview.mlib.compression.CompressionType;
import de.mediathekview.mlib.daten.Filmlist;
import de.mediathekview.mlib.filmlisten.reader.FilmlistOldFormatReader;
import de.mediathekview.mlib.filmlisten.reader.FilmlistReader;
import de.mediathekview.mlib.filmlisten.writer.AbstractFilmlistWriter;
import de.mediathekview.mlib.filmlisten.writer.FilmlistOldFormatWriter;
import de.mediathekview.mlib.filmlisten.writer.FilmlistWriter;
import de.mediathekview.mlib.messages.LibMessages;
import de.mediathekview.mlib.messages.MessageCreator;
import de.mediathekview.mlib.tool.MVHttpClient;
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
    InputStream input = decompressInputStreamIfFormatNeedsTo(aFormat, aInputStream);

    try {
      if (aFormat.isOldFormat()) {
        return new FilmlistOldFormatReader().read(input);
      } else {
        return new FilmlistReader().read(input);
      }
    } finally {
      publishMessage(LibMessages.FILMLIST_IMPORT_FINISHED);
    }
  }

private InputStream decompressInputStreamIfFormatNeedsTo(final FilmlistFormats aFormat, final InputStream aInputStream)
		throws IOException {
	InputStream input;
    final Optional<CompressionType> compressionType = aFormat.getCompressionType();
    if (compressionType.isPresent()) {
      input = CompressionManager.getInstance().decompress(compressionType.get(), aInputStream);
    } else {
      input = aInputStream;
    }
	return input;
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
      if (aFormat.isOldFormat()) {
        return save(filmlistOldFormatWriter, aFormat, aFilmlist, aSavePath);
      } else {
        return save(filmlistWriter, aFormat, aFilmlist, aSavePath);
      }
    } finally {
      publishMessage(LibMessages.FILMLIST_WRITE_FINISHED, aSavePath);
    }
  }

  private boolean compress(final FilmlistFormats aFormat, final Path aSourcePath,
      final Path aTargetPath) {
    try {
      CompressionManager.getInstance().compress(aFormat.getCompressionType().get(), aSourcePath,
          aTargetPath);
      return true;
    } catch (final IOException ioException) {
      publishMessage(LibMessages.FILMLIST_COMPRESS_ERROR, aTargetPath.toAbsolutePath().toString());
      return false;
    }
  }

  private boolean compressFile(final AbstractFilmlistWriter aWriter, final FilmlistFormats aFormat,
      final Path aSavePath, final Filmlist aFilmlist) {
    final Path tempPath = aSavePath.resolveSibling(aSavePath.getFileName().toString() + TEMP_ENDING);
    try {
      return aWriter.write(aFilmlist, tempPath) && compress(aFormat, tempPath, aSavePath);
    } finally {
      try {
        Files.deleteIfExists(tempPath);
      } catch (final IOException ioException) {
        LOG.error(String.format("Can't delete temp file \"%s\".", tempPath.toString()));
      }
    }

  }

  private boolean save(final AbstractFilmlistWriter aFilmlistwirter, final FilmlistFormats aFormat,
      final Filmlist aFilmlist, final Path aSavePath) {
    if (aFormat.getCompressionType().isPresent()) {
      return compressFile(aFilmlistwirter, aFormat, aSavePath, aFilmlist);
    } else {
      return aFilmlistwirter.write(aFilmlist, aSavePath);
    }
  }

}
