package de.mediathekview.mlib.filmlisten.reader;

import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Scanner;
import java.util.UUID;

import static java.time.format.FormatStyle.MEDIUM;
import static java.time.format.FormatStyle.SHORT;

@RequiredArgsConstructor
public class OldFilmlistToRawFilmlistReader {
  private static final Logger LOG = LogManager.getLogger(OldFilmlistToRawFilmlistReader.class);
  private static final String ENTRY_DELIMITER = "],\"X\":\\[";
  private static final String QUOTATION_MARK = "\"";
  private static final String ENTRY_SPLIT_PATTERN = "\",\"";
  private static final DateTimeFormatter DATE_FORMATTER =
      DateTimeFormatter.ofLocalizedDate(MEDIUM).withLocale(Locale.GERMANY);
  private static final DateTimeFormatter TIME_FORMATTER =
      DateTimeFormatter.ofLocalizedTime(SHORT).withLocale(Locale.GERMANY);
  private static final String DATE_TIME_SPLITERATOR = ",?\\s+";
  private final InputStream filmlistStream;

  public RawFilmlist read() {
    Instant start = Instant.now();
    RawFilmlist rawFilmlist = readEntryArrays();
    rawFilmlist.resolveEmptyFields();
    LOG.debug(
        "Read {} raw films in {}",
        rawFilmlist.getFilmCount(),
        Duration.between(start, Instant.now()));
    return rawFilmlist;
  }

  private UUID toListId(String entryPart) {
    try {
      String rawUUID = entryPart.replaceFirst("\"].*", "");
      return UUID.fromString(
          String.format(
              "%s-%s-%s-%s-%s",
              rawUUID.substring(0, 8),
              rawUUID.substring(8, 12),
              rawUUID.substring(12, 16),
              rawUUID.substring(16, 20),
              rawUUID.substring(20, 32)));
    } catch (final IllegalArgumentException illegalArgumentException) {
      LOG.debug("Can't parse the film list id. Setting a random uuid.", illegalArgumentException);
      return UUID.randomUUID();
    }
  }

  private LocalDateTime toCreationTime(String entryPart) {
    final String[] dateTimeSplitted =
        entryPart.replaceFirst(".*\\[\"", "").split(DATE_TIME_SPLITERATOR);
    return LocalDateTime.of(
        LocalDate.parse(dateTimeSplitted[0], DATE_FORMATTER),
        LocalTime.parse(dateTimeSplitted[1], TIME_FORMATTER));
  }

  private String clearField(String rawField) {
    if (rawField.isEmpty()) {
      return rawField;
    }

    String cleanedField = rawField;
    if (cleanedField.startsWith(QUOTATION_MARK)) {
      cleanedField = cleanedField.substring(1);
    }
    if (cleanedField.endsWith(QUOTATION_MARK)) {
      cleanedField = cleanedField.substring(0, cleanedField.length() - 1);
    }
    return cleanedField;
  }

  @NotNull
  private RawFilmlist readEntryArrays() {
    RawFilmlist.RawFilmlistBuilder rawFilmlistBuilder = RawFilmlist.builder();
    try (final Scanner scanner = new Scanner(filmlistStream, StandardCharsets.UTF_8.name());
        final Scanner entryScanner = scanner.useDelimiter(ENTRY_DELIMITER)) {

      while (entryScanner.hasNext()) {
        final String rawEntry = entryScanner.next();

        String[] entryArray = rawEntry.split(ENTRY_SPLIT_PATTERN);
        if (entryArray.length == 20) {
          rawFilmlistBuilder.rawFilm(
              new RawFilm(
                  clearField(entryArray[0]),
                  clearField(entryArray[1]),
                  clearField(entryArray[2]),
                  clearField(entryArray[3]),
                  clearField(entryArray[4]),
                  clearField(entryArray[5]),
                  clearField(entryArray[6]),
                  clearField(entryArray[7]),
                  clearField(entryArray[8]),
                  clearField(entryArray[9]),
                  clearField(entryArray[10]),
                  clearField(entryArray[11]),
                  clearField(entryArray[12]),
                  clearField(entryArray[13]),
                  clearField(entryArray[14]),
                  clearField(entryArray[15]),
                  clearField(entryArray[16]),
                  clearField(entryArray[17]),
                  clearField(entryArray[18]),
                  clearField(entryArray[19])));
        } else {
          rawFilmlistBuilder
              .creationDate(toCreationTime(entryArray[0]))
              .listId(toListId(entryArray[4]));
        }
      }
    } finally {
      try {
        filmlistStream.close();
      } catch (final IOException exception) {
        LOG.debug("Can't close the ioStream", exception);
      }
    }
    return rawFilmlistBuilder.build();
  }
}
