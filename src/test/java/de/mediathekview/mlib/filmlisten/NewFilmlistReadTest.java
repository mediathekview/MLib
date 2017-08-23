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
	private static final String BASE_FOLDER = "/";
	private static Filmlist testData;
	private static Path testFileFolderPath;

	private static final FilmlistManager filmlistManager = FilmlistManager.getInstance();
	private final String jsonName;
	private final FilmlistFormats format;

	@BeforeClass
	public static void initTestData() throws URISyntaxException, IOException {
		testData = FilmlistTestData.getInstance().createTestdataNewFormat();
		testFileFolderPath = Paths.get(NewFilmlistReadTest.class.getResource(BASE_FOLDER).toURI());
	}

	@Parameterized.Parameters(name = "Test {index} Filmlist for {0} with {1}")
	public static Collection<Object[]> data() {
		return Arrays.asList(new Object[][] { { "TestFilmlistNewJson.json", FilmlistFormats.JSON },
				{ "TestFilmlistNewJson.json.xz", FilmlistFormats.JSON_COMPRESSED },
				{ "TestFilmlist.json", FilmlistFormats.OLD_JSON },
				{ "TestFilmlist.json.xz", FilmlistFormats.OLD_JSON_COMPRESSED } });
	}

	public NewFilmlistReadTest(String aJsonName, FilmlistFormats aFormat) {
		jsonName = aJsonName;
		format = aFormat;
	}

	@Test
	public void testRead() throws IOException {
		Path testFilePath = testFileFolderPath.resolve(jsonName);
		Optional<Filmlist> result = filmlistManager.importList(format, testFilePath);

		Assert.assertThat(result.isPresent(), CoreMatchers.is(true));
		Assert.assertThat(result.get().getFilms().size(), CoreMatchers.is(testData.getFilms().size()));
	}

}
