package de.mediathekview.mlib.filmlisten.reader;

import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.Singular;
import org.apache.logging.log4j.util.Strings;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@RequiredArgsConstructor
public class RawFilmlist {
  private final LocalDateTime creationDate;
  private final UUID listId;
  @Singular private final List<RawFilm> rawFilms;

  public int getFilmCount() {
    return rawFilms.size();
  }

  public void resolveEmptyFields() {
    for (int i = 1; i < rawFilms.size(); i++) {
      RawFilm currentFilm = rawFilms.get(i);
      if (Strings.isEmpty(currentFilm.getSender())) {
        currentFilm.setSender(rawFilms.get(i - 1).getSender());
      }
      if (Strings.isEmpty(currentFilm.getThema())) {
        currentFilm.setThema(rawFilms.get(i - 1).getThema());
      }
      if (Strings.isEmpty(currentFilm.getTitel())) {
        currentFilm.setTitel(rawFilms.get(i - 1).getTitel());
      }
    }
  }
}
