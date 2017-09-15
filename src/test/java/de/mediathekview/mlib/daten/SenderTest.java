/*
 * SenderTest.java
 * 
 * Projekt    : MLib
 * erstellt am: 02.09.2017
 * Autor      : Sascha
 * 
 * (c) 2017 by Sascha Wiegandt
 */
package de.mediathekview.mlib.daten;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Collection;
import java.util.Optional;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class SenderTest {

    @Rule
    public ExpectedException exception = ExpectedException.none();
    
    @Test
    public void pruefeAnzahlBekannterSender() {
        assertEquals(21, Sender.getSenderNamen().size());
    }
    
    @Test
    public void pruefeZugriffAufARDperNamensaufloesungSchreibweiseGeaendert() {
        assertEquals(Optional.of(Sender.ARD), Sender.getSenderByName("ard"));
    }
    
    @Test
    public void pruefeZugriffAufARDperNamensaufloesung() {
        assertEquals(Optional.of(Sender.ARD), Sender.getSenderByName("ARD"));
    }
    
    @Test
    public void pruefeGetSenderByNameMitNullString() {
        exception.expect(IllegalArgumentException.class);
        exception.expectMessage(Sender.EXCEPTIONTEXT_NULL_SENDERNAME);
        assertEquals(Optional.empty(), Sender.getSenderByName((String)null));
    }

    @Test
    public void pruefeNullRueckgabeBeiNichtExistentenSendernamen() {
        assertEquals(Optional.empty(), Sender.getSenderByName("thequickbrownfoxjumpsoverthelazydog"));
    }
    
    @Test
    public void pruefeListeBekannterSender() {
        Collection<String> c = Sender.getSenderNamen();
        
        assertTrue(c.contains(Sender.ARD.getName()));
        assertTrue(c.contains(Sender.ARTE_DE.getName()));
        assertTrue(c.contains(Sender.ARTE_FR.getName()));
        assertTrue(c.contains(Sender.BR.getName()));
        assertTrue(c.contains(Sender.DREISAT.getName()));
        assertTrue(c.contains(Sender.DW.getName()));
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

}
