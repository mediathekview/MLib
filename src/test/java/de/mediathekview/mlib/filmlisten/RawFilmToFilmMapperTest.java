package de.mediathekview.mlib.filmlisten;

import de.mediathekview.mlib.daten.*;
import de.mediathekview.mlib.filmlisten.reader.RawFilm;
import de.mediathekview.mlib.filmlisten.reader.RawFilmToFilmMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.mockito.Mock;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

class RawFilmToFilmMapperTest {
  private final RawFilmToFilmMapper classUnderTest =
      Mappers.getMapper(RawFilmToFilmMapper.class);

  @Test
  void rawFilmToFilm_null_noExceptions() {
    assertThat(classUnderTest.rawFilmToFilm(null)).isNull();
  }

  @Mock
  RawFilm rawFilmMock;

  @BeforeEach
  void intializeFilmMockWithDefaultData() {
    when(rawFilmMock.getSender()).thenReturn("3Sat");

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

  }

  @Test
  void testMappper_withNullObjectTree_willGetNoNPE() {

    Film result = classUnderTest.rawFilmToFilm(rawFilmMock);

    assertThat(result).isNull();

  }

  @Test
  void testGatherNormalUrl_withNull_getsEmpty() {
    when(rawFilmMock.getSender()).thenReturn(null);

    Optional result = classUnderTest.gatherNormalUrl(null);

    assertThat(result).isEmpty();
  }

  @Test
  void testGatherNormalUrl_withValidURL_createsValidURL() throws Exception {

    Optional result = classUnderTest.gatherNormalUrl("https://www.heise.de");

    assertThat(result.orElseGet(() -> null)).isEqualTo(new URL("https://www.heise.de"));
  }

  @Test
  void testGatherNormalUrl_withEscpaedJava_GetsEmpty() throws Exception {

    Optional result = classUnderTest.gatherNormalUrl("https://www.heise\n.de");

    assertThat(result).isEmpty();
  }


  @Test
  void rawFilmToFilm_validRawFilm_correctFilm() throws MalformedURLException {
    // GIVNE
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
}