package de.mediathekview.mlib.tool;

import com.github.tomakehurst.wiremock.WireMockServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static jakarta.ws.rs.core.HttpHeaders.CONTENT_LENGTH;
import static jakarta.ws.rs.core.HttpHeaders.CONTENT_TYPE;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

class FileSizeDeterminerTest {
  private static final String TEST_FILE_NAME = "FileSizeDeterminerTest.txt";
  private static final String TEST_FILE_URL = "/" + TEST_FILE_NAME;
  private static final WireMockServer wireMockServer = new WireMockServer(options().dynamicPort());

  @BeforeAll
  public static void setUpWiremock() {
    wireMockServer.stubFor(
        head(urlEqualTo("/" + TEST_FILE_NAME))
            .willReturn(
                aResponse().withStatus(200).withHeader(CONTENT_LENGTH, "5643").withHeader(CONTENT_TYPE, "text/html")));
  }

  @BeforeEach
  public void startWireMock() {
    wireMockServer.start();
  }

  @AfterEach
  public void stopWireMock() {
    wireMockServer.stop();
  }

  @Test
  void testGetFileSize() {
    assertThat(getClassUnderTest().getRequestInfo(wireMockServer.baseUrl() + TEST_FILE_URL).getSize()).isEqualTo(5643L);
  }
  
  @Test
  void testGetStatusCode() {
    assertThat(getClassUnderTest().getRequestInfo(wireMockServer.baseUrl() + TEST_FILE_URL).getCode()).isEqualTo(200);
  }
  
  @Test
  void testGetContentType() {
    assertThat(getClassUnderTest().getRequestInfo(wireMockServer.baseUrl() + TEST_FILE_URL).getContentType()).isEqualTo("text/html");
  }

  private FileSizeDeterminer getClassUnderTest() {
    return new FileSizeDeterminer();
  }
}
