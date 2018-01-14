package de.mediathekview.mlib.daten;

import java.net.URL;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;
import java.util.UUID;

/**
 * Represents a found film.
 */
public class Film extends Podcast {
  private static final long serialVersionUID = -7834270191129532291L;
  private final Collection<URL> subtitles;

  public Film(final UUID aUuid, final Sender aSender, final String aTitel, final String aThema,
      final LocalDateTime aTime, final Duration aDauer) {
    super(aUuid, aSender, aTitel, aThema, aTime, aDauer);
    subtitles = new ArrayList<>();
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
