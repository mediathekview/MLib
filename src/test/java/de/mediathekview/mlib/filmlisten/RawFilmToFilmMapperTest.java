package de.mediathekview.mlib.filmlisten;

import de.mediathekview.mlib.daten.*;
import de.mediathekview.mlib.filmlisten.reader.RawFilm;
import de.mediathekview.mlib.filmlisten.reader.RawFilmToFilmException;
import de.mediathekview.mlib.filmlisten.reader.RawFilmToFilmMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.mapstruct.factory.Mappers;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

class RawFilmToFilmMapperTest {
  private final RawFilmToFilmMapper classUnderTest = Mappers.getMapper(RawFilmToFilmMapper.class);

  private static Stream<Arguments> mapSubtitleUrlArguments() throws MalformedURLException {
    return Stream.of(
        Arguments.of(null, new ArrayList<>()),
        Arguments.of("", new ArrayList<>()),
        Arguments.of("invalidUrl", new ArrayList<>()),
        Arguments.of("https://www.google.com/", List.of(new URL("https://www.google.com/"))));
  }

  private static Stream<Arguments> gatherNormalUrlArguments() throws MalformedURLException {
    return Stream.of(
        Arguments.of(null, null),
        Arguments.of("", null),
        Arguments.of("invalidUrl", null),
        Arguments.of("https://www.google.com/", new URL("https://www.google.com/")),
        Arguments.of("https://fr\u00FCher.xyz/", new URL("https://früher.xyz/")));
  }

  private static Stream<Arguments> buildAlternativeUrlArguments() throws MalformedURLException {
    return Stream.of(
        Arguments.of(null, null, null),
        Arguments.of(new URL("https://google.com"), null, null),
        Arguments.of(new URL("https://google.com"), null, null),
        Arguments.of(new URL("https://google.com"), "", null),
        Arguments.of(new URL("https://google.com"), "0|invalidUrl", null),
        Arguments.of(
            new URL("https://google.com"),
            "0|http://localhost/",
            new FilmUrl("http://localhost/", -42L)),
        Arguments.of(new URL("https://google.com"), "-1|http://localhost/", null),
        Arguments.of(
            new URL("https://google.com"), "15|de", new FilmUrl("https://google.de", -42L)),
        Arguments.of(
            new URL("https://google.com"),
            "18|/search",
            new FilmUrl("https://google.com/search", -42L)),
        Arguments.of(new URL("https://google.com"), "19|/search", null),
        Arguments.of(
            new URL("https://fr\u00FCher.xyz"), "15|de", new FilmUrl("https://früher.de", -42L)));
  }

  private static Stream<Arguments> mapDurationArguments() {
    return Stream.of(
        Arguments.of(null, Duration.ZERO),
        Arguments.of("", Duration.ZERO),
        Arguments.of("invalid Duration", Duration.ZERO),
        Arguments.of("00:00:01", Duration.ofSeconds(1)),
        Arguments.of("00:01:00", Duration.ofMinutes(1)),
        Arguments.of("01:00:00", Duration.ofHours(1)),
        Arguments.of("23:59:59", Duration.ofHours(23).plusMinutes(59).plusSeconds(59)));
  }

  private static Stream<Arguments> gatherTimeArguments() {
    return Stream.of(
        Arguments.of(null, LocalTime.MIDNIGHT),
        Arguments.of("", LocalTime.MIDNIGHT),
        Arguments.of("invalid time", LocalTime.MIDNIGHT),
        Arguments.of("00:00:01", LocalTime.of(0, 0, 1)),
        Arguments.of("00:01:00", LocalTime.of(0, 1, 0)),
        Arguments.of("01:00:00", LocalTime.of(1, 0, 0)),
        Arguments.of("23:59:59", LocalTime.of(23, 59, 59)));
  }

  private static Stream<Arguments> mapSizeArguments() {
    return Stream.of(
        Arguments.of(null, 0L),
        Arguments.of("", 0L),
        Arguments.of("invalid size", 0L),
        Arguments.of("753", 753L),
        Arguments.of("-3", -3L));
  }

  private static Stream<Arguments> gatherDateArguments() {
    return Stream.of(
        Arguments.of(null, null),
        Arguments.of("", null),
        Arguments.of("invalid date", null),
        Arguments.of("01.01.2000", LocalDate.of(2000, 1, 1)),
        Arguments.of("31.12.1999", LocalDate.of(1999, 12, 31)),
        Arguments.of("09.03.2022", LocalDate.of(2022, 3, 9)));
  }

  @Test
  void rawFilmToFilm_null_noExceptions() {
    assertThat(classUnderTest.rawFilmToFilm(null)).isNull();
  }

  @Test
  void gatherNormalUrl_withValidURL_createsValidURL() throws Exception {

    Optional<URL> result = classUnderTest.gatherNormalUrl("https://www.heise.de");

    assertThat(result.orElse(null)).isEqualTo(new URL("https://www.heise.de"));
  }

