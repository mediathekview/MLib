package de.mediathekview.mlib.filmlisten;

import de.mediathekview.mlib.daten.Filmlist;
import org.hamcrest.CoreMatchers;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;

@RunWith(Parameterized.class)
public class NewFilmlistWriteTest
{
    private static final String TEMPF_FOLDER_NAME_PATTERN = "MLIB_TEST_%d";
    private static Filmlist testData;
    private static Path testFileFolderPath;

    private static final FilmlistManager filmlistManager = FilmlistManager.getInstance();
    private final String jsonName;
    private final FilmlistOutputFormats format;

    @BeforeClass
    public static void initTestData() throws URISyntaxException, IOException
    {
        testData = FilmlistTestData.getInstance().createTestdataNewFormat();
        testFileFolderPath = Files.createTempDirectory(formatWithDate(TEMPF_FOLDER_NAME_PATTERN));
    }

    @Parameterized.Parameters(name = "Test {index} Filmlist for {0} with {1}")
    public static Collection<Object[]> data()
    {
        return Arrays.asList(new Object[][]{
                {formatWithDate("TestWriteNewJson_%d.json"), FilmlistOutputFormats.JSON},
                {formatWithDate("TestWriteNewJsonCompressed_%d.json"), FilmlistOutputFormats.JSON_COMPRESSED},
                {formatWithDate("TestWriteOldJson_%d.json"), FilmlistOutputFormats.OLD_JSON},
                {formatWithDate("TestWriteOldJsonCompressed_%d.json"), FilmlistOutputFormats.OLD_JSON_COMPRESSED},
        });
    }


    public NewFilmlistWriteTest(String aJsonName, FilmlistOutputFormats aFormat)
    {
        jsonName = aJsonName;
        format = aFormat;
    }


    @Test
    public void testWrite()
    {
        Path testFilePath = testFileFolderPath.resolve(formatWithDate(jsonName));
        filmlistManager.save(format, testData, testFilePath);

        if (format.getFileExtension().contains("xz"))
        {
            Assert.assertThat(Files.exists(testFilePath.resolveSibling(testFilePath.getFileName().toString() + ".xz")), CoreMatchers.is(true));
        } else
        {
            Assert.assertThat(Files.exists(testFilePath), CoreMatchers.is(true));
        }
    }

    private static String formatWithDate(final String aPattern)
    {
        return String.format(aPattern, new Date().getTime());
    }

    @AfterClass
    public static void deleteTempFiles() throws IOException
    {
        Files.walk(testFileFolderPath)
                .sorted(Comparator.reverseOrder())
                .map(Path::toFile)
                .forEach(File::delete);
    }

}
