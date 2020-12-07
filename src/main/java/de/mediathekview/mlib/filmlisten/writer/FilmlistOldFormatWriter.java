package de.mediathekview.mlib.filmlisten.writer;

import de.mediathekview.mlib.daten.Filmlist;
import de.mediathekview.mlib.daten.MediaResourceComperators;
import de.mediathekview.mlib.filmlisten.FilmToFakeJsonConverter;
import de.mediathekview.mlib.messages.LibMessages;
import de.mediathekview.mlib.tool.Version;
import de.mediathekview.mlib.tool.VersionReader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collection;
import java.util.Locale;

import static java.time.format.FormatStyle.MEDIUM;
import static java.time.format.FormatStyle.SHORT;

public class FilmlistOldFormatWriter extends AbstractFilmlistWriter {
  private static final Logger LOG = LogManager.getLogger(FilmlistOldFormatWriter.class);
  private static final String LINE_BREAK = "\\n";
  private static final DateTimeFormatter DATE_TIME_FORMAT =
      DateTimeFormatter.ofLocalizedDateTime(MEDIUM, SHORT).withLocale(Locale.GERMANY);

  @Override
  public boolean write(final Filmlist aFilmlist, final Path aSavePath) {
    final FilmToFakeJsonConverter filmToFakeJsonConverter = new FilmToFakeJsonConverter();
    final Version progVersion = new VersionReader().readVersion();
    final String filmlistAsFakeJson =
        filmToFakeJsonConverter.toFakeJson(
            aFilmlist.getSorted(MediaResourceComperators.DEFAULT_COMPERATOR.getComparator()),
            DATE_TIME_FORMAT.format(aFilmlist.getCreationDate()),
            DATE_TIME_FORMAT.format(aFilmlist.getCreationDate().atZone(ZoneOffset.UTC)),
            progVersion.toString(),
            String.format(" [Vers.: %s ]", progVersion),
            aFilmlist.getListId().toString());

    final Collection<String> linesToWrite = Arrays.asList(filmlistAsFakeJson.split(LINE_BREAK));
    try {
      // https://stackoverflow.com/questions/26268132/all-inclusive-charset-to-avoid-java-nio-charset-malformedinputexception-input
      //Files.write(aSavePath, linesToWrite, StandardCharsets.UTF_8);
      BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(aSavePath.toFile()),StandardCharsets.UTF_8),512000);
      for (String s : linesToWrite) 
        bw.write(s);
      bw.flush();
      bw.close();
    } catch (final IOException ioException) {
      LOG.debug("Something went wrong on writing the film list.", ioException);
      publishMessage(LibMessages.FILMLIST_WRITE_ERROR, aSavePath.toAbsolutePath().toString());
      return false;
    }

    return true;
  }
}
