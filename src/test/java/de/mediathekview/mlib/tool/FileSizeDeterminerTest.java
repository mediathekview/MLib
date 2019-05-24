package de.mediathekview.mlib.tool;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import javax.ws.rs.core.HttpHeaders;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class FileSizeDeterminerTest {
  public static final String MOCK_URL_BASE = "http://localhost:8589";
  private static final String TEST_FILE_NAME = "FileSizeDeterminerTest.txt";
  private static final String TEST_FILE_URL = MOCK_URL_BASE + "/" + TEST_FILE_NAME;
  @Rule public WireMockRule wireMockRule = new WireMockRule(8589);

  @Before
  public void setUpWiremock() {
    wireMockRule.stubFor(
        head(urlEqualTo("/" + TEST_FILE_NAME))
            .willReturn(
                aResponse().withStatus(200).withHeader(HttpHeaders.CONTENT_LENGTH, "5643")));
  }

  @Test
  public void testGetFileSize() {
    assertThat(new FileSizeDeterminer(TEST_FILE_URL).getFileSize(), is(5643l));
  }

  @Test
  public void testGetFileSizeMiB() {
    assertThat(new FileSizeDeterminer(TEST_FILE_URL).getFileSizeInMiB(), is(5l));
  }

  @Test
  public void testGetFileSizeMB() {
    assertThat(new FileSizeDeterminer(TEST_FILE_URL).getFileSizeInMB(), is(5l));
  }
}
