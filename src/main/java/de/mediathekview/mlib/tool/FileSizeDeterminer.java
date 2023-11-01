package de.mediathekview.mlib.tool;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.Optional;

import static jakarta.ws.rs.core.HttpHeaders.CONTENT_LENGTH;
import static jakarta.ws.rs.core.HttpHeaders.CONTENT_TYPE;

public class FileSizeDeterminer {
  private static final Logger LOG = LogManager.getLogger(FileSizeDeterminer.class);
  private static final int BYTE_TO_MIB = 1024;
  private static final int BYTE_TO_MB = 1000;
  private static final String PROTOCOL_RTMP = "rtmp";
  private static final String FILE_TYPE_M3U8 = "m3u8";
  private final String url;
  private final OkHttpClient client;
  //
  private Optional<Long> fileSizeInByte = Optional.empty();
  private Optional<String> responsePath = Optional.empty();
  private Optional<String> responseContentType = Optional.empty();
  
  
  /*
   * get the file size of the url in byte
   */
  public long getFileSizeInByte() {
    if (fileSizeInByte.isEmpty()) {
      getFileSizeForBuilder();
    }
    return fileSizeInByte.orElse(-1L);
  }
  
  /*
   * get the path of the response which may differ to request url for redirects
   */
  public String getResponsePath() {
    if (responsePath.isEmpty()) {
      getFileSizeInByte();
    }
    return responsePath.orElse("");
  }
  
  /*
   * get the content type of the reponse message
   */
  
  public String getResponseContentType() {
    if (responseContentType.isEmpty()) {
      getFileSizeInByte();
    }
    return responseContentType.orElse("");
  }
  
  
  
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
  public Long getFileSizeForBuilder() {
    return getFileSizeByRequest(RequestType.HEAD);
  }

  private Long getFileSizeByRequest(final RequestType requestType) {
    // Cant determine the file size of rtmp and m3u8.
    if (!url.startsWith(PROTOCOL_RTMP) && !url.endsWith(FILE_TYPE_M3U8)) {
      try (final Response response =
          client.newCall(createRequestBuilderForRequestType(requestType).build()).execute()) {
        final String contentLengthHeader = response.header(CONTENT_LENGTH);
        responseContentType = Optional.of(response.header(CONTENT_TYPE, ""));
        responsePath = Optional.of(response.request().url().encodedPath());
        fileSizeInByte = Optional.of(parseContentLength(contentLengthHeader));
        return fileSizeInByte.get();
      } catch (final IOException ioException) {
        LOG.error(
            "Something went wrong determining the file size of \"{}\" with {} request.",
            url,
            requestType);
        if (requestType.equals(RequestType.HEAD)) {
          LOG.info("Retrying the file size determination with GET request.");
          return getFileSizeByRequest(RequestType.GET);
        }
      }
    }
    return -1L;
  }

  @NotNull
  private Request.Builder createRequestBuilderForRequestType(final RequestType requestType) {
    final Request.Builder requestBuilder;
    switch (requestType) {
      case GET:
        requestBuilder = new Request.Builder().url(url).get();
        break;
      case HEAD:
        requestBuilder = new Request.Builder().url(url).head();
        break;
      default:
        throw new IllegalStateException("Unsupported request type for determining the file size.");
    }
    return requestBuilder;
  }

  /** @return The file size in MiB. */
  public Long getFileSizeInMiB() {
    return getFileSizeForBuilder() / BYTE_TO_MIB;
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

  /** @return The file size in MB. */
  public Long getFileSizeInMB() {
    return getFileSizeForBuilder() / BYTE_TO_MB;
  }

  private enum RequestType {
    GET,
    HEAD
  }
}
