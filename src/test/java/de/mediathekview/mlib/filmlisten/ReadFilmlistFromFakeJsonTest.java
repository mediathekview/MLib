package de.mediathekview.mlib.filmlisten;

import de.mediathekview.mlib.daten.*;
import org.hamcrest.*;
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
 * Tests the class {@link FilmlisteLesen}
 */
public class ReadFilmlistFromFakeJsonTest
{
    private static final String TEST_FILENAME = "TestFilmlist.json";

    private ListeFilme testData;

    @Before
    public void setUp() throws URISyntaxException
    {
        testData = FilmlistTestData.getInstance().getTestData();
    }

    @Test
    public void testReadData() throws IOException, URISyntaxException
    {
        ClassLoader classLoader = getClass().getClassLoader();

        FilmlisteLesen filmlisteLesen = new FilmlisteLesen();
        ListeFilme readFilme = filmlisteLesen.readData(Files.newInputStream(Paths.get(classLoader.getResource("TestFilmlist.json").toURI())));
        Assert.assertThat(readFilme,
                Matchers.hasItems(testData.toArray(new Film[]{})));
    }
}
