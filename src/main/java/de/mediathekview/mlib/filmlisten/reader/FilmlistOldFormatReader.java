package de.mediathekview.mlib.filmlisten.reader;

import static java.time.format.FormatStyle.MEDIUM;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collection;
import java.util.Locale;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.mediathekview.mlib.daten.FilmComperatorFactory;
import de.mediathekview.mlib.daten.Filmlist;
import de.mediathekview.mlib.filmlisten.FilmToFakeJsonConverter;
import de.mediathekview.mlib.messages.LibMessages;
import de.mediathekview.mlib.tool.Functions;

public class FilmlistOldFormatReader extends AbstractFilmlistReader {
	private static final Logger LOG = LogManager.getLogger(FilmlistOldFormatReader.class);

	@Override
	public Filmlist read(InputStream aInputStream) {
		// TODO Auto-generated method stub
		return null;
	}

}
