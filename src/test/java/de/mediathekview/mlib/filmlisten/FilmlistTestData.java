package de.mediathekview.mlib.filmlisten;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;

import de.mediathekview.mlib.daten.Film;
import de.mediathekview.mlib.daten.FilmUrl;
import de.mediathekview.mlib.daten.Filmlist;
import de.mediathekview.mlib.daten.ListeFilme;
import de.mediathekview.mlib.daten.Quality;
import de.mediathekview.mlib.daten.Sender;

/**
 * A singelton to get the test data for Filmlist tests.
 */
public class FilmlistTestData
{
    private static FilmlistTestData instance = null;

    public static FilmlistTestData getInstance() throws MalformedURLException
    {
        if (instance == null)
        {
            instance = new FilmlistTestData();
        }
        return instance;
    }

    private FilmlistTestData() throws MalformedURLException
    {
        super();
    }

    public ListeFilme createTestdataOldFormat() throws MalformedURLException
    {
        final ListeFilme testData = new ListeFilme();
        testData.writeMetaData();
        testData.addAll(createFilme());
        return testData;
    }

    public Filmlist createTestdataNewFormat() throws MalformedURLException
    {
        final Filmlist testData = new Filmlist();
        testData.addAll(createFilme());
        return testData;
    }

    public Collection<Film> createFilme() throws MalformedURLException
    {
        final Collection<Film> films = new ArrayList<>();

        final Film testFilm1 = new Film(UUID.randomUUID(), new ArrayList<>(), Sender.ARD, "TestTitel", "TestThema",
                LocalDateTime.parse("2017-01-01T23:55:00"), Duration.of(10, ChronoUnit.MINUTES),
                new URL("http://www.example.org/"));
        testFilm1.setBeschreibung("Test beschreibung.");
        testFilm1.addUrl(Quality.SMALL, new FilmUrl(new URL("http://example.org/klein.mp4"), 42l));
        testFilm1.addUrl(Quality.NORMAL, new FilmUrl(new URL("http://example.org/Test.mp4"), 42l));
        testFilm1.addUrl(Quality.HD, new FilmUrl(new URL("http://example.org/hd.mp4"), 42l));

        final Film testFilm2 = new Film(UUID.randomUUID(), new ArrayList<>(), Sender.ARD, "TestTitel", "TestThema",
                LocalDateTime.parse("2017-01-01T23:55:00"), Duration.of(10, ChronoUnit.MINUTES),
                new URL("http://www.example.org/2"));
        testFilm2.setBeschreibung("Test beschreibung.");
        testFilm2.addUrl(Quality.SMALL, new FilmUrl(new URL("http://example.org/klein2.mp4"), 42l));
        testFilm2.addUrl(Quality.NORMAL, new FilmUrl(new URL("http://example.org/Test2.mp4"), 42l));
        testFilm2.addUrl(Quality.HD, new FilmUrl(new URL("http://example.org/hd2.mp4"), 42l));

        final Film testFilm3 = new Film(UUID.randomUUID(), new ArrayList<>(), Sender.BR, "TestTitel", "TestThema2",
                LocalDateTime.parse("2017-01-01T23:55:00"), Duration.of(10, ChronoUnit.MINUTES),
                new URL("http://www.example.org/"));
        testFilm3.setBeschreibung("Test beschreibung.");
        testFilm3.addUrl(Quality.SMALL, new FilmUrl(new URL("http://example.org/klein.mp4"), 42l));
        testFilm3.addUrl(Quality.NORMAL, new FilmUrl(new URL("http://example.org/Test.mp4"), 42l));
        testFilm3.addUrl(Quality.HD, new FilmUrl(new URL("http://example.org/hd.mp4"), 42l));

        films.add(testFilm1);
        films.add(testFilm2);
        films.add(testFilm3);
        return films;
    }

}
