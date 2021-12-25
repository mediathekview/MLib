package de.mediathekview.mlib.filmlisten.reader;

import de.mediathekview.mlib.daten.Film;
import de.mediathekview.mlib.daten.Filmlist;
import de.mediathekview.mlib.tool.TextCleaner;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.time.format.FormatStyle.MEDIUM;
import static java.time.format.FormatStyle.SHORT;

public class FilmlistOldFormatReader extends AbstractFilmlistReader {
  private static final String ENTRY_DELIMETER = "\\],";
  private static final Logger LOG = LogManager.getLogger(FilmlistOldFormatReader.class);
  private static final DateTimeFormatter DATE_FORMATTER =
      DateTimeFormatter.ofLocalizedDate(MEDIUM).withLocale(Locale.GERMANY);
  private static final DateTimeFormatter TIME_FORMATTER =
      DateTimeFormatter.ofLocalizedTime(SHORT).withLocale(Locale.GERMANY);
  private static final String ENTRY_PATTERN = "\"\\w*\"\\s?:\\s*\\[\\s?(\"([^\"]|\\\")*\",?\\s?)*";
  private static final String ENTRY_SPLIT_PATTERN = "\"(\\\\\"|[^\"])*\"";
  private static final String FILM_ENTRY_ID = "X";
  private static final String DATE_TIME_SPLITTERATOR = ",?\\s+";
  private static final String QUOTATION_MARK = "\"";

  @Override
  public Optional<Filmlist> read(final InputStream aInputStream) {
    try (final Scanner scanner = new Scanner(aInputStream, StandardCharsets.UTF_8.name());
        final Scanner entryScanner = scanner.useDelimiter(ENTRY_DELIMETER)) {
      return convertEntriesToFilms(findEntries(entryScanner));
    } finally {
      try {
        aInputStream.close();
      } catch (final IOException exception) {
        LOG.debug("Can't close the ioStream", exception);
      }
    }
  }

  @NotNull
  private Optional<Filmlist> convertEntriesToFilms(final List<String> entries) {
    final Filmlist filmlist = new Filmlist();
    final List<Future<Film>> futureFilms = asyncConvertEntriesToFilms(entries, filmlist);
    futureFilms.stream()
        .map(
            filmFuture -> {
              try {
                return filmFuture.get();
              } catch (final InterruptedException interruptedException) {
                LOG.debug(
                    "Some error occured during converting a old film list entry to an film.",
                    interruptedException);
                Thread.currentThread().interrupt();
              } catch (final Exception exception) {
                LOG.debug(
                    "Some error occured during converting a old film list entry to an film.",
                    exception);
              }
              return null;
            })
        .filter(Objects::nonNull)
        .filter(film -> !film.getUrls().isEmpty())
        .forEach(filmlist::add);
    return Optional.of(filmlist);
  }

  @NotNull
  private List<Future<Film>> asyncConvertEntriesToFilms(
      final List<String> entries, final Filmlist filmlist) {
    final ExecutorService executorService = Executors.newWorkStealingPool();
    boolean isFirst = true;
    Future<Film> filmEntryBefore = null;
    final List<Future<Film>> futureFilms = new ArrayList<>();

    final List<List<String>> splittedEntries =
        entries.stream()
            .map(this::splittEntry)
            .filter(splittEntry -> !splittEntry.isEmpty())
            .toList();

    for (final List<String> splittedEntry : splittedEntries) {
      if (isFirst) {
        setMetaInfo(filmlist, splittedEntry);
        isFirst = false;
      } else if (splittedEntry.size() == 21 && FILM_ENTRY_ID.equals(splittedEntry.get(0))) {
        filmEntryBefore =
            convertEntryToFilm(filmEntryBefore, executorService, futureFilms, splittedEntry);
      }
    }
    return futureFilms;
  }

  private Future<Film> convertEntryToFilm(
      Future<Film> filmEntryBefore,
      final ExecutorService executorService,
      final List<Future<Film>> futureFilms,
      final List<String> splittedEntry) {
    try {
      final Future<Film> newEntry =
          executorService.submit(new OldFilmlistEntryToFilmTask(splittedEntry, filmEntryBefore));
      futureFilms.add(newEntry);
      filmEntryBefore = newEntry;
    } catch (final Exception exception) {
      LOG.debug(
          String.format("Error on converting the following text to a film:%n %s ", splittedEntry));
    }
    return filmEntryBefore;
  }

  private List<String> findEntries(final Scanner entryScanner) {
    final List<String> entries = new ArrayList<>();

    while (entryScanner.hasNext()) {
      final String entry = entryScanner.next();
      final Matcher entryMatcher = Pattern.compile(ENTRY_PATTERN).matcher(entry);
      if (entryMatcher.find()) {
        entries.add(entryMatcher.group());
      }
    }
    return entries;
  }

  private void setMetaInfo(final Filmlist aFilmlist, final List<String> aSplittedEntry) {
    try {
      setCreationTime(aFilmlist, aSplittedEntry);
      setListId(aFilmlist, aSplittedEntry);
    } catch (final Exception exception) {
      LOG.debug("Somethin went wrong on setting the meta data of filmlist.", exception);
    }
  }

  private void setListId(final Filmlist aFilmlist, final List<String> aSplittedEntry) {
    try {
      aFilmlist.setListId(UUID.fromString(aSplittedEntry.get(4)));
    } catch (final IllegalArgumentException illegalArgumentException) {
      LOG.debug("Can't parse the film list id. Setting a random uuid.", illegalArgumentException);
      aFilmlist.setListId(UUID.randomUUID());
    }
  }

  private void setCreationTime(final Filmlist aFilmlist, final List<String> aSplittedEntry) {
    final String[] dateTimeSplitted = aSplittedEntry.get(1).split(DATE_TIME_SPLITTERATOR);
    aFilmlist.setCreationDate(
        LocalDateTime.of(
            LocalDate.parse(dateTimeSplitted[0], DATE_FORMATTER),
            LocalTime.parse(dateTimeSplitted[1], TIME_FORMATTER)));
  }

  private List<String> splittEntry(final String aEntry) {
    final List<String> entrySplits = new ArrayList<>();
    final Matcher entrySplitMatcher = Pattern.compile(ENTRY_SPLIT_PATTERN).matcher(aEntry);
    while (entrySplitMatcher.find()) {
      entrySplits.add(
          TextCleaner.clean(
              entrySplitMatcher.group().replaceFirst(QUOTATION_MARK, "").replaceAll("\"$", "")));
    }

    return entrySplits;
  }
}
