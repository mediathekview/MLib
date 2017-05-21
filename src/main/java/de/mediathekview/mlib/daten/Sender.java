package de.mediathekview.mlib.daten;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

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
    SRF_PODCAST("SRF Podcast"),
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
    SRF("SRF"),
    WDR("WDR");

    private String name;

    Sender(String aName)
    {
        name = aName;
    }

    public String getName()
    {
        return name;
    }

    public static Sender getSenderByName(String aName)
    {
        for (Sender sender : Sender.values())
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
        Collection<String> senderNamen = new ArrayList<>();

        for (Sender sender : Sender.values())
        {
            senderNamen.add(sender.getName());
        }

        return senderNamen;
    }
}
