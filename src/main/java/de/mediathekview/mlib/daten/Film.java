package de.mediathekview.mlib.daten;

import java.net.URL;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Represents a found film.
 */
public class Film extends Podcast {
  private static final long serialVersionUID = -7834270191129532291L;
  private final Map<Resolution, FilmUrl> audioDescriptions;
  private final Map<Resolution, FilmUrl> signLanguages;
  private final Collection<URL> subtitles;

  public Film(final UUID aUuid, final Sender aSender, final String aTitel, final String aThema,
      final LocalDateTime aTime, final Duration aDauer) {
    super(aUuid, aSender, aTitel, aThema, aTime, aDauer);
    audioDescriptions = new EnumMap<>(Resolution.class);
    signLanguages = new EnumMap<>(Resolution.class);
    subtitles = new ArrayList<>();
  }

  public void addAudioDescription(final Resolution aQuality, final FilmUrl aUrl) {
    if (aQuality != null && aUrl != null) {
      audioDescriptions.put(aQuality, aUrl);
    }
  }

  public FilmUrl getAudioDescription(final Resolution aQuality) {
    return audioDescriptions.get(aQuality);
  }

  public void addSignLanguage(final Resolution aQuality, final FilmUrl aUrl) {
    if (aQuality != null && aUrl != null) {
      signLanguages.put(aQuality, aUrl);
    }
  }

  public FilmUrl getSignLanguage(final Resolution aQuality) {
    return signLanguages.get(aQuality);
  }
  
  public void addSubtitle(final URL aSubtitleUrl) {
    if (aSubtitleUrl != null) {
      subtitles.add(aSubtitleUrl);
    }
  }

  public Collection<URL> getSubtitles() {
    return new ArrayList<>(subtitles);
  }


  public boolean hasUT() {
    return !subtitles.isEmpty();
  }

  public void addAllSubtitleUrls(Set<URL> urlsToAdd) {
    this.subtitles.addAll(urlsToAdd);
  }
  
  @Override
  public String toString() {
    return "Film [subtitles=" + subtitles + "]";
  }
}
