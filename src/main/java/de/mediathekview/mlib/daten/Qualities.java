package de.mediathekview.mlib.daten;

public enum Qualities
{
    HD("HD"), NORMAL("Normal"), SMALL("Klein"), VERY_SMALL("Sehr klein");

    private final String description;

    Qualities(String aDescription)
    {
        description = aDescription;
    }

    public String getDescription()
    {
        return description;
    }
}
