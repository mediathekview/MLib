package de.mediathekview.mlib.filmlisten;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
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
  private static Filmlist testData;

  private static final FilmlistManager filmlistManager = FilmlistManager.getInstance();
  private final String jsonName;
  private final FilmlistFormats format;

  public NewFilmlistReadTest(final String aJsonName, final FilmlistFormats aFormat) {
    jsonName = getClass().getClassLoader().getResource(aJsonName).getFile();
    format = aFormat;
  }

  @Parameterized.Parameters(name = "Test {index} Filmlist for {0} with {1}")
  public static Collection<Object[]> data() {
    return Arrays.asList(new Object[][] {{"TestFilmlistNewJson.json", FilmlistFormats.JSON},
        {"TestFilmlistNewJson.json.xz", FilmlistFormats.JSON_COMPRESSED_XZ},
        {"TestFilmlistNewJson.json.gz", FilmlistFormats.JSON_COMPRESSED_GZIP},
        {"TestFilmlistNewJson.json.bz", FilmlistFormats.JSON_COMPRESSED_BZIP},
        {"TestFilmlist.json", FilmlistFormats.OLD_JSON},
        {"TestFilmlist.json.xz", FilmlistFormats.OLD_JSON_COMPRESSED_XZ},
        {"TestFilmlist.json.gz", FilmlistFormats.OLD_JSON_COMPRESSED_GZIP},
        {"TestFilmlist.json.bz", FilmlistFormats.OLD_JSON_COMPRESSED_BZIP}});
  }

  @BeforeClass
  public static void initTestData() throws URISyntaxException, IOException {
    testData = FilmlistTestData.getInstance().createTestdataNewFormat();  
  }
  
  @Test
  public void testRead() throws IOException {
    final Path testFilePath = new File(jsonName).toPath();
    final Optional<Filmlist> result = filmlistManager.importList(format, testFilePath);

    System.out.println(result.isPresent());

    Assert.assertThat(result.isPresent(), CoreMatchers.is(true));
    Assert.assertThat(result.get().getFilms().size(), CoreMatchers.is(testData.getFilms().size()));
  }

}
