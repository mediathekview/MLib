package de.mediathekview.mlib.filmlisten.reader;

import static java.time.format.FormatStyle.MEDIUM;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Scanner;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.mediathekview.mlib.daten.Film;
import de.mediathekview.mlib.daten.Filmlist;
import de.mediathekview.mlib.tool.Functions;

public class FilmlistOldFormatReader extends AbstractFilmlistReader {
	private static final String ENTRY_DELIMETER = "\\],";
	private static final Logger LOG = LogManager.getLogger(FilmlistOldFormatReader.class);
	private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofLocalizedDate(MEDIUM)
			.withLocale(Locale.GERMANY);
	private static final String ENTRY_PATTERN = "\"\\w*\"\\s?:\\s*\\[\\s?(\"([^\"]|\\\\\")*\",?\\s?)*";
	private static final String ENTRY_SPLIT_PATTERN = "\"(\\\\\"|[^\"])*\"";
	private static final String FILM_ENTRY_ID = "X";

	@Override
	public Optional<Filmlist> read(InputStream aInputStream) {
		try (Scanner scanner = new Scanner(aInputStream,StandardCharsets.UTF_8.name());
				Scanner entryScanner = scanner.useDelimiter(ENTRY_DELIMETER)) {
			Filmlist filmlist = new Filmlist();

			boolean isFirst = true;
			Future<Film> filmEntryBefore = null;

			List<String> entries = findEntries(entryScanner);

			ExecutorService executorService = Executors.newCachedThreadPool();
			List<Future<Film>> futureFilms = new ArrayList<>();
			for (String entry : entries) {
				List<String> splittedEntry = splittEntry(entry);

				if (!splittedEntry.isEmpty()) {
					if (isFirst) {
						setMetaInfo(filmlist, splittedEntry);
						isFirst = false;
					} else if (splittedEntry.size() == 21 && FILM_ENTRY_ID.equals(splittedEntry.get(0))) {
						try {
							final Future<Film> newEntry = executorService
									.submit(new OldFilmlistEntryToFilmTask(splittedEntry, filmEntryBefore));
							futureFilms.add(newEntry);
							filmEntryBefore = newEntry;
						} catch (Exception exception) {
							LOG.debug(String.format("Error on converting the following text to a film:\n %s ", entry));
						}
					}
				}
			}
			futureFilms.forEach(f -> {
				try {
					filmlist.add(f.get());
				} catch (InterruptedException | ExecutionException exception) {
					LOG.debug("Some error occured during converting a old film list entry to an film.", exception);
				}
			});
			return Optional.of(filmlist);
		} finally {
			try {
				aInputStream.close();
			} catch (IOException exception) {
				LOG.debug("Can't close the ioStream", exception);
			}
		}
	}

	private List<String> findEntries(Scanner entryScanner) {
		List<String> entries = new ArrayList<>();

		while (entryScanner.hasNext()) {
			String entry = entryScanner.next();
			Matcher entryMatcher = Pattern.compile(ENTRY_PATTERN).matcher(entry);
			if (entryMatcher.find()) {
				entries.add(entryMatcher.group());
			}

		}
		return entries;
	}

	private void setMetaInfo(final Filmlist aFilmlist, final List<String> aSplittedEntry) {
		try {
			aFilmlist.setCreationDate(LocalDateTime.parse(aSplittedEntry.get(0), DATE_FORMATTER));
			aFilmlist.setListId(UUID.fromString(aSplittedEntry.get(4)));
		} catch (Exception exception) {
			LOG.debug("Somethin went wrong on setting the meta data of filmlist.", exception);
		}
	}

	private List<String> splittEntry(final String aEntry) {
		List<String> entrySplits = new ArrayList<>();
		Matcher entrySplitMatcher = Pattern.compile(ENTRY_SPLIT_PATTERN).matcher(aEntry);
		while (entrySplitMatcher.find()) {
			entrySplits.add(Functions.unescape(entrySplitMatcher.group().replaceFirst("\"", "").replaceAll("\"$", "")));
		}

		return entrySplits;
	}

}
