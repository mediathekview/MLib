package de.mediathekview.mlib.filmlisten;

import de.mediathekview.mlib.daten.*;
import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.UUID;

/**
 * Tests the class {@link WriteFilmlistJson}
 */
public class WriteFilmlistToFakeJsonTest
{
    private static final String TEST_RIGHT_RESULT_FILENAME = "TestFilmlist.json";
    private static final String TEST_FILENAME = "TestGeneratedFilmlist.json";

    private ListeFilme testData;

    @Before
    public void setUp() throws URISyntaxException
    {
        testData = FilmlistTestData.getInstance().getTestData();
    }

    @Test
    public void testFilmlistSave() throws IOException, URISyntaxException
    {
        ClassLoader classLoader = getClass().getClassLoader();
        WriteFilmlistJson writeFilmlistJson = new WriteFilmlistJson();
        writeFilmlistJson.filmlisteSchreibenJson(TEST_FILENAME, testData);
        Assert.assertThat(Files.readAllLines(Paths.get(classLoader.getResource(TEST_FILENAME).toURI())).remove(2),
                CoreMatchers.is(Files.readAllLines(Paths.get(classLoader.getResource(TEST_RIGHT_RESULT_FILENAME).toURI())).remove(2)
                ));
    }
}
