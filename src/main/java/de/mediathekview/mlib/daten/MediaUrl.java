package de.mediathekview.mlib.daten;

import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class MediaUrl implements Serializable {
	private static final long serialVersionUID = 1854501394228477489L;
	private static final Logger LOG = LogManager.getLogger(MediaUrl.class);
	private static final String URL_START_NRODL = "//nrodl";
	private static final String URL_START_RODL = "//rodl";
	private static final String URL_HTTPS = "https";
	private static final String URL_HTTP = "http";
	private String url;

	public MediaUrl(final URL url) {
		this.url = url.toString();
	}


	public URL getUrl() {
		try {
			return new URL(url);
		} catch (MalformedURLException malformedURLException) {
			LOG.fatal("The URL can't converted to a URL object. This should never happen.", malformedURLException);
			throw new IllegalStateException();
		}
	}

	public void setUrl(final URL url) {
		this.url = url.toString();
	}

	private String makeUrlComparable(final String aUrl) {
		String newUrl;
		if (aUrl == null) {
			newUrl = aUrl;
		} else {
			newUrl = aUrl.replaceAll(URL_START_NRODL, URL_START_RODL).replaceAll(URL_HTTPS, URL_HTTP);
		}
		return newUrl;
	}

	public String toString() {
		return getUrl().toString();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((url == null) ? 0 : makeUrlComparable(url).hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof MediaUrl))
			return false;
		MediaUrl other = (MediaUrl) obj;
		if (url == null) {
			if (other.url != null)
				return false;
		} else if (!makeUrlComparable(url).equals(makeUrlComparable(other.url))) {
			return false;
		}
		return true;
	}

}
