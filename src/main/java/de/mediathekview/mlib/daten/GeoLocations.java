package de.mediathekview.mlib.daten;

import org.apache.commons.lang3.StringUtils;

/**
 * The available GEO locations.
 */
public enum GeoLocations
{
    GEO_NONE("","WELT","world"), // nur in .. zu sehen
    GEO_DE("DE"),
    GEO_AT("AT"),
    GEO_CH("CH"),
    GEO_EU("EU"),
    GEO_DE_FR("DE-FR"),
    GEO_DE_AT_CH("DE-AT-CH","dach"),
    GEO_DE_AT_CH_EU("DE-AT-CH-EU");

    private final String description;
    private String[] alternatives;

    GeoLocations(String aDescription,String... aAlternatives)
    {
        description = aDescription;
        alternatives = aAlternatives;
    }

    public String getDescription()
    {
        return description;
    }

    public static GeoLocations getFromDescription(String aDescription)
    {
        for (GeoLocations geoLoc : GeoLocations.values())
        {
            if (geoLoc.getDescription().equalsIgnoreCase(aDescription) || StringUtils.equalsAny(aDescription, geoLoc.alternatives))
            {
                return geoLoc;
            }
        }
        return null;
    }
}
