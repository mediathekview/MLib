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

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (!super.equals(obj)) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final Film other = (Film) obj;
    if (subtitles == null) {
      if (other.subtitles != null) {
        return false;
      }
    } else if (!subtitles.equals(other.subtitles)) {
      return false;
    }
    return true;
  }

  public Collection<URL> getSubtitles() {
    return new ArrayList<>(subtitles);
  }


  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result + (subtitles == null ? 0 : subtitles.hashCode());
    return result;
  }


  public boolean hasUT() {
    return !subtitles.isEmpty();
  }


  @Override
  public String toString() {
    return "Film [subtitles=" + subtitles + "]";
  }
}