  @Test
  void gatherNormalUrl_withEscapedJava_GetsEmpty() {

    Optional<URL> result = classUnderTest.gatherNormalUrl("https://www.heise\n.de");

    assertThat(result).isEmpty();
  }

  @Test
  void rawFilmToFilm_validRawFilm_correctFilm() throws MalformedURLException {
    // GIVEN
    var rawFilm =
        new RawFilm(
            "3Sat",
            "37 Grad",
            "37°: Gewalt in den Familien (Audiodeskription)",
            "07.12.2021",
            "00:18:00",
            "00:28:50",
            "396",
            "Laut einer Studie des Bundesfamilienministeriums wird etwa jede vierte Frau mindestens einmal Opfer körperlicher oder sexueller Gewalt durch ihren aktuellen oder früheren Partner.",
            "https://rodlzdf-a.akamaihd.net/none/zdf/21/11/211130_sendung_37g/4/211130_sendung_37g_a3a4_2360k_p35v15.mp4",
            "https://www.3sat.de/gesellschaft/37-grad/37-schlag-ins-herz-100.html",
            "https://utstreaming.zdf.de/mtt/zdf/21/11/211130_sendung_37g/4/F1033253_hoh_deu_37_Grad_Schlag_ins_Herz_301121.xml",
            "91|808k_p11v15.mp4",
            "91|3360k_p36v15.mp4",
            "",
            "false");

    var expectedFilm =
        new Film(
            UUID.randomUUID(),
            Sender.DREISAT,
            "37°: Gewalt in den Familien (Audiodeskription)",
            "37 Grad",
            LocalDateTime.of(2021, 12, 7, 0, 18, 0),
            Duration.ofMinutes(28).plusSeconds(50));
    expectedFilm.setBeschreibung(
        "Laut einer Studie des Bundesfamilienministeriums wird etwa jede vierte Frau mindestens einmal Opfer körperlicher oder sexueller Gewalt durch ihren aktuellen oder früheren Partner.");
    expectedFilm.addUrl(
        Resolution.NORMAL,
        new FilmUrl(
            "https://rodlzdf-a.akamaihd.net/none/zdf/21/11/211130_sendung_37g/4/211130_sendung_37g_a3a4_2360k_p35v15.mp4",
            396L));
    expectedFilm.setWebsite(
        new URL("https://www.3sat.de/gesellschaft/37-grad/37-schlag-ins-herz-100.html"));
    expectedFilm.addSubtitle(
        new URL(
            "https://utstreaming.zdf.de/mtt/zdf/21/11/211130_sendung_37g/4/F1033253_hoh_deu_37_Grad_Schlag_ins_Herz_301121.xml"));
    expectedFilm.addUrl(
        Resolution.SMALL,
        new FilmUrl(
            "https://rodlzdf-a.akamaihd.net/none/zdf/21/11/211130_sendung_37g/4/211130_sendung_37g_a3a4_808k_p11v15.mp4",
            396L));
    expectedFilm.addUrl(
        Resolution.HD,
        new FilmUrl(
            "https://rodlzdf-a.akamaihd.net/none/zdf/21/11/211130_sendung_37g/4/211130_sendung_37g_a3a4_3360k_p36v15.mp4",
            396L));
    expectedFilm.addGeolocation(GeoLocations.GEO_NONE);

    // WHEN
    var film = classUnderTest.rawFilmToFilm(rawFilm);

    // THEN
    assertThat(film).usingRecursiveComparison().ignoringFields("uuid").isEqualTo(expectedFilm);
  }

  @Test
  void mapGeolocation_geoNull_emptyList() {
    assertThat(classUnderTest.mapGeolocation(RawFilm.builder().build())).isEmpty();
  }

  @Test
  void mapGeolocation_unknownGeo_emptyList() {
    assertThat(classUnderTest.mapGeolocation(RawFilm.builder().geo("unknown").build())).isEmpty();
  }

  @ParameterizedTest
  @EnumSource(GeoLocations.class)
  void mapGeolocation_validGeo_listWithGeo(GeoLocations testGeoLocation) {
    // Description
    assertThat(
            classUnderTest.mapGeolocation(
                RawFilm.builder().geo(testGeoLocation.getDescription()).build()))
        .containsExactly(testGeoLocation);

    // Alternatives
    Arrays.stream(testGeoLocation.getAlternatives())
        .forEach(
            alternative ->
                assertThat(
                        classUnderTest.mapGeolocation(RawFilm.builder().geo(alternative).build()))
                    .containsExactly(testGeoLocation));
  }

  @Test
  void websiteToWebsiteUrl_null_null() {
    assertThat(classUnderTest.websiteToWebsiteUrl(RawFilm.builder().website(null).build()))
        .isNull();
  }

  @Test
  void websiteToWebsiteUrl_emptyString_null() {
    assertThat(classUnderTest.websiteToWebsiteUrl(RawFilm.builder().website("").build())).isNull();
  }

  @Test
  void websiteToWebsiteUrl_invalidUrl_null() {
    assertThat(classUnderTest.websiteToWebsiteUrl(RawFilm.builder().website("unknown").build()))
        .isNull();
  }

