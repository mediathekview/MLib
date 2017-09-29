package de.mediathekview.mlib.daten;

import java.net.URL;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.UUID;

public class Livestream extends AbstractMediaResource<URL> {


  public Livestream(final UUID aUuid, final Collection<GeoLocations> aGeoLocations,
      final Sender aSender, final String aTitel, final String aThema, final LocalDateTime aTime,
      final URL aWebsite) {
    super(aUuid, aGeoLocations, aSender, aTitel, aThema, aTime, aWebsite);

  }

}
