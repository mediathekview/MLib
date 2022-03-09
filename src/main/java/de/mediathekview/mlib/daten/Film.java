package de.mediathekview.mlib.daten;

import java.io.Serial;
import java.net.URL;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/** Represents a found film. */
public class Film extends Podcast {
  @Serial
  private static final long serialVersionUID = -7834270191129532291L;
  private Set<URL> subtitles;
  private Map<Resolution, FilmUrl> audioDescriptions;
  private Map<Resolution, FilmUrl> signLanguages;
  public Film(
      final UUID aUuid,
      final Sender aSender,
      final String aTitel,
      final String aThema,
      final LocalDateTime aTime,
      final Duration aDauer) {
    super(aUuid, aSender, aTitel, aThema, aTime, aDauer);
    audioDescriptions = new EnumMap<>(Resolution.class);
    signLanguages = new EnumMap<>(Resolution.class);
    subtitles = new HashSet<>();
  }

  public Film(final Film copyObj) {
    super(copyObj);
    audioDescriptions = copyObj.audioDescriptions;
    signLanguages = copyObj.signLanguages;
    subtitles = copyObj.subtitles;
  }

  /** DON'T USE! - ONLY FOR GSON! */
  public Film() {
    super();
    audioDescriptions = new EnumMap<>(Resolution.class);
    signLanguages = new EnumMap<>(Resolution.class);
    subtitles = new HashSet<>();
  }

  @Override
  public AbstractMediaResource<FilmUrl> merge(final AbstractMediaResource<FilmUrl> objToMergeWith) {
    addAllGeoLocations(objToMergeWith.getGeoLocations());
    addAllUrls(
        objToMergeWith.getUrls().entrySet().stream()
            .filter(
                urlEntry ->
                    !getUrls().containsKey(urlEntry.getKey())
                        || urlEntry.getValue().getFileSize()
                            > getUrls().get(urlEntry.getKey()).getFileSize())
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));
    return this;
  }

  public Film merge(final Film objToMergeWith) {
    merge((AbstractMediaResource<FilmUrl>) objToMergeWith);
    objToMergeWith.getAudioDescriptions().forEach(audioDescriptions::putIfAbsent);
    objToMergeWith.getSignLanguages().forEach(signLanguages::putIfAbsent);
    subtitles.addAll(objToMergeWith.getSubtitles());
    return this;
  }

  public void addAllSubtitleUrls(final Set<URL> urlsToAdd) {
    subtitles.addAll(urlsToAdd);
  }

  public void addAudioDescription(final Resolution aQuality, final FilmUrl aUrl) {
    if (aQuality != null && aUrl != null) {
      audioDescriptions.put(aQuality, aUrl);
    }
  }

  public void addSignLanguage(final Resolution aQuality, final FilmUrl aUrl) {
    if (aQuality != null && aUrl != null) {
      signLanguages.put(aQuality, aUrl);
    }
  }

  public void addSubtitle(final URL aSubtitleUrl) {
    if (aSubtitleUrl != null) {
      subtitles.add(aSubtitleUrl);
    }
  }

  public FilmUrl getAudioDescription(final Resolution aQuality) {
    return audioDescriptions.get(aQuality);
  }

  public Map<Resolution, FilmUrl> getAudioDescriptions() {
    if (audioDescriptions.isEmpty()) {
      return new EnumMap<>(Resolution.class);
    }
    return new EnumMap<>(audioDescriptions);
  }

  public void setAudioDescriptions(Map<Resolution, FilmUrl> audioDescriptions) {
    this.audioDescriptions = audioDescriptions;
  }

  public FilmUrl getSignLanguage(final Resolution aQuality) {
    return signLanguages.get(aQuality);
  }

  public Map<Resolution, FilmUrl> getSignLanguages() {
    if (signLanguages.isEmpty()) {
      return new EnumMap<>(Resolution.class);
    }
    return new EnumMap<>(signLanguages);
  }

  public void setSignLanguages(Map<Resolution, FilmUrl> signLanguages) {
    this.signLanguages = signLanguages;
  }

  public Collection<URL> getSubtitles() {
    return new ArrayList<>(subtitles);
  }

  public void setSubtitles(Set<URL> subtitles) {
    this.subtitles = subtitles;
  }

  @Override
  public String toString() {
    return "Film{"
        + "audioDescriptions="
        + audioDescriptions
        + ", signLanguages="
        + signLanguages
        + ", subtitles="
        + subtitles
        + "} "
        + super.toString();
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof Film)) {
      return false;
    }
    if (!super.equals(o)) {
      return false;
    }
    return super.equals(o);
  }

  @Override
  public int hashCode() {
    return super.hashCode();
  }

  public boolean hasUT() {
    return !subtitles.isEmpty();
  }
}
