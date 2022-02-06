package de.mediathekview.mlib.filmlisten.reader;

import de.mediathekview.mlib.daten.Film;
import de.mediathekview.mlib.daten.Filmlist;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.InputStream;
import java.util.Optional;

public class FilmlistOldFormatReader extends AbstractFilmlistReader {
  private static final Logger LOG = LogManager.getLogger(FilmlistOldFormatReader.class);

  @Override
  public Optional<Filmlist> read(final InputStream filmlistInputStream) {
    OldFilmlistToRawFilmlistReader oldFilmlistToRawFilmlistReader =
        new OldFilmlistToRawFilmlistReader(filmlistInputStream);
    RawFilmlist rawFilmlist = oldFilmlistToRawFilmlistReader.read();

    Filmlist filmlist = new Filmlist(rawFilmlist.getListId(), rawFilmlist.getCreationDate());
    filmlist.addAllFilms(
        rawFilmlist.getRawFilms().parallelStream()
            .map(this::mapRawFilmToFilm)
            .filter(Optional::isPresent)
            .map(Optional::get)
            .toList());
    return Optional.of(filmlist);
  }

  private Optional<Film> mapRawFilmToFilm(RawFilm rawFilm) {
    try {
      return Optional.of(RawFilmToFilmMapper.INSTANCE.rawFilmToFilm(rawFilm, rawFilm));
    } catch (RawFilmToFilmException rawFilmToFilmException) {
      LOG.error("Skipping a film with invalid data.", rawFilmToFilmException);
      return Optional.empty();
    }
  }
}
