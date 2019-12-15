package de.mediathekview.mlib.daten;

import org.junit.jupiter.api.Test;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

public class FilmlistMergeTest {

  public Film createTestFilm1() throws MalformedURLException {
    final Film testFilm1 =
        new Film(
            UUID.randomUUID(),
            Sender.ARD,
            "TestTitel",
            "TestThema",
            LocalDateTime.parse("2017-01-01T23:55:00"),
            Duration.of(10, ChronoUnit.MINUTES));
    testFilm1.setWebsite(new URL("http://www.example.org/"));
    testFilm1.setBeschreibung("Test beschreibung.");
    testFilm1.addUrl(Resolution.SMALL, new FilmUrl(new URL("http://example.org/klein.mp4"), 42l));
    testFilm1.addUrl(Resolution.NORMAL, new FilmUrl(new URL("http://example.org/Test.mp4"), 42l));
    testFilm1.addUrl(Resolution.HD, new FilmUrl(new URL("http://example.org/hd.mp4"), 42l));
    return testFilm1;
  }

  public Film createTestFilm2() throws MalformedURLException {
    final Film testFilm2 =
        new Film(
            UUID.randomUUID(),
            Sender.ARD,
            "TestTitel",
            "TestThema",
            LocalDateTime.parse("2017-01-01T23:55:00"),
            Duration.of(10, ChronoUnit.MINUTES));
    testFilm2.setWebsite(new URL("http://www.example.org/2"));
    testFilm2.setBeschreibung("Test beschreibung.");
    testFilm2.addUrl(Resolution.SMALL, new FilmUrl(new URL("http://example.org/klein2.mp4"), 42l));
    testFilm2.addUrl(Resolution.NORMAL, new FilmUrl(new URL("http://example.org/Test2.mp4"), 42l));
    testFilm2.addUrl(Resolution.HD, new FilmUrl(new URL("http://example.org/hd2.mp4"), 42l));
    return testFilm2;
  }

  public Film createTestFilm3() throws MalformedURLException {
    final Film testFilm3 =
        new Film(
            UUID.randomUUID(),
            Sender.BR,
            "TestTitel",
            "TestThema2",
            LocalDateTime.parse("2017-01-01T23:55:00"),
            Duration.of(10, ChronoUnit.MINUTES));
    testFilm3.setWebsite(new URL("http://www.example.org/"));
    testFilm3.setBeschreibung("Test beschreibung.");
    testFilm3.addUrl(Resolution.SMALL, new FilmUrl(new URL("http://example.org/klein.mp4"), 42l));
    testFilm3.addUrl(Resolution.NORMAL, new FilmUrl(new URL("http://example.org/Test.mp4"), 42l));
    testFilm3.addUrl(Resolution.HD, new FilmUrl(new URL("http://example.org/hd.mp4"), 42l));
    return testFilm3;
  }

  @Test
  public void testMergeNotEqualsSender() throws MalformedURLException {
    final Film testFilm1 = createTestFilm1();
    final Film testFilm2 = createTestFilm2();
    final Film testFilm3 = createTestFilm3();

    final Filmlist testFilmlist1 = new Filmlist();
    testFilmlist1.add(testFilm1);
    testFilmlist1.add(testFilm2);
    testFilmlist1.add(testFilm3);

    final Film testFilm4 =
        new Film(
            UUID.randomUUID(),
            Sender.ARD,
            testFilm3.getTitel(),
            testFilm3.getThema(),
            testFilm3.getTime(),
            testFilm3.getDuration());
    testFilm4.setWebsite(testFilm3.getWebsite().orElse(null));
    testFilm4.setBeschreibung(testFilm3.getBeschreibung());
    testFilm3.getUrls().entrySet().forEach(e -> testFilm4.addUrl(e.getKey(), e.getValue()));

    final Filmlist testFilmlist2 = new Filmlist();
    testFilmlist2.add(testFilm1);
    testFilmlist2.add(testFilm2);
    testFilmlist2.add(testFilm4);
    final int sizeOld = testFilmlist1.getFilms().size();
    testFilmlist1.merge(testFilmlist2);
    assertThat(testFilmlist1.getFilms().size()).isNotEqualTo(sizeOld);
  }

