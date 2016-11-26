package mSearch.daten;

/**
 * A enum which contains the available video resolutions.
 */
public enum Aufloesungen
{
    NORMAL("normal"),
    HD("hd"),
    KLEIN("klein");

    private String description;

    Aufloesungen(String aDescription)
    {
        description = aDescription;
    }

    public String getDescription()
    {
        return description;
    }
}
