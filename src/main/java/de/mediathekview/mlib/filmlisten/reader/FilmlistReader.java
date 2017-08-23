package de.mediathekview.mlib.filmlisten.reader;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;

import de.mediathekview.mlib.daten.Filmlist;
import de.mediathekview.mlib.messages.LibMessages;
import de.mediathekview.mlib.messages.listener.MessageListener;

public class FilmlistReader extends AbstractFilmlistReader {
	private static final Logger LOG = LogManager.getLogger(FilmlistReader.class);

	public FilmlistReader() {
		super();
	}

	public FilmlistReader(final MessageListener... aListeners) {
		super(aListeners);
	}

	@Override
	public Optional<Filmlist> read(InputStream aInputStream) {
		Gson gson = new Gson();
		try (JsonReader jsonReader = new JsonReader(new BufferedReader(new InputStreamReader(aInputStream)))) {
			return Optional.of(gson.fromJson(jsonReader, Filmlist.class));
		} catch (IOException ioException) {
			LOG.debug("Something went wrong on writing the film list.", ioException);
			publishMessage(LibMessages.FILMLIST_READ_ERROR);
			return Optional.empty();
		}
	}
}