  @Test
  public void testMergeNotEqualsThema() throws MalformedURLException {
    final Film testFilm1 = createTestFilm1();
    final Film testFilm2 = createTestFilm2();
    final Film testFilm3 = createTestFilm3();

    final Filmlist testFilmlist1 = new Filmlist();
    testFilmlist1.add(testFilm1);
    testFilmlist1.add(testFilm2);
    testFilmlist1.add(testFilm3);

    final Film testFilm4 =
        new Film(
            UUID.randomUUID(),
            testFilm3.getSender(),
            testFilm3.getTitel(),
            testFilm3.getThema(),
            testFilm3.getTime(),
            testFilm3.getDuration());
    testFilm4.setWebsite(testFilm3.getWebsite().orElse(null));
    testFilm4.setThema("testMergeNotEqualsThema");
    final Filmlist testFilmlist2 = new Filmlist();
    testFilmlist2.add(testFilm1);
    testFilmlist2.add(testFilm2);
    testFilmlist2.add(testFilm4);
    final int sizeOld = testFilmlist1.getFilms().size();
    testFilmlist1.merge(testFilmlist2);
    assertThat(testFilmlist1.getFilms().size()).isNotEqualTo(sizeOld);
  }

  @Test
  public void testMergeNotEqualsTitle() throws MalformedURLException {
    final Film testFilm1 = createTestFilm1();
    final Film testFilm2 = createTestFilm2();
    final Film testFilm3 = createTestFilm3();

    final Filmlist testFilmlist1 = new Filmlist();
    testFilmlist1.add(testFilm1);
    testFilmlist1.add(testFilm2);
    testFilmlist1.add(testFilm3);

    final Film testFilm4 =
        new Film(
            UUID.randomUUID(),
            testFilm3.getSender(),
            testFilm3.getTitel(),
            testFilm3.getThema(),
            testFilm3.getTime(),
            testFilm3.getDuration());
    testFilm4.setWebsite(testFilm3.getWebsite().orElse(null));
    testFilm4.setTitel("testMergeNotEqualsTitle");
    final Filmlist testFilmlist2 = new Filmlist();
    testFilmlist2.add(testFilm1);
    testFilmlist2.add(testFilm2);
    testFilmlist2.add(testFilm4);
    final int sizeOld = testFilmlist1.getFilms().size();
    testFilmlist1.merge(testFilmlist2);
    assertThat(testFilmlist1.getFilms().size()).isNotEqualTo(sizeOld);
  }

  @Test
  public void testMergeUpdateUUID() throws MalformedURLException {
    final Film testFilm1 = createTestFilm1();
    final Film testFilm2 = createTestFilm2();
    final Film testFilm3 = createTestFilm3();

    final Filmlist testFilmlist1 = new Filmlist();
    testFilmlist1.add(testFilm1);
    testFilmlist1.add(testFilm2);
    testFilmlist1.add(testFilm3);

    final Film testFilm4 =
        new Film(
            UUID.randomUUID(),
            testFilm3.getSender(),
            testFilm3.getTitel(),
            testFilm3.getThema(),
            testFilm3.getTime(),
            testFilm3.getDuration());
    testFilm4.setWebsite(testFilm3.getWebsite().orElse(null));
    testFilm4.setBeschreibung(testFilm3.getBeschreibung());
    testFilm3.getUrls().entrySet().forEach(e -> testFilm4.addUrl(e.getKey(), e.getValue()));

    final Filmlist testFilmlist2 = new Filmlist();
    testFilmlist2.add(testFilm1);
    testFilmlist2.add(testFilm2);
    testFilmlist1.add(testFilm4);
    final int sizeOld = testFilmlist1.getFilms().size();
    testFilmlist1.merge(testFilmlist2);
    assertThat(testFilmlist1.getFilms().size()).isEqualTo(sizeOld);
    assertThat(testFilm3.hashCode()).isEqualTo(testFilm4.hashCode());
  }
}