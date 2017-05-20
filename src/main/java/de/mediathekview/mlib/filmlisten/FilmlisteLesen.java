/*
 * MediathekView
 * Copyright (C) 2008 W. Xaver
 * W.Xaver[at]googlemail.com
 * http://zdfmediathk.sourceforge.net/
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package de.mediathekview.mlib.filmlisten;

import java.io.*;
import java.lang.reflect.Type;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipInputStream;

import javax.swing.event.EventListenerList;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import de.mediathekview.mlib.daten.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.FastDateFormat;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.tukaani.xz.XZInputStream;

import de.mediathekview.mlib.Config;
import de.mediathekview.mlib.Const;
import de.mediathekview.mlib.filmesuchen.ListenerFilmeLaden;
import de.mediathekview.mlib.filmesuchen.ListenerFilmeLadenEvent;
import de.mediathekview.mlib.tool.InputStreamProgressMonitor;
import de.mediathekview.mlib.tool.Log;
import de.mediathekview.mlib.tool.MVHttpClient;
import de.mediathekview.mlib.tool.ProgressMonitorInputStream;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

import static java.time.format.FormatStyle.MEDIUM;
import static java.time.format.FormatStyle.SHORT;

public class FilmlisteLesen
{
    private static final Logger LOG = LogManager.getLogger(FilmlisteLesen.class);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofLocalizedDate(MEDIUM).withLocale(Locale.GERMANY);
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofLocalizedTime(MEDIUM).withLocale(Locale.GERMANY);
    private static final int PROGRESS_MAX = 100;
    private static final String ENTRY_PATTERN = "\"\\w*\":\\s*\\[(\"[^\"]*\",?)*]";
    private static final String ENTRY_SPLIT_PATTERN = "\"[^\"]*\"";
    private static final String FILM_ENTRY_ID = "X";
    private static final char GEO_SPLITTERATOR = '-';
    private static final String EXCEPTION_TEXT_CANT_BUILD_FILM = "Can't build a Film from splits.";
    private static final String URL_SPLITTERATOR = "\\|";
    private static WorkMode workMode = WorkMode.NORMAL; // die Klasse wird an verschiedenen Stellen benutzt, klappt sonst nicht immer, zB. FilmListe zu alt und neu laden
    private final EventListenerList listeners = new EventListenerList();
    private int max = 0;
    private int progress = 0;

    /**
     * Set the specific work mode for reading film list.
     * In FASTAUTO mode, no film descriptions will be read into memory.
     *
     * @param mode The mode in which to operate when reading film list.
     */
    public static void setWorkMode(WorkMode mode)
    {
        workMode = mode;
    }

    public void addAdListener(ListenerFilmeLaden listener)
    {
        listeners.add(ListenerFilmeLaden.class, listener);
    }

    private InputStream selectDecompressor(String source, InputStream in) throws Exception
    {
        if (source.endsWith(Const.FORMAT_XZ))
        {
            in = new XZInputStream(in);
        } else if (source.endsWith(Const.FORMAT_ZIP))
        {
            ZipInputStream zipInputStream = new ZipInputStream(in);
            zipInputStream.getNextEntry();
            in = zipInputStream;
        }
        return in;
    }

    public ListeFilme readData(InputStream aInputStream) throws IOException
    {
        try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(aInputStream)))
        {
            ListeFilme listeFilme = new ListeFilme();

            String fileAsText = allLinesToOneText(bufferedReader);
            Matcher entryMatcher = Pattern.compile(ENTRY_PATTERN).matcher(fileAsText);

            boolean isFirst = true;
            Film filmEntryBefore = null;
            while (entryMatcher.find())
            {
                String entry = entryMatcher.group();
                List<String> splittedEntry = splittEntry(entry);

                if (!splittedEntry.isEmpty())
                {
                    if (isFirst)
                    {
                        setMetaInfo(listeFilme, splittedEntry);
                        isFirst = false;
                    } else if (splittedEntry.size() == 21 && FILM_ENTRY_ID.equals(splittedEntry.get(0)))
                    {
                        try
                        {
                            filmEntryBefore = entrySplitsToFilm(splittedEntry, filmEntryBefore);
                            listeFilme.add(filmEntryBefore);
                        } catch (Exception exception)
                        {
                            LOG.fatal(EXCEPTION_TEXT_CANT_BUILD_FILM, exception);
                        }
                    }
                }
            }

            return listeFilme;
        }
    }

    private void setMetaInfo(final ListeFilme aListeFilme, final List<String> aSplittedEntry)
    {
        for (int i = 0; i < ListeFilme.MAX_ELEM; i++)
        {
            aListeFilme.metaDaten[i] = aSplittedEntry.get(i);
        }
    }

    private Film entrySplitsToFilm(List<String> aEntrySplits, Film aFilmEntryBefore) throws Exception
    {
        try
        {
            String senderText = aEntrySplits.get(1);
            Sender sender;
            if (senderText.isEmpty() && aFilmEntryBefore != null)
            {
                sender = aFilmEntryBefore.getSender();
            } else
            {
                sender = Sender.valueOf(senderText);
            }

            String thema = aEntrySplits.get(2);
            if (thema.isEmpty() && aFilmEntryBefore != null)
            {
                thema = aFilmEntryBefore.getThema();
            }

            String titel = aEntrySplits.get(3);
            if (titel.isEmpty() && aFilmEntryBefore != null)
            {
                titel = aFilmEntryBefore.getTitel();
            }
            LocalDate date = LocalDate.parse(aEntrySplits.get(4), DATE_FORMATTER);
            LocalTime time = LocalTime.parse(aEntrySplits.get(5), TIME_FORMATTER);

            Duration dauer = Duration.between(LocalTime.MIDNIGHT, LocalTime.parse(aEntrySplits.get(6)));
            long groesse = Long.parseLong(aEntrySplits.get(7));
            String beschreibung = aEntrySplits.get(8);

            URI urlNormal = new URI(aEntrySplits.get(9));
            URI urlWebseite = new URI(aEntrySplits.get(10));

            String urlTextUntertitel = aEntrySplits.get(11);

            String urlTextKlein = aEntrySplits.get(13);
            String urlTextHD = aEntrySplits.get(15);

            //Ignoring RTMP because can't find any usage.

            //Ignoring Film URL History because can't find any usage.

            Collection<GeoLocations> geoLocations = readGeoLocations(aEntrySplits.get(19));

            String neu = aEntrySplits.get(20);

            Film film = new Film(UUID.randomUUID(), geoLocations, sender, titel, thema, LocalDateTime.of(date, time), dauer, urlWebseite);

            if (StringUtils.isNotBlank(neu))
            {
                film.setNeu(Boolean.parseBoolean(neu));
            }

            film.addUrl(Qualities.NORMAL, new FilmUrl(urlNormal, groesse));
            film.setBeschreibung(beschreibung);

            if (!urlTextUntertitel.isEmpty())
            {
                film.addSubtitle(new URI(urlTextUntertitel));
            }

            if (!urlTextKlein.isEmpty())
            {
                FilmUrl urlKlein = urlTextToUri(urlNormal, groesse, urlTextKlein);
                if (urlKlein != null)
                {
                    film.addUrl(Qualities.SMALL, urlKlein);
                }
            }

            if (!urlTextHD.isEmpty())
            {
                FilmUrl urlHD = urlTextToUri(urlNormal, groesse, urlTextHD);
                if (urlHD != null)
                {
                    film.addUrl(Qualities.HD, urlHD);
                }
            }


            return film;
        } catch (Exception exception)
        {
            throw new Exception(EXCEPTION_TEXT_CANT_BUILD_FILM, exception);
        }
    }

    private FilmUrl urlTextToUri(final URI aUrlNormal, final long aGroesse, final String aUrlText) throws URISyntaxException
    {
        FilmUrl filmUrl = null;

        String[] splittedUrlText = aUrlText.split(URL_SPLITTERATOR);
        if (splittedUrlText.length == 2)
        {
            int lengthOfOld = Integer.parseInt(splittedUrlText[0]);

            StringBuilder newUrlBuilder = new StringBuilder();
            newUrlBuilder.append(aUrlNormal.toString().substring(0,lengthOfOld));
            newUrlBuilder.append(splittedUrlText[1]);

            filmUrl = new FilmUrl(new URI(newUrlBuilder.toString()), aGroesse);
        }
        return filmUrl;
    }

    private Collection<GeoLocations> readGeoLocations(final String aGeoText)
    {
        Collection<GeoLocations> geoLocations = new ArrayList<>();

        GeoLocations singleGeoLocation = GeoLocations.getFromDescription(aGeoText);
        if (singleGeoLocation == null)
        {
            for (String geoText : aGeoText.split(String.valueOf(GEO_SPLITTERATOR)))
            {
                GeoLocations geoLocation = GeoLocations.getFromDescription(geoText);
                if (geoLocation != null)
                {
                    geoLocations.add(geoLocation);
                }
            }
        } else
        {
            geoLocations.add(singleGeoLocation);
        }

        return geoLocations;
    }

    private List<String> splittEntry(final String aEntry)
    {
        List<String> entrySplits = new ArrayList<>();
        Matcher entrySplitMatcher = Pattern.compile(ENTRY_SPLIT_PATTERN).matcher(aEntry);
        while (entrySplitMatcher.find())
        {
            entrySplits.add(entrySplitMatcher.group().replaceAll("\"", ""));
        }

        return entrySplits;
    }

    private String allLinesToOneText(final BufferedReader bufferedReader)
    {
        StringBuilder textBuilder = new StringBuilder();
        bufferedReader.lines().forEach(textBuilder::append);
        return textBuilder.toString();
    }

    /**
     * Read a locally available filmlist.
     *
     * @param aSource file path as string
     */
    private ListeFilme processFromFile(String aSource)
    {
        notifyProgress(aSource, PROGRESS_MAX);
        try (InputStream in = selectDecompressor(aSource, Files.newInputStream(Paths.get(aSource))))
        {
            return readData(in);
        } catch (FileNotFoundException ex)
        {
            Log.errorLog(894512369, "FilmListe existiert nicht: " + aSource);
        } catch (Exception ex)
        {
            Log.errorLog(945123641, ex, "FilmListe: " + aSource);
        }
        return new ListeFilme();
    }

    public ListeFilme readFilmListe(String source, int days)
    {
        ListeFilme listeFilme;
        try
        {
            Log.sysLog("Liste Filme lesen von: " + source);
            this.notifyStart(source, PROGRESS_MAX); // für die Progressanzeige

            if (!source.startsWith("http"))
            {
                listeFilme = processFromFile(source);
            } else
            {
                listeFilme = processFromWeb(new URL(source));
            }

            if (Config.getStop())
            {
                Log.sysLog("--> Abbruch");
            }
        } catch (MalformedURLException ex)
        {
            ex.printStackTrace();
            listeFilme = new ListeFilme();
        }

        notifyFertig(source, listeFilme);
        return listeFilme;
    }

    /**
     * Download a process a filmliste from the web.
     *
     * @param source source url as string
     */
    private ListeFilme processFromWeb(URL source)
    {
        Request.Builder builder = new Request.Builder().url(source);
        builder.addHeader("User-Agent", Config.getUserAgent());

        //our progress monitor callback
        InputStreamProgressMonitor monitor = new InputStreamProgressMonitor()
        {
            private int oldProgress = 0;

            @Override
            public void progress(long bytesRead, long size)
            {
                final int iProgress = (int) (bytesRead * 100 / size);
                if (iProgress != oldProgress)
                {
                    oldProgress = iProgress;
                    notifyProgress(source.toString(), iProgress);
                }
            }
        };

        try (Response response = MVHttpClient.getInstance().getHttpClient().newCall(builder.build()).execute();
             ResponseBody body = response.body())
        {
            if (response.isSuccessful())
            {
                try (InputStream input = new ProgressMonitorInputStream(body.byteStream(), body.contentLength(), monitor))
                {
                    try (InputStream is = selectDecompressor(source.toString(), input))
                    {
                        return readData(is);
                    }
                }
            }
        } catch (Exception ex)
        {
            Log.errorLog(945123641, ex, "FilmListe: " + source);
        }
        return new ListeFilme();
    }

    private void notifyStart(String url, int mmax)
    {
        max = mmax;
        progress = 0;
        for (ListenerFilmeLaden l : listeners.getListeners(ListenerFilmeLaden.class))
        {
            l.start(new ListenerFilmeLadenEvent(url, "", max, 0, 0, false));
        }
    }

    private void notifyProgress(String url, int iProgress)
    {
        progress = iProgress;
        if (progress > max)
        {
            progress = max;
        }
        for (ListenerFilmeLaden l : listeners.getListeners(ListenerFilmeLaden.class))
        {
            l.progress(new ListenerFilmeLadenEvent(url, "Download", max, progress, 0, false));
        }
    }

    private void notifyFertig(String url, ListeFilme liste)
    {
        Log.sysLog("Liste Filme gelesen am: " + FastDateFormat.getInstance("dd.MM.yyyy, HH:mm").format(new Date()));
        Log.sysLog("  erstellt am: " + liste.genDate());
        Log.sysLog("  Anzahl Filme: " + liste.size());
        for (ListenerFilmeLaden l : listeners.getListeners(ListenerFilmeLaden.class))
        {
            l.fertig(new ListenerFilmeLadenEvent(url, "", max, progress, 0, false));
        }
    }

    public enum WorkMode
    {

        NORMAL, FASTAUTO
    }
}
