package de.mediathekview.mlib.daten;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;

/**
 * A enum of the possible sender.
 *
 * @author Nicklas Wiegandt (Nicklas2751)<br>
 *         <b>Mail:</b> nicklas@wiegandt.eu<br>
 *         <b>Jabber:</b> nicklas2751@elaon.de<br>
 *         <b>Riot.im:</b> nicklas2751:matrix.elaon.de<br>
 *
 */
public enum Sender {
  ARD("ARD"),
  ARTE_DE("ARTE.DE", "ARTEDE", "ARTE_DE"),
  ARTE_FR("ARTE.FR", "ARTEFR", "ARTE_FR"),
  BR("BR"),
  DREISAT("3sat"),
  DW("DW"),
  FUNK("Funk.net"),
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
  WDR1_LIVE("WDR 1LIVE"),
  WDR2("WDR 2"),
  WDR3("WDR 3"),
  WDR4("WDR 4"),
  WDR5("WDR 5"),
  WDR_COSMO("WDR COSMO"),
  WDR_KIRAKA("WDR KIRAKA"),
  ZDF("ZDF"),
  ZDF_TIVI("ZDF Tivi", "ZDF-tivi");

  private String name;
  private String[] nameAlternatives;

  Sender(final String aName, final String... aNameAlternatives) {
    name = aName;
    nameAlternatives = aNameAlternatives;
  }

  public static Optional<Sender> getSenderByName(final String searchedSenderName) {
    for (final Sender sender : Sender.values()) {
      if (sender.getName().equalsIgnoreCase(searchedSenderName)
          || sender.name().equalsIgnoreCase(searchedSenderName)
          || Arrays.stream(sender.nameAlternatives)
              .anyMatch(n -> n.equalsIgnoreCase(searchedSenderName))) {
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