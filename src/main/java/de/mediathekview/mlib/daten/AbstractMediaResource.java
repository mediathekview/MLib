package de.mediathekview.mlib.daten;

import de.mediathekview.mlib.Const;
import de.mediathekview.mlib.tool.Functions;

import java.io.Serializable;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collectors;

public abstract class AbstractMediaResource<T> implements Serializable {
  private static final long serialVersionUID = -6404888306701549134L;
  private static final String[] GERMAN_GEOBLOCKING_TEXTS = {
    "+++ Aus rechtlichen Gründen ist der Film nur innerhalb von Deutschland abrufbar. +++",
    "+++ Aus rechtlichen Gründen ist diese Sendung nur innerhalb von Deutschland abrufbar. +++",
    "+++ Aus rechtlichen Gründen ist dieses Video nur innerhalb von Deutschland abrufbar. +++",
    "+++ Aus rechtlichen Gründen ist dieses Video nur innerhalb von Deutschland verfügbar. +++",
    "+++ Aus rechtlichen Gründen kann das Video nur innerhalb von Deutschland abgerufen werden. +++ Due to legal reasons the video is only available in Germany.+++",
    "+++ Aus rechtlichen Gründen kann das Video nur innerhalb von Deutschland abgerufen werden. +++",
    "+++ Due to legal reasons the video is only available in Germany.+++",
    "+++ Aus rechtlichen Gründen kann das Video nur in Deutschland abgerufen werden. +++"
  };
  protected final Map<Resolution, T> urls;
  private final UUID uuid; // Old: filmNr
  private final Sender sender;
  private final LocalDateTime time;
  private Collection<GeoLocations> geoLocations;
  private String titel;
  private String thema;
  private String beschreibung;
  private Optional<URL> website;

  /** DON'T USE! - ONLY FOR GSON! */
  protected AbstractMediaResource() {
    geoLocations = new ArrayList<>();
    urls = new EnumMap<>(Resolution.class);
    uuid = null;
    sender = null;
    time = null;
    website = Optional.empty();
    beschreibung = "";
  }

  public AbstractMediaResource(
      final UUID aUuid,
      final Sender aSender,
      final String aTitel,
      final String aThema,
      final LocalDateTime aTime) {
    geoLocations = new ArrayList<>();
    urls = new EnumMap<>(Resolution.class);
    uuid = aUuid;
    if (aSender == null) {
      throw new IllegalArgumentException("The sender can't be null!");
    }
    sender = aSender;
    setTitel(aTitel);
    setThema(aThema);
    time = aTime;
    website = Optional.empty();

    beschreibung = "";
  }

  public AbstractMediaResource(final AbstractMediaResource<T> copyObj) {
    super();
    uuid = copyObj.uuid;
    geoLocations = copyObj.geoLocations;
    urls = copyObj.urls;
    sender = copyObj.sender;
    titel = copyObj.titel;
    thema = copyObj.thema;
    time = copyObj.time;
    beschreibung = copyObj.beschreibung;
    website = copyObj.website;
  }

  public AbstractMediaResource<T> merge(final AbstractMediaResource<T> objToMergeWith) {
    addAllGeoLocations(objToMergeWith.getGeoLocations());
    addAllUrls(
        objToMergeWith.getUrls().entrySet().stream()
            .filter(urlEntry -> !urls.containsKey(urlEntry.getKey()))
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));
    return this;
  }

  public void addAllGeoLocations(final Collection<GeoLocations> aGeoLocations) {
    geoLocations.addAll(aGeoLocations);
  }

  public void addAllUrls(final Map<Resolution, T> urlMap) {
    urls.putAll(urlMap);
  }

  public void addGeolocation(final GeoLocations aGeoLocation) {
    geoLocations.add(aGeoLocation);
  }

  public void addUrl(final Resolution aQuality, final T aUrl) {
    if (aQuality != null && aUrl != null) {
      urls.put(aQuality, aUrl);
    }
  }

  public void addUrlIfAbsent(final Resolution aQuality, final T aUrl) {
    if (aQuality != null && aUrl != null) {
      urls.putIfAbsent(aQuality, aUrl);
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
      return other.titel == null;
    } else {
      return titel.equals(other.titel);
    }
  }

  public String getBeschreibung() {
    return beschreibung;
  }

  public void setBeschreibung(final String aBeschreibung) {
    // die Beschreibung auf x Zeichen beschränken
    String unfilteredBeschreibung = aBeschreibung;
    unfilteredBeschreibung =
        Functions.unescape(Functions.removeHtml(unfilteredBeschreibung)); // damit
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

  public Optional<T> getDefaultUrl() {
    if (urls.containsKey(Resolution.NORMAL)) {
      return Optional.of(getUrl(Resolution.NORMAL));
    }
    final Iterator<Entry<Resolution, T>> entryIterator = urls.entrySet().iterator();
    if (entryIterator.hasNext()) {
      return Optional.of(entryIterator.next().getValue());
    }
    return Optional.empty();
  }

  public Collection<GeoLocations> getGeoLocations() {
    return new ArrayList<>(geoLocations);
  }

  public void setGeoLocations(final Collection<GeoLocations> aGeoLocations) {
    geoLocations = aGeoLocations;
  }

  public String getIndexName() {
    return new StringBuilder(titel == null ? "" : titel)
        .append(thema == null ? "" : thema)
        .append(urls.isEmpty() ? "" : urls.get(Resolution.NORMAL))
        .toString();
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

  public void setThema(final String aThema) {
    thema = Functions.unescape(Functions.removeHtml(aThema));
  }

  public LocalDateTime getTime() {
    return time;
  }

  public String getTitel() {
    return titel;
  }

  public void setTitel(final String aTitel) {
    titel = Functions.unescape(Functions.removeHtml(aTitel));
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

  public Optional<URL> getWebsite() {
    return website;
  }

  public void setWebsite(final Optional<URL> aWebsite) {
    website = aWebsite;
  }

  public void setWebsite(final URL aWebsite) {
    website = Optional.of(aWebsite);
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

  @Override
  public String toString() {
    return "AbstractMediaResource [uuid="
        + uuid
        + ", geoLocations="
        + geoLocations
        + ", sender="
        + sender
        + ", titel="
        + titel
        + ", thema="
        + thema
        + ", time="
        + time
        + ", beschreibung="
        + beschreibung
        + ", website="
        + website
        + "]";
  }
}
