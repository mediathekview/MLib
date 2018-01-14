package de.mediathekview.mlib.filmlisten;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import org.hamcrest.CoreMatchers;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import de.mediathekview.mlib.daten.Filmlist;

@RunWith(Parameterized.class)
public class NewFilmlistWriteTest {
  private static final String TEMP_FOLDER_NAME_PATTERN = "MLIB_TEST_%d";
  private static Filmlist testData;
  private static Path testFileFolderPath;

  private static final FilmlistManager filmlistManager = FilmlistManager.getInstance();
  private final String jsonName;
  private final FilmlistFormats format;

  public NewFilmlistWriteTest(final String aJsonName, final FilmlistFormats aFormat) {
    jsonName = aJsonName;
    format = aFormat;
  }

  @Parameterized.Parameters(name = "Test {index} Filmlist for {0} with {1}")
  public static Collection<Object[]> data() {
    return Arrays.asList(new Object[][] {{"TestWriteNewJson_%d.json", FilmlistFormats.JSON},
        {"TestWriteNewJsonCompressed_%d.json.xz", FilmlistFormats.JSON_COMPRESSED_XZ},
        {"TestWriteNewJsonCompressed_%d.json.gz", FilmlistFormats.JSON_COMPRESSED_GZIP},
        {"TestWriteOldJson_%d.json", FilmlistFormats.OLD_JSON},
        {"TestWriteOldJsonCompressed_%d.json.xz", FilmlistFormats.OLD_JSON_COMPRESSED_XZ},
        {"TestWriteOldJsonCompressed_%d.json.gz", FilmlistFormats.OLD_JSON_COMPRESSED_GZIP}});
  }

  @AfterClass
  public static void deleteTempFiles() throws IOException {
    Files.walk(testFileFolderPath).sorted(Comparator.reverseOrder()).map(Path::toFile)
        .forEach(File::delete);
  }

  @BeforeClass
  public static void initTestData() throws URISyntaxException, IOException {
    testData = FilmlistTestData.getInstance().createTestdataNewFormat();
    testFileFolderPath = Files.createTempDirectory(formatWithDate(TEMP_FOLDER_NAME_PATTERN));
  }

  private static String formatWithDate(final String aPattern) {
    return String.format(aPattern, new Date().getTime());
  }

  @Test
  public void testWrite() {
    final Path testFilePath = testFileFolderPath.resolve(formatWithDate(jsonName));
    filmlistManager.save(format, testData, testFilePath);

    if (format.getFileExtension().contains("xz")) {
      Assert.assertThat(
          Files.exists(testFilePath.resolveSibling(testFilePath.getFileName().toString())),
          CoreMatchers.is(true));
    } else {
      Assert.assertThat(Files.exists(testFilePath), CoreMatchers.is(true));
    }
  }

}
