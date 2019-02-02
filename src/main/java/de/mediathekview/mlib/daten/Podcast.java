package de.mediathekview.mlib.daten;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;

public class Podcast extends AbstractMediaResource<FilmUrl> {
	private static final long serialVersionUID = -7161315980975471103L;
	private final Duration duration;
	private boolean neu;

	public Podcast(final UUID aUuid, final Sender aSender, final String aTitel, final String aThema,
			final LocalDateTime aTime, final Duration aDauer) {
		super(aUuid, aSender, aTitel, aThema, aTime);
		duration = aDauer;
		neu = false;
	}

	public Podcast(Podcast copyObj) {
		super(copyObj);
		duration = copyObj.duration;
		neu = copyObj.neu;
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
		final Podcast other = (Podcast) obj;
		if (duration == null) {
			if (other.duration != null) {
				return false;
			}
		} else if (!duration.equals(other.duration)) {
			return false;
		}
		return true;
	}

	public Duration getDuration() {
		return duration;
	}

	public Long getFileSize(final Resolution aQuality) {
		if (urls.containsKey(aQuality)) {
			return urls.get(aQuality).getFileSize();
		} else {
			return 0l;
		}
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + (duration == null ? 0 : duration.hashCode());
		return result;
	}

	public boolean isNeu() {
		return neu;
	}

	public void setNeu(final boolean aNeu) {
		neu = aNeu;
	}

	@Override
	public String toString() {
		return "Podcast [duration=" + duration + ", neu=" + neu + "]";
	}

}
