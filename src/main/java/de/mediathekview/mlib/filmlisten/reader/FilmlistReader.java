package de.mediathekview.mlib.filmlisten.reader;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.jidesoft.icons.NetworkIconSet;

import de.mediathekview.mlib.daten.Filmlist;
import de.mediathekview.mlib.messages.LibMessages;
import de.mediathekview.mlib.messages.MessageCreator;
import de.mediathekview.mlib.messages.listener.MessageListener;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

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