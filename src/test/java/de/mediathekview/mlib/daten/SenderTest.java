/*
 * SenderTest.java
 *
 * Projekt : MLib erstellt am: 02.09.2017 Autor : Sascha
 *
 * (c) 2017 by Sascha Wiegandt
 */
package de.mediathekview.mlib.daten;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

import org.junit.jupiter.api.Test;

public class SenderTest {

  @Test
  public void pruefeAnzahlBekannterSender() {
    assertThat(Sender.getSenderNamen())
        .hasSize(33);
  }

  @Test
  public void pruefeGetSenderByNameMitNullString() {
    assertThat(Sender.getSenderByName(null))
        .isEmpty();
  }

  @Test
  public void pruefeListeBekannterSender() {
    assertThat(Sender.getSenderNamen())
        .contains(
            Sender.ARD.getName(),
            Sender.ARTE_DE.getName(),
            Sender.ARTE_FR.getName(),
            Sender.ARTE_PL.getName(),
            Sender.ARTE_EN.getName(),
            Sender.ARTE_ES.getName(),
            Sender.ARTE_IT.getName(),
            Sender.BR.getName(),
            Sender.DREISAT.getName(),
            Sender.DW.getName(),
            Sender.FUNK.getName(),
            Sender.HR.getName(),
            Sender.KIKA.getName(),
            Sender.MDR.getName(),
            Sender.NDR.getName(),
            Sender.ORF.getName(),
            Sender.PHOENIX.getName(),
            Sender.RBB.getName(),
            Sender.SF.getName(),
            Sender.SR.getName(),
            Sender.SRF.getName(),
            Sender.SRF_PODCAST.getName(),
            Sender.SWR.getName(),
            Sender.WDR1_LIVE.getName(),
            Sender.WDR2.getName(),
            Sender.WDR3.getName(),
            Sender.WDR4.getName(),
            Sender.WDR5.getName(),
            Sender.WDR_COSMO.getName(),
            Sender.WDR_KIRAKA.getName(),
            Sender.WDR.getName(),
            Sender.ZDF.getName(),
            Sender.ZDF_TIVI.getName()
        );

  }

  @Test
  public void pruefeNullRueckgabeBeiNichtExistentenSendernamen() {
    assertThat(Sender.getSenderByName("thequickbrownfoxjumpsoverthelazydog")).isEmpty();
  }

  @Test
  public void pruefeZugriffAufARDperNamensaufloesung() {
    assertThat(Sender.getSenderByName("ARD")).contains(Sender.ARD);
  }

  @Test
  public void pruefeZugriffAufARDperNamensaufloesungSchreibweiseGeaendert() {
    assertThat(Sender.getSenderByName("ard")).contains(Sender.ARD);
  }

  @Test
  public void pruefeZugriffAufPhoenixPerNamensaufloesung() {
    assertThat(Sender.getSenderByName("PHOENIX")).contains(Sender.PHOENIX);
  }
}