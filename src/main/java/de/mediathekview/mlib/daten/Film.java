package de.mediathekview.mlib.daten;

import java.net.URL;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Represents a found film.
 */
public class Film extends Podcast {
	private static final long serialVersionUID = -7834270191129532291L;

	private final Map<Resolution, FilmUrl> audioDescriptions;

	private final Map<Resolution, FilmUrl> signLanguages;
	private final Set<URL> subtitles;

	public Film(final UUID aUuid, final Sender aSender, final String aTitel, final String aThema,
			final LocalDateTime aTime, final Duration aDauer) {
		super(aUuid, aSender, aTitel, aThema, aTime, aDauer);
		audioDescriptions = new EnumMap<>(Resolution.class);
		signLanguages = new EnumMap<>(Resolution.class);
		subtitles = new HashSet<>();
	}

	public Film(Film copyObj) {
		super(copyObj);
		audioDescriptions = copyObj.audioDescriptions;
		signLanguages = copyObj.signLanguages;
		subtitles = copyObj.subtitles;
	}
	
	/**
	 * DON'T USE! - ONLY FOR GSON!
	 */
	@SuppressWarnings("unused")
	private Film() {
		super();
		audioDescriptions = new EnumMap<>(Resolution.class);
		signLanguages = new EnumMap<>(Resolution.class);
		subtitles = new HashSet<>();
	}
	
	@Override
	public AbstractMediaResource<FilmUrl> merge(AbstractMediaResource<FilmUrl> objToMergeWith) {
		addAllGeoLocations(objToMergeWith.getGeoLocations());
		addAllUrls(objToMergeWith.getUrls().entrySet().stream().filter(urlEntry -> !urls.containsKey(urlEntry.getKey()) || 
				urlEntry.getValue().getFileSize() > urls.get(urlEntry.getKey()).getFileSize()
				)
				.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));
		return this;
	}
	
		public Film merge(Film objToMergeWith) {
			merge((AbstractMediaResource<FilmUrl>)objToMergeWith);
			objToMergeWith.getAudioDescriptions().forEach(audioDescriptions::putIfAbsent);
			objToMergeWith.getSignLanguages().forEach(signLanguages::putIfAbsent);
			subtitles.addAll(objToMergeWith.getSubtitles());
			return this;
		}

	public void addAllSubtitleUrls(Set<URL> urlsToAdd) {
		this.subtitles.addAll(urlsToAdd);
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
		return new HashMap<>(audioDescriptions);
	}

	public FilmUrl getSignLanguage(final Resolution aQuality) {
		return signLanguages.get(aQuality);
	}

	public Map<Resolution, FilmUrl> getSignLanguages() {
		return new HashMap<>(signLanguages);
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
