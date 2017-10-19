package de.mediathekview.mlib.daten;

import java.net.URL;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.Map;
import java.util.UUID;
import de.mediathekview.mlib.Const;
import de.mediathekview.mlib.tool.Functions;

public abstract class AbstractMediaResource<T> {
  private static final String[] GERMAN_GEOBLOCKING_TEXTS = {
      "+++ Aus rechtlichen Gründen ist der Film nur innerhalb von Deutschland abrufbar. +++",
      "+++ Aus rechtlichen Gründen ist diese Sendung nur innerhalb von Deutschland abrufbar. +++",
      "+++ Aus rechtlichen Gründen ist dieses Video nur innerhalb von Deutschland abrufbar. +++",
      "+++ Aus rechtlichen Gründen ist dieses Video nur innerhalb von Deutschland verfügbar. +++",
      "+++ Aus rechtlichen Gründen kann das Video nur innerhalb von Deutschland abgerufen werden. +++ Due to legal reasons the video is only available in Germany.+++",
      "+++ Aus rechtlichen Gründen kann das Video nur innerhalb von Deutschland abgerufen werden. +++",
      "+++ Due to legal reasons the video is only available in Germany.+++",
      "+++ Aus rechtlichen Gründen kann das Video nur in Deutschland abgerufen werden. +++"};
  private final UUID uuid;// Old: filmNr
  private final Collection<GeoLocations> geoLocations;
  protected final Map<Resolution, T> urls;
  private final Sender sender;
  private String titel;
  private String thema;
  private final LocalDateTime time;
  private String beschreibung;
  private final URL website;

  public AbstractMediaResource(final UUID aUuid, final Collection<GeoLocations> aGeoLocations,
      final Sender aSender, final String aTitel, final String aThema, final LocalDateTime aTime,
      final URL aWebsite) {
    geoLocations = new ArrayList<>(aGeoLocations);
    urls = new EnumMap<>(Resolution.class);
    uuid = aUuid;
    sender = aSender;
    setTitel(aTitel);
    setThema(aThema);
    time = aTime;
    website = aWebsite;

    beschreibung = "";
  }

  public void addUrl(final Resolution aQuality, final T aUrl) {
    if (aQuality != null && aUrl != null) {
      urls.put(aQuality, aUrl);
    }
  }


  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final AbstractMediaResource other = (AbstractMediaResource) obj;
    if (sender != other.sender) {
      return false;
    }
    if (thema == null) {
      if (other.thema != null) {
        return false;
      }
    } else if (!thema.equals(other.thema)) {
      return false;
    }
    if (titel == null) {
      if (other.titel != null) {
        return false;
      }
    } else if (!titel.equals(other.titel)) {
      return false;
    }
    return true;
  }

  public String getBeschreibung() {
    return beschreibung;
  }

  public Collection<GeoLocations> getGeoLocations() {
    return new ArrayList<>(geoLocations);
  }

  public String getIndexName() {
    return new StringBuilder(titel == null ? "" : titel).append(thema == null ? "" : thema)
        .append(urls.isEmpty() ? "" : urls.get(Resolution.NORMAL)).toString();
  }

  public Sender getSender() {
    return sender;
  }

  public String getSenderName() {
    return sender.getName();
  }

  public String getThema() {
    return thema;
  }

  public LocalDateTime getTime() {
    return time;
  }

  public String getTitel() {
    return titel;
  }

  public T getUrl(final Resolution aQuality) {
    return urls.get(aQuality);
  }



  public Map<Resolution, T> getUrls() {
    return new EnumMap<>(urls);
  }

  public UUID getUuid() {
    return uuid;
  }



  public URL getWebsite() {
    return website;
  }


  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + (sender == null ? 0 : sender.hashCode());
    result = prime * result + (thema == null ? 0 : thema.hashCode());
    result = prime * result + (titel == null ? 0 : titel.hashCode());
    return result;
  }

  public boolean hasHD() {
    return urls.containsKey(Resolution.HD);
  }

  public void setBeschreibung(final String aBeschreibung) {
    // die Beschreibung auf x Zeichen beschränken
    String unfilteredBeschreibung = aBeschreibung;
    unfilteredBeschreibung = Functions.unescape(Functions.removeHtml(unfilteredBeschreibung)); // damit
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

    for (final String geoBlockingText : GERMAN_GEOBLOCKING_TEXTS) {
      if (unfilteredBeschreibung.contains(geoBlockingText)) {
        unfilteredBeschreibung = unfilteredBeschreibung.replace(geoBlockingText, ""); // steht
        // auch
        // mal
        // in
        // der
        // Mitte
      }
    }
    if (unfilteredBeschreibung.startsWith(titel)) {
      unfilteredBeschreibung = unfilteredBeschreibung.substring(titel.length()).trim();
    }
    if (unfilteredBeschreibung.startsWith(thema)) {
      unfilteredBeschreibung = unfilteredBeschreibung.substring(thema.length()).trim();
    }
    if (unfilteredBeschreibung.startsWith("|")) {
      unfilteredBeschreibung = unfilteredBeschreibung.substring(1).trim();
    }
    if (unfilteredBeschreibung.startsWith("Video-Clip")) {
      unfilteredBeschreibung = unfilteredBeschreibung.substring("Video-Clip".length()).trim();
    }
    if (unfilteredBeschreibung.startsWith(titel)) {
      unfilteredBeschreibung = unfilteredBeschreibung.substring(titel.length()).trim();
    }
    if (unfilteredBeschreibung.startsWith(":")) {
      unfilteredBeschreibung = unfilteredBeschreibung.substring(1).trim();
    }
    if (unfilteredBeschreibung.startsWith(",")) {
      unfilteredBeschreibung = unfilteredBeschreibung.substring(1).trim();
    }
    if (unfilteredBeschreibung.startsWith("\n")) {
      unfilteredBeschreibung = unfilteredBeschreibung.substring(1).trim();
    }
    if (unfilteredBeschreibung.contains("\\\"")) { // wegen " in json-Files
      unfilteredBeschreibung = unfilteredBeschreibung.replace("\\\"", "\"");
    }
    if (unfilteredBeschreibung.length() > Const.MAX_BESCHREIBUNG) {
      unfilteredBeschreibung =
          unfilteredBeschreibung.substring(0, Const.MAX_BESCHREIBUNG) + "\n.....";
    }

    beschreibung = unfilteredBeschreibung;
  }

  public void setThema(final String aThema) {
    thema = Functions.unescape(Functions.removeHtml(aThema));
  }

  public void setTitel(final String aTitel) {
    titel = Functions.unescape(Functions.removeHtml(aTitel));
  }

  @Override
  public String toString() {
    return "AbstractMediaResource [uuid=" + uuid + ", geoLocations=" + geoLocations + ", sender="
        + sender + ", titel=" + titel + ", thema=" + thema + ", time=" + time + ", beschreibung="
        + beschreibung + ", website=" + website + "]";
  }

}
