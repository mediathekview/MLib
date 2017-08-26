package de.mediathekview.mlib.daten;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by nicklas on 29.12.16.
 */
public enum Sender
{
    BR("BR"),
    MDR("MDR"),
    SWR("SWR"),
    ZDF("ZDF"),
    ZDF_TIVI("ZDF Tivi"),
    DREISAT("3sat"),
    ORF("ORF"),
    SRF_PODCAST("SRF.Podcast"),
    NDR("NDR"),
    KIKA("KIKA"),
    ARD("ARD"),
    ARTE_DE("ARTE.DE"),
    ARTE_FR("ARTE.FR"),
    DW("DW"),
    HR("HR"),
    PHOENIX("Phönix"),
    RBB("RBB"),
    SR("SR"),
    SF("SF"),
    SRF("SRF"),
    WDR("WDR");

    private String name;

    Sender(final String aName)
    {
        name = aName;
    }

    public String getName()
    {
        return name;
    }

    public static Sender getSenderByName(final String aName)
    {
        for (final Sender sender : Sender.values())
        {
            if (sender.getName().equalsIgnoreCase(aName) || sender.toString().equalsIgnoreCase(aName))
            {
                return sender;
            }
        }

        return null;
    }

    public static Collection<String> getSenderNamen()
    {
        final Collection<String> senderNamen = new ArrayList<>();

        for (final Sender sender : Sender.values())
        {
            senderNamen.add(sender.getName());
        }

        return senderNamen;
    }
}
