package de.mediathekview.mlib.daten;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A class that holds a thread safe map of {@link Film} with there UUIDs and
 * some additional information.
 */
public class Filmlist {
	private final ConcurrentHashMap<UUID, Film> films;
	private LocalDateTime creationDate;
	private UUID listId;

	public void setCreationDate(LocalDateTime creationDate) {
		this.creationDate = creationDate;
	}

	public void setListId(UUID listId) {
		this.listId = listId;
	}

	public Filmlist(UUID aListId, LocalDateTime aCreationDate) {
		super();
		films = new ConcurrentHashMap<>();
		listId = aListId;
		creationDate = aCreationDate;
	}

	public Filmlist() {
		this(UUID.randomUUID(), LocalDateTime.now());
	}

	public UUID getListId() {
		return listId;
	}

	public LocalDateTime getCreationDate() {
		return creationDate;
	}

	public void add(Film aFilm) {
		films.put(aFilm.getUuid(), aFilm);
	}

	public void addAll(Collection<Film> aFilms) {
		aFilms.stream().forEach(this::add);
	}

	public ConcurrentHashMap<UUID, Film> getFilms() {
		return films;
	}

	public List<Film> getFilmsSorted(Comparator<Film> aFilmComperator) {
		List<Film> sortedFilms = new ArrayList<>(films.values());
		sortedFilms.sort(aFilmComperator);
		return sortedFilms;
	}

	public void merge(Filmlist aFilmlist) {
		aFilmlist.films.forEach(films::putIfAbsent);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((creationDate == null) ? 0 : creationDate.hashCode());
		result = prime * result + ((films == null) ? 0 : films.hashCode());
		result = prime * result + ((listId == null) ? 0 : listId.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof Filmlist))
			return false;
		Filmlist other = (Filmlist) obj;
		if (creationDate == null) {
			if (other.creationDate != null)
				return false;
		} else if (!creationDate.equals(other.creationDate))
			return false;
		if (films == null) {
			if (other.films != null)
				return false;
		} else if (!films.equals(other.films))
			return false;
		if (listId == null) {
			if (other.listId != null)
				return false;
		} else if (!listId.equals(other.listId))
			return false;
		return true;
	}
}
