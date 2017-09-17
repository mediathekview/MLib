package de.mediathekview.mlib.daten;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public enum Quality
{
    HD(3, "HD"), NORMAL(2, "Normal"), SMALL(1, "Klein"), VERY_SMALL(0, "Sehr klein");

    /**
     * The bigger the index the better the quality.
     */
    private final int index;
    private final String description;

    Quality(final int aIndex, final String aDescription)
    {
        index = aIndex;
        description = aDescription;
    }

    public String getDescription()
    {
        return description;
    }

    private int getIndex()
    {
        return index;
    }

    public static List<Quality> getFromBestToLowest()
    {
        return Arrays.asList(Quality.values()).stream().sorted(Comparator.comparing(Quality::getIndex).reversed())
                .collect(Collectors.toList());
    }
}
