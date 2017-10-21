package de.mediathekview.mlib.daten;

import java.net.URL;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;

/**
 * Represents a found film.
 */
public class Film extends Podcast {
  private static final long serialVersionUID = 5765342968912286643L;
  private final Collection<URL> subtitles;

  public Film(final UUID aUuid, final Collection<GeoLocations> aGeoLocations, final Sender aSender,
      final String aTitel, final String aThema, final LocalDateTime aTime, final Duration aDauer,
      final URL aWebsite) {
    super(aUuid, aGeoLocations, aSender, aTitel, aThema, aTime, aDauer, aWebsite);
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


  @Override
  public String toString() {
    return "Film [subtitles=" + subtitles + "]";
  }
}
