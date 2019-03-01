package de.mediathekview.mlib.daten;

import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Objects;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class FilmUrl implements Serializable {
	private static final long serialVersionUID = 576534421232286643L;
	private static final Logger LOG = LogManager.getLogger(FilmUrl.class);
	private static final String URL_START_NRODL = "//nrodl";
	private static final String URL_START_RODL = "//rodl";
	private static final String URL_HTTPS = "https";
	private static final String URL_HTTP = "http";
	private URL url;
	private Long fileSize;

	public FilmUrl(final URL url, final Long fileSize) {
		this.url = url;
		this.fileSize = fileSize;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof FilmUrl)) {
			return false;
		}
		FilmUrl other = (FilmUrl) obj;
		return Objects.equals(fileSize, other.fileSize)
				&& Objects.equals(makeUrlComparable(url), makeUrlComparable(other.url));
	}

	public Long getFileSize() {
		return fileSize;
	}

	public URL getUrl() {
		return url;
	}

	@Override
	public int hashCode() {
		return Objects.hash(fileSize, makeUrlComparable(url));
	}

	public void setFileSize(final Long fileSize) {
		this.fileSize = fileSize;
	}

	public void setUrl(final URL url) {
		this.url = url;
	}

	private URL makeUrlComparable(final URL aUrl) {
		URL newUrl;
		if (aUrl == null) {
			newUrl = aUrl;
		} else {
			final String urlAsText = aUrl.toString();
			try {
				newUrl = new URL(urlAsText.replaceAll(URL_START_NRODL, URL_START_RODL).replaceAll(URL_HTTPS, URL_HTTP));
			} catch (final MalformedURLException aMalformedURLException) {
				LOG.fatal("Can't replace the nrodl in these URL: " + aUrl.toString(), aMalformedURLException);
				newUrl = aUrl;
			}
		}
		return newUrl;
	}

	public String toString() {
		return getUrl().toString();
	}

}
