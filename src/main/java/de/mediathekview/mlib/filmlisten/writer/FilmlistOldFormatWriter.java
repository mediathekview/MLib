package de.mediathekview.mlib.filmlisten.writer;

import static java.time.format.FormatStyle.MEDIUM;
import static java.time.format.FormatStyle.SHORT;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collection;
import java.util.Locale;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.mediathekview.mlib.daten.Filmlist;
import de.mediathekview.mlib.daten.MediaResourceComperators;
import de.mediathekview.mlib.filmlisten.FilmToFakeJsonConverter;
import de.mediathekview.mlib.messages.LibMessages;
import de.mediathekview.mlib.tool.Functions;

public class FilmlistOldFormatWriter extends AbstractFilmlistWriter {
  private static final Logger LOG = LogManager.getLogger(FilmlistOldFormatWriter.class);
  private static final String LINE_BREAK = "\\n";
  private static final DateTimeFormatter DATE_TIME_FORMAT =
      DateTimeFormatter.ofLocalizedDateTime(MEDIUM, SHORT).withLocale(Locale.GERMANY);

  @Override
  public boolean write(final Filmlist aFilmlist, final Path aSavePath) {
    final FilmToFakeJsonConverter filmToFakeJsonConverter = new FilmToFakeJsonConverter();
    final String filmlistAsFakeJson = filmToFakeJsonConverter.toFakeJson(
        aFilmlist.getSorted(MediaResourceComperators.DEFAULT_COMPERATOR.getComparator()),
        DATE_TIME_FORMAT.format(aFilmlist.getCreationDate()),
        DATE_TIME_FORMAT.format(aFilmlist.getCreationDate().atZone(ZoneOffset.UTC)),
        Functions.getProgVersion().toString(), Functions.getProgVersionString(),
        aFilmlist.getListId().toString());

    final Collection<String> linesToWrite = Arrays.asList(filmlistAsFakeJson.split(LINE_BREAK));
    try {
      Files.write(aSavePath, linesToWrite, StandardCharsets.UTF_8);
    } catch (final IOException ioException) {
      LOG.debug("Something went wrong on writing the film list.", ioException);
      publishMessage(LibMessages.FILMLIST_WRITE_ERROR, aSavePath.toAbsolutePath().toString());
      return false;
    }

    return true;
  }

}
