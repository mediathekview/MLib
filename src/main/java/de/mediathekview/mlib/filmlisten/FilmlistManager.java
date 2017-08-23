package de.mediathekview.mlib.filmlisten;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

import de.mediathekview.mlib.daten.Filmlist;
import de.mediathekview.mlib.filmlisten.reader.FilmlistOldFormatReader;
import de.mediathekview.mlib.filmlisten.reader.FilmlistReader;
import de.mediathekview.mlib.filmlisten.writer.FilmlistOldFormatWriter;
import de.mediathekview.mlib.filmlisten.writer.FilmlistWriter;
import de.mediathekview.mlib.messages.LibMessages;
import de.mediathekview.mlib.messages.MessageCreator;
import de.mediathekview.mlib.tool.MVHttpClient;
import de.mediathekview.mlib.tool.XZManager;
import okhttp3.OkHttpClient;
import okhttp3.Request;

public class FilmlistManager extends MessageCreator {
	private static FilmlistManager instance;
	private final FilmlistOldFormatWriter filmlistOldFormatWriter;
	private final FilmlistWriter filmlistWriter;

	public static FilmlistManager getInstance() {
		if (instance == null) {
			instance = new FilmlistManager();
		}
		return instance;
	}

	private FilmlistManager() {
		super();
		filmlistOldFormatWriter = new FilmlistOldFormatWriter();
		filmlistWriter = new FilmlistWriter();
	}

	public boolean save(FilmlistFormats aFormat, Filmlist aFilmlist, Path aSavePath) {
		try {
			publishMessage(LibMessages.FILMLIST_WRITE_STARTED, aSavePath);
			switch (aFormat) {
			case JSON:
				return filmlistWriter.write(aFilmlist, aSavePath);

			case JSON_COMPRESSED:
				return filmlistWriter.write(aFilmlist, aSavePath) && compress(aSavePath);

			case OLD_JSON:
				return filmlistOldFormatWriter.write(aFilmlist, aSavePath);

			case OLD_JSON_COMPRESSED:
				return filmlistOldFormatWriter.write(aFilmlist, aSavePath) && compress(aSavePath);

			default:
				return false;
			}
		} finally {
			publishMessage(LibMessages.FILMLIST_WRITE_FINISHED, aSavePath);
		}
	}

	private boolean compress(final Path aSavePath) {
		try {
			XZManager.getInstance().compress(aSavePath);
			return true;
		} catch (IOException ioException) {
			publishMessage(LibMessages.FILMLIST_COMPRESS_ERROR, aSavePath.toAbsolutePath().toString());
			return false;
		}
	}

	public Optional<Filmlist> importList(final FilmlistFormats aFormat, final Path aFilePath) throws IOException {
		try (InputStream fileInputStream = Files.newInputStream(aFilePath)) {
			return importList(aFormat, fileInputStream);
		}
	}

	public Optional<Filmlist> importList(final FilmlistFormats aFormat, final URL aUrl) throws IOException {
		final Request request = new Request.Builder().url(aUrl).build();
		OkHttpClient httpClient = MVHttpClient.getInstance().getHttpClient();
		try (InputStream fileInputStream = httpClient.newCall(request).execute().body().byteStream()) {
			return importList(aFormat, fileInputStream);
		}
	}

	public Optional<Filmlist> importList(final FilmlistFormats aFormat, final InputStream aInputStream) throws IOException {
		InputStream input;
		if (FilmlistFormats.JSON_COMPRESSED.equals(aFormat) || FilmlistFormats.OLD_JSON_COMPRESSED.equals(aFormat)) {
			input = XZManager.getInstance().decompress(aInputStream);
		} else {
			input = aInputStream;
		}

		if (FilmlistFormats.JSON.equals(aFormat) || FilmlistFormats.JSON_COMPRESSED.equals(aFormat)) {
			return new FilmlistReader().read(input);
		} else {
			return new FilmlistOldFormatReader().read(input);
		}
	}

}
