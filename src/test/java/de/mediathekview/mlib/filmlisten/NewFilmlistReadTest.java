package de.mediathekview.mlib.filmlisten;

import static org.assertj.core.api.Assertions.from;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import de.mediathekview.mlib.daten.Filmlist;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Path;
import java.util.Optional;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

public class NewFilmlistReadTest {

  private static final FilmlistManager filmlistManager = FilmlistManager.getInstance();
  private static Filmlist referenceData;

  @BeforeAll
  public static void intializeReferenceData() throws MalformedURLException {
    referenceData = FilmlistTestData.getInstance().createTestdataNewFormat();
  }

  @ParameterizedTest
  @MethodSource("createReadTestArguments")
  public void filmListReadTest(String filename, FilmlistFormats filmlistFormats)
      throws IOException {
    ClassLoader classLoader = getClass().getClassLoader();
    final Path testFilePath = new File(classLoader.getResource(filename).getFile()).toPath();

    Optional<Filmlist> classUnderTest = filmlistManager.importList(filmlistFormats, testFilePath);

    assertThat(classUnderTest)
        .isNotEmpty()
        .get().returns(referenceData.getFilms().size(),
        from(Filmlist::getFilms).andThen(ConcurrentMap::size));
  }

  private static Stream<Arguments> createReadTestArguments() {
    return Stream.of(
        Arguments.of("TestFilmlistNewJson.json", FilmlistFormats.JSON),
        Arguments.of("TestFilmlistNewJson.json.xz", FilmlistFormats.JSON_COMPRESSED_XZ),
        Arguments.of("TestFilmlistNewJson.json.gz", FilmlistFormats.JSON_COMPRESSED_GZIP),
        Arguments.of("TestFilmlistNewJson.json.bz", FilmlistFormats.JSON_COMPRESSED_BZIP),
        Arguments.of("TestFilmlist.json", FilmlistFormats.OLD_JSON),
        Arguments.of("TestFilmlist.json.xz", FilmlistFormats.OLD_JSON_COMPRESSED_XZ),
        Arguments.of("TestFilmlist.json.gz", FilmlistFormats.OLD_JSON_COMPRESSED_GZIP),
        Arguments.of("TestFilmlist.json.bz", FilmlistFormats.OLD_JSON_COMPRESSED_BZIP)
    );
  }

}
