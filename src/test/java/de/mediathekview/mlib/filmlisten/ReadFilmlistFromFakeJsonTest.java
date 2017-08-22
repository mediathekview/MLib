package de.mediathekview.mlib.filmlisten;

import de.mediathekview.mlib.daten.Film;
import de.mediathekview.mlib.daten.ListeFilme;
import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Tests the class {@link FilmlisteLesen}
 */
public class ReadFilmlistFromFakeJsonTest
{
    private static final String TEST_FILENAME = "TestFilmlist.json";

    private ListeFilme testData;

    @Before
    public void setUp() throws URISyntaxException
    {
        testData = FilmlistTestData.getInstance().createTestdataOldFormat();
    }

    @Test
    public void testReadData() throws IOException, URISyntaxException
    {
        ClassLoader classLoader = getClass().getClassLoader();

        FilmlisteLesen filmlisteLesen = new FilmlisteLesen();
        ListeFilme readFilme = filmlisteLesen.readData(Files.newInputStream(Paths.get(classLoader.getResource(TEST_FILENAME).toURI())));
        Assert.assertThat(readFilme,
                CoreMatchers.hasItems(testData.toArray(new Film[]{})));
    }
}
