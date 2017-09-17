package de.mediathekview.mlib.daten;

import java.net.URL;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import de.mediathekview.mlib.Const;
import de.mediathekview.mlib.tool.Functions;

/**
 * Represents a found film.
 */
public class Film
{
    private static final String[] GERMAN_GEOBLOCKING_TEXTS =
    { "+++ Aus rechtlichen Gründen ist der Film nur innerhalb von Deutschland abrufbar. +++",
            "+++ Aus rechtlichen Gründen ist diese Sendung nur innerhalb von Deutschland abrufbar. +++",
            "+++ Aus rechtlichen Gründen ist dieses Video nur innerhalb von Deutschland abrufbar. +++",
            "+++ Aus rechtlichen Gründen ist dieses Video nur innerhalb von Deutschland verfügbar. +++",
            "+++ Aus rechtlichen Gründen kann das Video nur innerhalb von Deutschland abgerufen werden. +++ Due to legal reasons the video is only available in Germany.+++",
            "+++ Aus rechtlichen Gründen kann das Video nur innerhalb von Deutschland abgerufen werden. +++",
            "+++ Due to legal reasons the video is only available in Germany.+++",
            "+++ Aus rechtlichen Gründen kann das Video nur in Deutschland abgerufen werden. +++" };

    private final UUID uuid;// Old: filmNr
    private final Map<Qualities, FilmUrl> urls;
    private final Collection<GeoLocations> geoLocations;
    private final Sender sender;
    private String titel;
    private String thema;
    private final LocalDateTime time;
    private final Duration duration;
    private boolean neu;

    private final Collection<URL> subtitles;
    private String beschreibung;
    private final URL website;

    public Film(final UUID aUuid, final Collection<GeoLocations> aGeoLocations, final Sender aSender,
            final String aTitel, final String aThema, final LocalDateTime aTime, final Duration aDuration,
            final URL aWebsite)
    {
        urls = new HashMap<>();
        subtitles = new ArrayList<>();
        geoLocations = new ArrayList<>();

        uuid = aUuid;
        sender = aSender;
        setTitel(aTitel);
        setThema(aThema);
        time = aTime;
        duration = aDuration;
        website = aWebsite;
        neu = false;

        beschreibung = "";
    }

    public UUID getUuid()
    {
        return uuid;
    }

    public Map<Qualities, FilmUrl> getUrls()
    {
        return new HashMap<>(urls);
    }

    public Sender getSender()
    {
        return sender;
    }

    public String getSenderName()
    {
        return sender.getName();
    }

    public void setTitel(final String aTitel)
    {
        titel = Functions.unescape(Functions.removeHtml(aTitel));
    }

    public String getTitel()
    {
        return titel;
    }

    public String getThema()
    {
        return thema;
    }

    public void setThema(final String aThema)
    {
        thema = Functions.unescape(Functions.removeHtml(aThema));
    }

    public Collection<GeoLocations> getGeoLocations()
    {
        return new ArrayList<>(geoLocations);
    }

    public LocalDateTime getTime()
    {
        return time;
    }

    public Duration getDuration()
    {
        return duration;
    }

    public Collection<URL> getSubtitles()
    {
        return new ArrayList<>(subtitles);
    }

    public String getBeschreibung()
    {
        return beschreibung;
    }