  @Test
  void websiteToWebsiteUrl_urlWithSpecialChars_url() throws MalformedURLException {
    assertThat(
            classUnderTest.websiteToWebsiteUrl(
                RawFilm.builder().website("https://fr\u00FCher.xyz/").build()))
        .isEqualTo(new URL("https://früher.xyz/"));
  }

  @Test
  void senderTextToSender_null_exception() {
    assertThat(catchThrowable(() -> classUnderTest.senderTextToSender(null)))
        .isInstanceOf(RawFilmToFilmException.class);
  }

  @Test
  void senderTextToSender_empty_exception() {
    assertThat(catchThrowable(() -> classUnderTest.senderTextToSender("")))
        .isInstanceOf(RawFilmToFilmException.class);
  }

  @Test
  void senderTextToSender_invalidSender_exception() {
    assertThat(catchThrowable(() -> classUnderTest.senderTextToSender("unknown")))
        .isInstanceOf(RawFilmToFilmException.class);
  }

  @ParameterizedTest
  @EnumSource(Sender.class)
  void senderTextToSender_validSenderName_sender(Sender testSender) {
    assertThat(classUnderTest.senderTextToSender(testSender.getName())).isEqualTo(testSender);

    Arrays.stream(testSender.getNameAlternatives())
        .forEach(
            alternativeName ->
                assertThat(classUnderTest.senderTextToSender(alternativeName))
                    .isEqualTo(testSender));
  }

  @ParameterizedTest
  @MethodSource("mapSubtitleUrlArguments")
  void mapSubtitleUrl(String testUrl, List<URL> expectedUrl) {
    RawFilm testFilm = RawFilm.builder().urlUntertitel(testUrl).build();

    if (expectedUrl.isEmpty()) {
      assertThat(classUnderTest.mapSubtitleUrl(testFilm)).isEmpty();
    } else {
      assertThat(classUnderTest.mapSubtitleUrl(testFilm)).containsExactlyElementsOf(expectedUrl);
    }
  }

  @ParameterizedTest
  @MethodSource("gatherNormalUrlArguments")
  void gatherNormalUrl(String testUrl, URL expectedUrl) {
    if (expectedUrl == null) {
      assertThat(classUnderTest.gatherNormalUrl(testUrl)).isNotPresent();
    } else {
      assertThat(classUnderTest.gatherNormalUrl(testUrl)).isPresent().get().isEqualTo(expectedUrl);
    }
  }

  @ParameterizedTest
  @MethodSource("buildAlternativeUrlArguments")
  void buildAlternativeUrl(URL testBaseUrl, String testUrl, FilmUrl expectedUrl) {
    // Just for the logging ¯\_(ツ)_/¯
    Film testFilm =
        new Film(
            UUID.randomUUID(),
            Sender.NDR,
            "Test title",
            "Test thema",
            LocalDateTime.now(),
            Duration.ZERO);
    long testGroesse = -42L;
    if (expectedUrl == null) {
      assertThat(classUnderTest.buildAlternativeUrl(testFilm, testGroesse, testBaseUrl, testUrl))
          .isNotPresent();
    } else {
      assertThat(classUnderTest.buildAlternativeUrl(testFilm, testGroesse, testBaseUrl, testUrl))
          .isPresent()
          .get()
          .isEqualTo(expectedUrl);
    }
  }

  @ParameterizedTest
  @MethodSource("mapDurationArguments")
  void mapDuration(String dauer, Duration expectedDuration) {
    RawFilm film =
        RawFilm.builder().sender("BR").titel("Test titel").thema("Test thema").dauer(dauer).build();
    assertThat(classUnderTest.mapDuration(film)).isEqualTo(expectedDuration);
  }

  @ParameterizedTest
  @MethodSource("gatherTimeArguments")
  void gatherTime(String time, LocalTime expectedTime) {
    assertThat(classUnderTest.gatherTime(time)).isEqualTo(expectedTime);
  }

  @ParameterizedTest
  @MethodSource("mapSizeArguments")
  void mapSize(String sizeInMbText, Long sizeinMb) {
    RawFilm film =
        RawFilm.builder()
            .sender("BR")
            .titel("Test titel")
            .thema("Test thema")
            .groesseMb(sizeInMbText)
            .build();
    assertThat(classUnderTest.mapSize(film)).isEqualTo(sizeinMb);
  }

  @ParameterizedTest
  @MethodSource("gatherDateArguments")
  void gatherDate(String dateText, LocalDate expectedDate) {
    RawFilm film =
        RawFilm.builder()
            .sender("BR")
            .titel("Test titel")
            .thema("Test thema")
            .datum(dateText)
            .build();
    if (expectedDate == null) {
      assertThat(classUnderTest.gatherDate(film)).isNotPresent();
    } else {
      assertThat(classUnderTest.gatherDate(film)).isPresent().get().isEqualTo(expectedDate);
    }
  }
}
