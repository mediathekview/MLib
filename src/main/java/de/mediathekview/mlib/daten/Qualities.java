package de.mediathekview.mlib.daten;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public enum Qualities
{
    HD(3, "HD"), NORMAL(2, "Normal"), SMALL(1, "Klein"), VERY_SMALL(0, "Sehr klein");

    /**
     * The bigger the index the better the quality.
     */
    private final int index;
    private final String description;

    Qualities(final int aIndex, final String aDescription)
    {
        index = aIndex;
        description = aDescription;
    }

    public String getDescription()
    {
        return description;
    }

    public int getIndex()
    {
        return index;
    }

    public static List<Qualities> getFromBestToLowest()
    {
        return Arrays.asList(Qualities.values()).stream().sorted(Comparator.comparing(Qualities::getIndex).reversed())
                .collect(Collectors.toList());
    }
}
