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
  private static final String TEST_FILE_NAME = "FileSizeDeterminerTest.txt";
  private static final String TEST_FILE_URL = "/" + TEST_FILE_NAME;
  private static WireMockServer wireMockServer = new WireMockServer(options().dynamicPort());

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
    assertThat(getClassUnderTest().getFileSizeForBuilder()).isEqualTo(5643L);
  }

  @Test
  public void testGetFileSizeMiB() {
    assertThat(getClassUnderTest().getFileSizeInMiB()).isEqualTo(5L);
  }

  @Test
  public void testGetFileSizeMB() {
    assertThat(getClassUnderTest().getFileSizeInMB()).isEqualTo(5L);
  }

  private FileSizeDeterminer getClassUnderTest() {
    return new FileSizeDeterminer(wireMockServer.baseUrl() + TEST_FILE_URL);
  }


}
