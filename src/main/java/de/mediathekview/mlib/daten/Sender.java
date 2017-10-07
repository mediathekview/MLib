package de.mediathekview.mlib.daten;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;

/**
 * A enum of the possible sender.
 *
 * @author Nicklas Wiegandt (Nicklas2751)<br/>
 *         <b>Mail:</b> nicklas@wiegandt.eu<br/>
 *         <b>Jabber:</b> nicklas2751@elaon.de<br/>
 *         <b>Skype:</b> Nicklas2751<br/>
 *
 */
public enum Sender {
  ARD("ARD"), ARTE_DE("ARTE.DE"), ARTE_FR("ARTE.FR"), BR("BR"), DREISAT("3sat"), DW("DW"), HR(
      "HR"), KIKA("KIKA"), MDR("MDR"), NDR("NDR"), ORF("ORF"), PHOENIX("Phönix"), RBB("RBB"), SF(
          "SF"), SR("SR"), SRF("SRF"), SRF_PODCAST(
              "SRF.Podcast"), SWR("SWR"), WDR("WDR"), ZDF("ZDF"), ZDF_TIVI("ZDF Tivi");

  private String name;

  Sender(final String aName) {
    name = aName;
  }

  public static Optional<Sender> getSenderByName(final String searchedSenderName) {
    for (final Sender sender : Sender.values()) {
      if (sender.getName().equalsIgnoreCase(searchedSenderName)
          || sender.name().equalsIgnoreCase(searchedSenderName))

      {
        return Optional.of(sender);
      }
    }

    return Optional.empty();
  }

  public static Collection<String> getSenderNamen() {
    final Collection<String> senderNamen = new ArrayList<>();

    for (final Sender sender : Sender.values()) {
      senderNamen.add(sender.getName());
    }

    return senderNamen;
  }

  public String getName() {
    return name;
  }
}