    public void setBeschreibung(final String aBeschreibung)
    {
        // die Beschreibung auf x Zeichen beschränken
        String beschreibung = aBeschreibung;
        beschreibung = Functions.unescape(Functions.removeHtml(beschreibung)); // damit
                                                                               // die
                                                                               // Beschreibung
                                                                               // nicht
                                                                               // unnötig
                                                                               // kurz
                                                                               // wird
                                                                               // wenn
                                                                               // es
                                                                               // erst
                                                                               // später
                                                                               // gemacht
                                                                               // wird

        for (final String geoBlockingText : GERMAN_GEOBLOCKING_TEXTS)
        {
            if (beschreibung.contains(geoBlockingText))
            {
                beschreibung = beschreibung.replace(geoBlockingText, ""); // steht
                                                                          // auch
                                                                          // mal
                                                                          // in
                                                                          // der
                                                                          // Mitte
            }
        }
        if (beschreibung.startsWith(titel))
        {
            beschreibung = beschreibung.substring(titel.length()).trim();
        }
        if (beschreibung.startsWith(thema))
        {
            beschreibung = beschreibung.substring(thema.length()).trim();
        }
        if (beschreibung.startsWith("|"))
        {
            beschreibung = beschreibung.substring(1).trim();
        }
        if (beschreibung.startsWith("Video-Clip"))
        {
            beschreibung = beschreibung.substring("Video-Clip".length()).trim();
        }
        if (beschreibung.startsWith(titel))
        {
            beschreibung = beschreibung.substring(titel.length()).trim();
        }
        if (beschreibung.startsWith(":"))
        {
            beschreibung = beschreibung.substring(1).trim();
        }
        if (beschreibung.startsWith(","))
        {
            beschreibung = beschreibung.substring(1).trim();
        }
        if (beschreibung.startsWith("\n"))
        {
            beschreibung = beschreibung.substring(1).trim();
        }
        if (beschreibung.contains("\\\""))
        { // wegen " in json-Files
            beschreibung = beschreibung.replace("\\\"", "\"");
        }
        if (beschreibung.length() > Const.MAX_BESCHREIBUNG)
        {
            beschreibung = beschreibung.substring(0, Const.MAX_BESCHREIBUNG) + "\n.....";
        }

        this.beschreibung = beschreibung;
    }

    public URL getWebsite()
    {
        return website;
    }

    public void addUrl(final Qualities aQuality, final FilmUrl aUrl)
    {
        if (aQuality != null && aUrl != null)
        {

            urls.put(aQuality, aUrl);
        }
    }

    public void addSubtitle(final URL aSubtitleUrl)
    {
        if (aSubtitleUrl != null)
        {
            subtitles.add(aSubtitleUrl);
        }
    }

    public URL getUrl(final Qualities aQuality)
    {
        return urls.get(aQuality).getUrl();
    }

    public Long getFileSize(final Qualities aQuality)
    {
        if (urls.containsKey(aQuality))
        {
            return urls.get(aQuality).getFileSize();
        }
        else
        {
            return 0l;
        }
    }

    public String getIndexName()
    {
        return new StringBuilder(titel == null ? "" : titel).append(thema == null ? "" : thema)
                .append(urls.isEmpty() ? "" : urls.get(Qualities.NORMAL)).toString();
    }

    public boolean hasHD()
    {
        return urls.containsKey(Qualities.HD);
    }

    public boolean hasUT()
    {
        return !subtitles.isEmpty();
    }

    public boolean isNeu()
    {
        return neu;
    }

    public void setNeu(final boolean aNeu)
    {
        neu = aNeu;
    }

    @Override
    public boolean equals(final Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (o == null || getClass() != o.getClass())
        {
            return false;
        }

        final Film film = (Film) o;

        if (!getUrls().equals(film.getUrls()))
        {
            return false;
        }
        if (getGeoLocations() != null ? !getGeoLocations().equals(film.getGeoLocations())
                : film.getGeoLocations() != null)
        {
            return false;
        }
        if (getSender() != film.getSender())
        {
            return false;
        }
        if (!getTitel().equals(film.getTitel()))
        {
            return false;
        }
        if (!getThema().equals(film.getThema()))
        {
            return false;
        }
        if (getDuration() != null ? !getDuration().equals(film.getDuration()) : film.getDuration() != null)
        {
            return false;
        }
        return getSubtitles() != null ? getSubtitles().equals(film.getSubtitles()) : film.getSubtitles() == null;
    }

    @Override
    public int hashCode()
    {
        int result = getUrls().hashCode();
        result = 31 * result + (getGeoLocations() != null ? getGeoLocations().hashCode() : 0);
        result = 31 * result + getSender().hashCode();
        result = 31 * result + getTitel().hashCode();
        result = 31 * result + getThema().hashCode();
        result = 31 * result + (getDuration() != null ? getDuration().hashCode() : 0);
        result = 31 * result + (getSubtitles() != null ? getSubtitles().hashCode() : 0);
        return result;
    }

    @Override
    public String toString()
    {
        return "Film{" + "uuid=" + uuid + ", urls=" + urls + ", geoLocation=" + geoLocations + ", sender=" + sender
                + ", titel='" + titel + '\'' + ", thema='" + thema + '\'' + ", time=" + time + ", duration=" + duration
                + ", subtitles=" + subtitles + ", beschreibung='" + beschreibung + '\'' + ", website=" + website
                + ", neu=" + neu + '}';
    }
}
