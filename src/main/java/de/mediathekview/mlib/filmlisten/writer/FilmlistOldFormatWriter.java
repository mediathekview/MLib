package de.mediathekview.mlib.filmlisten.writer;

import de.mediathekview.mlib.daten.Filmlist;
import de.mediathekview.mlib.daten.MediaResourceComperators;
import de.mediathekview.mlib.filmlisten.FilmToFakeJsonConverter;
import de.mediathekview.mlib.tool.Version;
import de.mediathekview.mlib.tool.VersionReader;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collection;
import java.util.Locale;

import static java.time.format.FormatStyle.MEDIUM;
import static java.time.format.FormatStyle.SHORT;

public class FilmlistOldFormatWriter extends AbstractFilmlistWriter {
  private static final String LINE_BREAK = "\\n";
  private static final DateTimeFormatter DATE_TIME_FORMAT =
      DateTimeFormatter.ofLocalizedDateTime(MEDIUM, SHORT).withLocale(Locale.GERMANY);

  @Override
  public boolean write(Filmlist filmlist, OutputStream outputStream) throws IOException {
    final FilmToFakeJsonConverter filmToFakeJsonConverter = new FilmToFakeJsonConverter();
    final Version progVersion = new VersionReader().readVersion();
    final String filmlistAsFakeJson =
        filmToFakeJsonConverter.toFakeJson(
            filmlist.getSorted(MediaResourceComperators.DEFAULT_COMPERATOR.getComparator()),
            DATE_TIME_FORMAT.format(filmlist.getCreationDate()),
            DATE_TIME_FORMAT.format(filmlist.getCreationDate().atZone(ZoneOffset.UTC)),
            progVersion.toString(),
            String.format(" [Vers.: %s ]", progVersion),
            filmlist.getListId().toString());

    final Collection<String> linesToWrite = Arrays.asList(filmlistAsFakeJson.split(LINE_BREAK));
    try (final BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(outputStream,StandardCharsets.UTF_8),512000)){
      for (String line : linesToWrite) {
        bufferedWriter.write(line);
        }
      bufferedWriter.flush();
    }

    return true;
  }
}
