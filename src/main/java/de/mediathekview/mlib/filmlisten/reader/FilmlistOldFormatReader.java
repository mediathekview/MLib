package de.mediathekview.mlib.filmlisten.reader;

import static java.time.format.FormatStyle.MEDIUM;

import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Scanner;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.mediathekview.mlib.daten.Film;
import de.mediathekview.mlib.daten.FilmUrl;
import de.mediathekview.mlib.daten.Filmlist;
import de.mediathekview.mlib.daten.GeoLocations;
import de.mediathekview.mlib.daten.ListeFilme;
import de.mediathekview.mlib.daten.Qualities;
import de.mediathekview.mlib.daten.Sender;
import de.mediathekview.mlib.tool.Functions;

public class FilmlistOldFormatReader extends AbstractFilmlistReader {
	private static final String ENTRY_DELIMETER = "\\],";
	private static final Logger LOG = LogManager.getLogger(FilmlistOldFormatReader.class);
	private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofLocalizedDate(MEDIUM)
			.withLocale(Locale.GERMANY);
	private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofLocalizedTime(MEDIUM)
			.withLocale(Locale.GERMANY);
	private static final int PROGRESS_MAX = 100;
	private static final String ENTRY_PATTERN = "\"\\w*\"\\s?:\\s*\\[\\s?(\"([^\"]|\\\\\")*\",?\\s?)*";
	private static final String ENTRY_SPLIT_PATTERN = "\"(\\\\\"|[^\"])*\"";
	private static final String FILM_ENTRY_ID = "X";
	private static final String EXCEPTION_TEXT_CANT_BUILD_FILM = "Can't build a Film from splits.";
	private static final String URL_SPLITTERATOR = "\\|";

	@Override
	public Optional<Filmlist> read(InputStream aInputStream) {
		try (Scanner entryScanner = new Scanner(aInputStream).useDelimiter(ENTRY_DELIMETER))
        {
            Filmlist filmlist = new Filmlist();
            

            boolean isFirst = true;
            Film filmEntryBefore = null;
            
            List<String> entries = findEntries(entryScanner);
            
            for(String entry : entries)
            {
                List<String> splittedEntry = splittEntry(entry);

                if (!splittedEntry.isEmpty())
                {
                    if (isFirst)
                    {
                        setMetaInfo(filmlist, splittedEntry);
                        isFirst = false;
                    } else if (splittedEntry.size() == 21 && FILM_ENTRY_ID.equals(splittedEntry.get(0)))
                    {
                        try
                        {
                            final Film newEntry = entrySplitsToFilm(splittedEntry, filmEntryBefore);
                            /*
                             * TODO Move the entrySplitsToFilm Part to a extra class
                             *
                             * and work with Future objects and Executor service
                             */
                            listeFilme.add(newEntry);
                            filmEntryBefore = newEntry;
                            notifyProgress(newEntry.getUrl(Qualities.NORMAL).toString(),count*100/entries.size(), count,false);
                        } catch (Exception exception)
                        {
                            LOG.fatal(EXCEPTION_TEXT_CANT_BUILD_FILM, exception);
                            LOG.debug(String.format("Error on converting the following text to a film:\n %s ",entry));
                            notifyProgress("",count*100/entries.size(), count,true);
                        }
                    }
                }
            }
            return listeFilme;
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

	private void setMetaInfo(final Filmlist aListeFilme, final List<String> aSplittedEntry) {
		// TODO
	}

	private FilmUrl urlTextToUri(final URI aUrlNormal, final long aGroesse, final String aUrlText)
			throws URISyntaxException {
		FilmUrl filmUrl = null;

		String[] splittedUrlText = aUrlText.split(URL_SPLITTERATOR);
		if (splittedUrlText.length == 2) {
			int lengthOfOld = Integer.parseInt(splittedUrlText[0]);

			StringBuilder newUrlBuilder = new StringBuilder();
			newUrlBuilder.append(aUrlNormal.toString().substring(0, lengthOfOld));
			newUrlBuilder.append(splittedUrlText[1]);

			filmUrl = new FilmUrl(new URI(newUrlBuilder.toString()), aGroesse);
		}
		return filmUrl;
	}

	private Collection<GeoLocations> readGeoLocations(final String aGeoText) {
		Collection<GeoLocations> geoLocations = new ArrayList<>();

		GeoLocations singleGeoLocation = GeoLocations.getFromDescription(aGeoText);
		if (singleGeoLocation == null) {
			for (String geoText : aGeoText.split(String.valueOf(GEO_SPLITTERATOR))) {
				GeoLocations geoLocation = GeoLocations.getFromDescription(geoText);
				if (geoLocation != null) {
					geoLocations.add(geoLocation);
				}
			}
		} else {
			geoLocations.add(singleGeoLocation);
		}

		return geoLocations;
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
