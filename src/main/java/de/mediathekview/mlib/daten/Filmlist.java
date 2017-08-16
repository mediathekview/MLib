package de.mediathekview.mlib.daten;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListMap;

/**
 * A class that holds a thread safe map of {@link Film} with there UUIDs and some additional information.
 */
public class Filmlist
{
    private final ConcurrentHashMap<UUID,Film> films;
    private final LocalDateTime creationDate;
    private final UUID listId;

    public Filmlist(UUID aListId, LocalDateTime aCreationDate)
    {
        super();
        films = new ConcurrentHashMap<>();
        listId=aListId;
        creationDate = aCreationDate;
    }

    public Filmlist()
    {
        this(UUID.randomUUID(),LocalDateTime.now());
    }

    public UUID getListId()
    {
        return listId;
    }

    public LocalDateTime getCreationDate()
    {
        return creationDate;
    }
    
    public void addAll(Collection<Film> aFilms)
    {
        aFilms.stream().forEach(f -> films.put(f.getUuid(),f));
    }

    public ConcurrentHashMap<UUID, Film> getFilms()
    {
        return films;
    }

    public List<Film> getFilmsSorted(Comparator<Film> aFilmComperator)
    {
        List<Film> sortedFilms = new ArrayList<>(films.values());
        sortedFilms.sort(aFilmComperator);
        return sortedFilms;
    }
    
    public void merge(Filmlist aFilmlist)
    {
        aFilmlist.films.forEach(films::putIfAbsent);
    }
}
