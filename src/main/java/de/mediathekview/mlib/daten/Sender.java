package de.mediathekview.mlib.daten;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by nicklas on 29.12.16.
 */
public enum Sender
{
    ARD("ARD"),
    ARTE_DE("ARTE.DE"),
    ARTE_FR("ARTE.FR"),
    BR("BR"),
    DREISAT("3sat"),
    DW("DW"),
    HR("HR"),
    KIKA("KIKA"),
    MDR("MDR"),
    NDR("NDR"),
    ORF("ORF"),
    PHOENIX("Phönix"),
    RBB("RBB"),
    SF("SF"),
    SR("SR"),
    SRF("SRF"),
    SRF_PODCAST("SRF.Podcast"),
    SWR("SWR"),
    WDR("WDR"),
    ZDF("ZDF"),
    ZDF_TIVI("ZDF Tivi")
    ;

    static final String EXCEPTIONTEXT_NULL_SENDERNAME = "Gesuchter Sendername ist Null!";

    private String name;

    Sender(final String aName)
    {
        name = aName;
    }

    public String getName()
    {
        return name;
    }

    public static Sender getSenderByName(final String searchedSenderName) throws IllegalArgumentException
    {
        if(null==searchedSenderName) {
            throw new IllegalArgumentException(EXCEPTIONTEXT_NULL_SENDERNAME);
        }
        
        for (final Sender sender : Sender.values())
        {
            if (sender.getName().equalsIgnoreCase(searchedSenderName) || sender.toString().equalsIgnoreCase(searchedSenderName))
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
