package de.mediathekview.mlib.filmlisten;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import de.mediathekview.mlib.daten.ListeFilme;

/**
 * Tests the class {@link WriteFilmlistJson}
 */
public class WriteFilmlistToFakeJsonTest
{
    private static final String TEST_RIGHT_RESULT_FILENAME = "TestFilmlist.json";
    private static final String TEST_FILENAME = "TestGeneratedFilmlist.json";

    private ListeFilme testData;

    @Before
    public void setUp() throws MalformedURLException
    {
        testData = FilmlistTestData.getInstance().createTestdataOldFormat();
    }

    @Test
    public void testFilmlistSave() throws IOException, URISyntaxException
    {
        final ClassLoader classLoader = getClass().getClassLoader();
        final WriteFilmlistJson writeFilmlistJson = new WriteFilmlistJson();
        writeFilmlistJson.filmlisteSchreibenJson(TEST_FILENAME, testData);
        Assert.assertThat(Files.readAllLines(Paths.get(classLoader.getResource(TEST_FILENAME).toURI())).remove(2),
                CoreMatchers
                        .is(Files.readAllLines(Paths.get(classLoader.getResource(TEST_RIGHT_RESULT_FILENAME).toURI()))
                                .remove(2)));
    }
}
