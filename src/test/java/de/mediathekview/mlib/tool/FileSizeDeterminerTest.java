package de.mediathekview.mlib.tool;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.head;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

import com.github.tomakehurst.wiremock.WireMockServer;
import javax.ws.rs.core.HttpHeaders;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class FileSizeDeterminerTest {
  public static final String MOCK_URL_BASE = "http://localhost:8589";
  private static final String TEST_FILE_NAME = "FileSizeDeterminerTest.txt";
  private static final String TEST_FILE_URL = MOCK_URL_BASE + "/" + TEST_FILE_NAME;
  private static WireMockServer wireMockServer = new WireMockServer(options().port(8589));

  @BeforeEach
  public void startWireMock() {
    wireMockServer.start();
  }

  @AfterEach
  public void stopWireMock() {
    wireMockServer.stop();
  }

  @BeforeAll
  public static void setUpWiremock() {
    wireMockServer.stubFor(
        head(urlEqualTo("/" + TEST_FILE_NAME))
            .willReturn(
                aResponse().withStatus(200).withHeader(HttpHeaders.CONTENT_LENGTH, "5643")));
  }

  @Test
  public void testGetFileSize() {
    assertThat(new FileSizeDeterminer(TEST_FILE_URL).getFileSizeForBuilder()).isEqualTo(5643l);
  }

  @Test
  public void testGetFileSizeMiB() {
    assertThat(new FileSizeDeterminer(TEST_FILE_URL).getFileSizeInMiB()).isEqualTo(5l);
  }

  @Test
  public void testGetFileSizeMB() {
    assertThat(new FileSizeDeterminer(TEST_FILE_URL).getFileSizeInMB()).isEqualTo(5l);
  }
}
