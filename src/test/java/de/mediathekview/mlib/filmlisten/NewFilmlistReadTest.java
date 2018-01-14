package de.mediathekview.mlib.filmlisten;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;
import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import de.mediathekview.mlib.daten.Filmlist;

@RunWith(Parameterized.class)
public class NewFilmlistReadTest {
  private static final String BASE_FOLDER = "";
  private static Filmlist testData;
  private static Path testFileFolderPath;

  private static final FilmlistManager filmlistManager = FilmlistManager.getInstance();
  private final String jsonName;
  private final FilmlistFormats format;

  public NewFilmlistReadTest(final String aJsonName, final FilmlistFormats aFormat) {
    jsonName = aJsonName;
    format = aFormat;
  }

  @Parameterized.Parameters(name = "Test {index} Filmlist for {0} with {1}")
  public static Collection<Object[]> data() {
    return Arrays.asList(new Object[][] {{"TestFilmlistNewJson.json", FilmlistFormats.JSON},
        {"TestFilmlistNewJson.json.xz", FilmlistFormats.JSON_COMPRESSED_XZ},
        {"TestFilmlistNewJson.json.gz", FilmlistFormats.JSON_COMPRESSED_GZIP},
        {"TestFilmlist.json", FilmlistFormats.OLD_JSON},
        {"TestFilmlist.json.xz", FilmlistFormats.OLD_JSON_COMPRESSED_XZ},
        {"TestFilmlist.json.gz", FilmlistFormats.OLD_JSON_COMPRESSED_GZIP}});
  }

  @BeforeClass
  public static void initTestData() throws URISyntaxException, IOException {
    testData = FilmlistTestData.getInstance().createTestdataNewFormat();
    testFileFolderPath =
        Paths.get(NewFilmlistReadTest.class.getClassLoader().getResource(BASE_FOLDER).toURI());
  }

  @Test
  public void testRead() throws IOException {
    final Path testFilePath = testFileFolderPath.resolve(jsonName);
    final Optional<Filmlist> result = filmlistManager.importList(format, testFilePath);

    Assert.assertThat(result.isPresent(), CoreMatchers.is(true));
    Assert.assertThat(result.get().getFilms().size(), CoreMatchers.is(testData.getFilms().size()));
  }

}
