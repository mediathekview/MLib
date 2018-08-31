package de.mediathekview.mlib.daten;

import java.io.Serializable;
import java.net.URL;

public class FilmUrl extends MediaUrl implements Serializable {
	private static final long serialVersionUID = -7673915328280663006L;
	private Long fileSize;

	public FilmUrl(final URL url, final Long fileSize) {
		super(url);
		this.fileSize = fileSize;
	}

	public Long getFileSize() {
		return fileSize;
	}

	public void setFileSize(final Long fileSize) {
		this.fileSize = fileSize;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((fileSize == null) ? 0 : fileSize.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (!(obj instanceof FilmUrl))
			return false;
		FilmUrl other = (FilmUrl) obj;
		if (fileSize == null) {
			if (other.fileSize != null)
				return false;
		} else if (!fileSize.equals(other.fileSize)) {
			return false;
		}
		return true;
	}

}
