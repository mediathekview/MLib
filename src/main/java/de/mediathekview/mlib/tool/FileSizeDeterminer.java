package de.mediathekview.mlib.tool;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import javax.ws.rs.core.HttpHeaders;
import java.io.IOException;

public class FileSizeDeterminer {
  private static final Logger LOG = LogManager.getLogger(FileSizeDeterminer.class);
  private static final int BYTE_TO_MiB = 1024;
  private static final int BYTE_TO_MB = 1000;
  private static final String PROTOCOL_RTMP = "rtmp";
  private final String url;
  private final OkHttpClient client;

  /**
   * Builds the determiner with the default read- and connect timeout of 30 seconds.
   *
   * @param aUrl The url of the file.
   */
  public FileSizeDeterminer(final String aUrl) {
    this(aUrl, 30L, 30L);
  }

  /**
   * @param aUrl The url of the file.
   * @param connectTimeoutInSeconds The connection timeout in seconds.
   * @param readTimeoutInSeconds The read timeout in seconds.
   */
  public FileSizeDeterminer(
      final String aUrl, final long connectTimeoutInSeconds, final long readTimeoutInSeconds) {
    url = aUrl;
    client =
        new OkHttpClientBuilder()
            .withConnectTimeout(connectTimeoutInSeconds)
            .withReadTimeout(readTimeoutInSeconds)
            .build();
  }

  /** @return The file size in bytes. */
  public Long getFileSize() {
    if (url.startsWith(PROTOCOL_RTMP)) {
      // Cant determine the file size of rtmp.
      return -1L;
    }

    try {
      final Response headResponse =
          client.newCall(new Request.Builder().url(url).head().build()).execute();
      final String contentLengthHeader = headResponse.header(HttpHeaders.CONTENT_LENGTH);
      return parseContentLength(contentLengthHeader);
    } catch (final IOException ioException) {
      LOG.error("Somethin went wrong determining the file size of \"{}\"", url);
      return -1L;
    }
  }

  @NotNull
  private Long parseContentLength(final String contentLengthHeader) {
    try {
      return contentLengthHeader == null ? -1L : Long.parseLong(contentLengthHeader);
    } catch (final NumberFormatException numberFormatException) {
      LOG.error(
          "The Content-Length \"{}\" isn't a valid number.",
          contentLengthHeader,
          numberFormatException);
      return -1L;
    }
  }

  /** @return The file size in MiB. */
  public Long getFileSizeInMiB() {
    return getFileSize() / BYTE_TO_MiB;
  }

  /** @return The file size in MB. */
  public Long getFileSizeInMB() {
    return getFileSize() / BYTE_TO_MB;
  }
}
