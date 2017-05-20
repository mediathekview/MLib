package de.mediathekview.mlib.daten;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.URI;
import java.net.URISyntaxException;

public class FilmUrl
{
    private static final Logger LOG = LogManager.getLogger(FilmUrl.class);
    private static final String URL_START_NRODL = "//nrodl";
    private static final String URL_START_RODL = "//rodl";
    private static final String URL_HTTPS = "https";
    private static final String URL_HTTP = "http";
    private URI url;
    private Long fileSize;

    public FilmUrl(final URI url, final Long fileSize)
    {
        this.url = url;
        this.fileSize = fileSize;
    }

    public URI getUrl()
    {
        return url;
    }

    public void setUrl(final URI url)
    {
        this.url = url;
    }

    public Long getFileSize()
    {
        return fileSize;
    }

    public void setFileSize(final Long fileSize)
    {
        this.fileSize = fileSize;
    }

    @Override
    public boolean equals(final Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final FilmUrl filmUrl = (FilmUrl) o;

        if (!makeUrlComparable(getUrl()).equals(makeUrlComparable(filmUrl.getUrl()))) return false;
        return getFileSize().equals(filmUrl.getFileSize());

    }

    private URI makeUrlComparable(final URI aUrl)
    {
        URI newUrl;
        if (aUrl == null)
        {
            newUrl = aUrl;
        } else
        {
            String urlAsText = aUrl.toString();
            try
            {
                newUrl = new URI(
                        urlAsText.replaceAll(URL_START_NRODL, URL_START_RODL)
                                .replaceAll(URL_HTTPS, URL_HTTP)
                );
            } catch (URISyntaxException aURISyntaxException)
            {
                LOG.fatal("Can'T replace the nrodl in these URL: " + aUrl.toString(), aURISyntaxException);
                newUrl = aUrl;
            }
        }
        return newUrl;
    }

    @Override
    public int hashCode()
    {
        int result = makeUrlComparable(getUrl()).hashCode();
        result = 31 * result + getFileSize().hashCode();
        return result;
    }

}
