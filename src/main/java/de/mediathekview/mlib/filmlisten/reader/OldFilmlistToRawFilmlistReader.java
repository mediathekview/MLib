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
  private static final String ENTRY_DELIMITER = "],\"X\":\s*\\[";
  private static final String QUOTATION_MARK = "\"";
  private static final String ENTRY_SPLIT_PATTERN = "\",\"";
  private static final DateTimeFormatter DATE_FORMATTER =
      DateTimeFormatter.ofLocalizedDate(MEDIUM).withLocale(Locale.GERMANY);
  private static final DateTimeFormatter TIME_FORMATTER =
      DateTimeFormatter.ofLocalizedTime(SHORT).withLocale(Locale.GERMANY);
  private static final String DATE_TIME_SPLITERATOR = ",?\\s+";
  private static final String ALL_AFTER_UUID_PATTERN = "\"].*";
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
      String rawUUID = entryPart.replaceFirst(ALL_AFTER_UUID_PATTERN, "");
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

  private String removeFirstAndLastQuotationMarkFromField(String rawField) {
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
                  removeFirstAndLastQuotationMarkFromField(entryArray[0]),
                  removeFirstAndLastQuotationMarkFromField(entryArray[1]),
                  removeFirstAndLastQuotationMarkFromField(entryArray[2]),
                  removeFirstAndLastQuotationMarkFromField(entryArray[3]),
                  removeFirstAndLastQuotationMarkFromField(entryArray[4]),
                  removeFirstAndLastQuotationMarkFromField(entryArray[5]),
                  removeFirstAndLastQuotationMarkFromField(entryArray[6]),
                  removeFirstAndLastQuotationMarkFromField(entryArray[7]),
                  removeFirstAndLastQuotationMarkFromField(entryArray[8]),
                  removeFirstAndLastQuotationMarkFromField(entryArray[9]),
                  removeFirstAndLastQuotationMarkFromField(entryArray[10]),
                  removeFirstAndLastQuotationMarkFromField(entryArray[12]),
                  removeFirstAndLastQuotationMarkFromField(entryArray[14]),
                  removeFirstAndLastQuotationMarkFromField(entryArray[18]),
                  removeFirstAndLastQuotationMarkFromField(entryArray[19])));
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
