/*
 * SenderTest.java
 *
 * Projekt : MLib erstellt am: 02.09.2017 Autor : Sascha
 *
 * (c) 2017 by Sascha Wiegandt
 */
package de.mediathekview.mlib.daten;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import java.util.Collection;
import java.util.Optional;
import org.junit.Test;

public class SenderTest {

  @Test
  public void pruefeAnzahlBekannterSender() {
    assertEquals(22, Sender.getSenderNamen().size());
  }

  @Test
  public void pruefeGetSenderByNameMitNullString() {
    assertEquals(Optional.empty(), Sender.getSenderByName((String) null));
  }

  @Test
  public void pruefeListeBekannterSender() {
    final Collection<String> c = Sender.getSenderNamen();

    assertTrue(c.contains(Sender.ARD.getName()));
    assertTrue(c.contains(Sender.ARTE_DE.getName()));
    assertTrue(c.contains(Sender.ARTE_FR.getName()));
    assertTrue(c.contains(Sender.BR.getName()));
    assertTrue(c.contains(Sender.DREISAT.getName()));
    assertTrue(c.contains(Sender.DW.getName()));
    assertTrue(c.contains(Sender.FUNK.getName()));
    assertTrue(c.contains(Sender.HR.getName()));
    assertTrue(c.contains(Sender.KIKA.getName()));
    assertTrue(c.contains(Sender.MDR.getName()));
    assertTrue(c.contains(Sender.NDR.getName()));
    assertTrue(c.contains(Sender.ORF.getName()));
    assertTrue(c.contains(Sender.PHOENIX.getName()));
    assertTrue(c.contains(Sender.RBB.getName()));
    assertTrue(c.contains(Sender.SF.getName()));
    assertTrue(c.contains(Sender.SR.getName()));
    assertTrue(c.contains(Sender.SRF.getName()));
    assertTrue(c.contains(Sender.SRF_PODCAST.getName()));
    assertTrue(c.contains(Sender.SWR.getName()));
    assertTrue(c.contains(Sender.WDR.getName()));
    assertTrue(c.contains(Sender.ZDF.getName()));
    assertTrue(c.contains(Sender.ZDF_TIVI.getName()));
  }

  @Test
  public void pruefeNullRueckgabeBeiNichtExistentenSendernamen() {
    assertEquals(Optional.empty(), Sender.getSenderByName("thequickbrownfoxjumpsoverthelazydog"));
  }

  //

  @Test
  public void pruefeZugriffAufARDperNamensaufloesung() {
    assertEquals(Optional.of(Sender.ARD), Sender.getSenderByName("ARD"));
  }

  @Test
  public void pruefeZugriffAufARDperNamensaufloesungSchreibweiseGeaendert() {
    assertEquals(Optional.of(Sender.ARD), Sender.getSenderByName("ard"));
  }

  @Test
  public void pruefeZugriffAufPhönixPerNamensaufloesung() {
    assertEquals(Optional.of(Sender.PHOENIX), Sender.getSenderByName("PHOENIX"));
  }

}
