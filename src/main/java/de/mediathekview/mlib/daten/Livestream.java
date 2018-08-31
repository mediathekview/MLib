package de.mediathekview.mlib.daten;

import java.time.LocalDateTime;
import java.util.UUID;

public class Livestream extends AbstractMediaResource<MediaUrl> {
  private static final long serialVersionUID = 6510203888335220851L;

  public Livestream(final UUID aUuid, final Sender aSender, final String aTitel,
      final String aThema, final LocalDateTime aTime) {
    super(aUuid, aSender, aTitel, aThema, aTime);
  }

}
