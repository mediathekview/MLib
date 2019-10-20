package de.mediathekview.mlib.daten;

import java.net.URL;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/** Represents a found film. */
public class Film extends Podcast {
  private static final long serialVersionUID = -7834270191129532291L;

  private final Map<Resolution, FilmUrl> audioDescriptions;

  private final Map<Resolution, FilmUrl> signLanguages;
  private final Set<URL> subtitles;

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
  private Film() {
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
    return new EnumMap<>(audioDescriptions);
  }

  public FilmUrl getSignLanguage(final Resolution aQuality) {
    return signLanguages.get(aQuality);
  }

  public Map<Resolution, FilmUrl> getSignLanguages() {
    return new EnumMap<>(signLanguages);
  }

  public Collection<URL> getSubtitles() {
    return new ArrayList<>(subtitles);
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
        + '}';
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
