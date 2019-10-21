package de.mediathekview.mlib.daten;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatExceptionOfType;

import java.net.MalformedURLException;
import java.net.URL;
import org.junit.jupiter.api.Test;

class FilmUrlTest {

  @Test
  void testStringConstructorWithValidHTTPSUrl() throws Exception {
    // given
    String url = "https://sha512.badssl.com/";
    URL expectedResult = new URL("https://sha512.badssl.com/");

    // when
    FilmUrl classUnderTest = new FilmUrl(url);

    // then
    assertThat(classUnderTest.getUrl()).isEqualTo(expectedResult);
    assertThat(classUnderTest.getFileSize()).isEqualTo(0);

  }

  @Test
  void testStringConstructorWithValidHTTPUrl() throws Exception {
    // given
    String url = "http://google.com/alias.m3u8";
    URL expectedResult = new URL("http://google.com/alias.m3u8");

    // when
    FilmUrl classUnderTest = new FilmUrl(url);

    // then
    assertThat(classUnderTest.getUrl()).isEqualTo(expectedResult);
    assertThat(classUnderTest.getFileSize()).isEqualTo(0);

  }

  @Test
  void testStringConsturctorWithValidJavaProtocol() throws Exception {
    // given
    String url = "tcp://127.0.0.1:80";

    // when, then
    assertThatExceptionOfType(MalformedURLException.class)
        .isThrownBy(() -> {
          FilmUrl classUnderTest = new FilmUrl(url);
        })
        .withMessage("unknown protocol: tcp")
        .withNoCause();

  }

  @Test
  void testURLConstructorWithValidJavaProtocol() throws Exception {
    // given
    URL url = new URL("file://myfile.m3u8");

    // when
    FilmUrl classUnderTest = new FilmUrl(url);

    // then
    assertThat(classUnderTest.getUrl()).isEqualTo(url);

  }

}