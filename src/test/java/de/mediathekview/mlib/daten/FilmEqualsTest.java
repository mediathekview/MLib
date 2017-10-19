package de.mediathekview.mlib.daten;
import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.junit.Test;

public class FilmEqualsTest {
    @Test
    public void testEqualsUUID() throws MalformedURLException
    {
        Film testFilm1 = createTestFilm1();
        
        Film testFilm2 = new Film(UUID.randomUUID(), testFilm1.getGeoLocations(), testFilm1.getSender(), testFilm1.getTitel(), testFilm1.getThema(), 
            testFilm1.getTime(), testFilm1.getDuration(),testFilm1.getWebsite());
        testFilm2.setBeschreibung(testFilm1.getBeschreibung());
        testFilm1.getUrls().entrySet().forEach(e -> testFilm2.addUrl(e.getKey(),e.getValue()));
        
        assertThat(testFilm1,equalTo(testFilm2));
        assertThat(testFilm1.hashCode(),is(testFilm2.hashCode()));
    }
    
    @Test
    public void testEqualsGeoLocations() throws MalformedURLException
    {
        Film testFilm1 = createTestFilm1();
        List<GeoLocations> geoLocs = new ArrayList<>();
        geoLocs.add(GeoLocations.GEO_DE);
        Film testFilm2 = new Film(testFilm1.getUuid(), geoLocs, testFilm1.getSender(), testFilm1.getTitel(), testFilm1.getThema(), 
            testFilm1.getTime(), testFilm1.getDuration(),testFilm1.getWebsite());
        testFilm2.setBeschreibung(testFilm1.getBeschreibung());
        testFilm1.getUrls().entrySet().forEach(e -> testFilm2.addUrl(e.getKey(),e.getValue()));
        
        assertThat(testFilm1,equalTo(testFilm2));
        assertThat(testFilm1.hashCode(),is(testFilm2.hashCode()));
    }
    
    @Test
    public void testEqualsCopy() throws MalformedURLException
    {
        Film testFilm1 = createTestFilm1();
        
        Film testFilm2 = new Film(testFilm1.getUuid(), testFilm1.getGeoLocations(), testFilm1.getSender(), testFilm1.getTitel(), testFilm1.getThema(), 
            testFilm1.getTime(), testFilm1.getDuration(),testFilm1.getWebsite());
        testFilm2.setBeschreibung(testFilm1.getBeschreibung());
        testFilm1.getUrls().entrySet().forEach(e -> testFilm2.addUrl(e.getKey(),e.getValue()));
        
        assertThat(testFilm1,equalTo(testFilm2));
        assertThat(testFilm1.hashCode(),is(testFilm2.hashCode()));
    }
    
    @Test
    public void testEqualsTime() throws MalformedURLException
    {
        Film testFilm1 = createTestFilm1();
        
        Film testFilm2 = new Film(testFilm1.getUuid(), testFilm1.getGeoLocations(), testFilm1.getSender(), testFilm1.getTitel(), testFilm1.getThema(), 
            LocalDateTime.now(), testFilm1.getDuration(),testFilm1.getWebsite());
        testFilm2.setBeschreibung(testFilm1.getBeschreibung());
        testFilm1.getUrls().entrySet().forEach(e -> testFilm2.addUrl(e.getKey(),e.getValue()));
        
        assertThat(testFilm1,equalTo(testFilm2));
        assertThat(testFilm1.hashCode(),is(testFilm2.hashCode()));
    }
    
    @Test
    public void testEqualsWebsite() throws MalformedURLException
    {
        Film testFilm1 = createTestFilm1();
        
        Film testFilm2 = new Film(testFilm1.getUuid(), testFilm1.getGeoLocations(), testFilm1.getSender(), testFilm1.getTitel(), testFilm1.getThema(), 
            testFilm1.getTime(), testFilm1.getDuration(),new URL("http://localhost/"));
        testFilm2.setBeschreibung(testFilm1.getBeschreibung());
        testFilm1.getUrls().entrySet().forEach(e -> testFilm2.addUrl(e.getKey(),e.getValue()));
        
        assertThat(testFilm1,equalTo(testFilm2));
        assertThat(testFilm1.hashCode(),is(testFilm2.hashCode()));
    }
    
    @Test
    public void testEqualsBeschreibung() throws MalformedURLException
    {
        Film testFilm1 = createTestFilm1();
        
        Film testFilm2 = new Film(testFilm1.getUuid(), testFilm1.getGeoLocations(), testFilm1.getSender(), testFilm1.getTitel(), testFilm1.getThema(), 
            testFilm1.getTime(), testFilm1.getDuration(),testFilm1.getWebsite());
        testFilm2.setBeschreibung("testEqualsBeschreibung");
        testFilm1.getUrls().entrySet().forEach(e -> testFilm2.addUrl(e.getKey(),e.getValue()));
        
        assertThat(testFilm1,equalTo(testFilm2));
        assertThat(testFilm1.hashCode(),is(testFilm2.hashCode()));
    }
    
    @Test
    public void testEqualsUrls() throws MalformedURLException
    {
        Film testFilm1 = createTestFilm1();
        
        Film testFilm2 = new Film(testFilm1.getUuid(), testFilm1.getGeoLocations(), testFilm1.getSender(), testFilm1.getTitel(), testFilm1.getThema(), 
            testFilm1.getTime(), testFilm1.getDuration(),testFilm1.getWebsite());
        testFilm2.setBeschreibung(testFilm1.getBeschreibung());
        
        assertThat(testFilm1,equalTo(testFilm2));
        assertThat(testFilm1.hashCode(),is(testFilm2.hashCode()));
    }
    
    @Test
    public void testNotEqualsSender() throws MalformedURLException
    {
        Film testFilm1 = createTestFilm1();
        
        Film testFilm2 = new Film(testFilm1.getUuid(), testFilm1.getGeoLocations(), Sender.ARTE_FR, testFilm1.getTitel(), testFilm1.getThema(), 
            testFilm1.getTime(), testFilm1.getDuration(),testFilm1.getWebsite());
        testFilm2.setBeschreibung(testFilm1.getBeschreibung());
        testFilm1.getUrls().entrySet().forEach(e -> testFilm2.addUrl(e.getKey(),e.getValue()));
        
        assertThat(testFilm1,not(equalTo(testFilm2)));
        assertThat(testFilm1.hashCode(),not(is(testFilm2.hashCode())));
    }
    
    @Test
    public void testNotEqualsTitel() throws MalformedURLException
    {
        Film testFilm1 = createTestFilm1();
        
        Film testFilm2 = new Film(testFilm1.getUuid(), testFilm1.getGeoLocations(), testFilm1.getSender(), "testNotEqualsTitel", testFilm1.getThema(), 
            testFilm1.getTime(), testFilm1.getDuration(),testFilm1.getWebsite());
        testFilm2.setBeschreibung(testFilm1.getBeschreibung());
        testFilm1.getUrls().entrySet().forEach(e -> testFilm2.addUrl(e.getKey(),e.getValue()));
        
        assertThat(testFilm1,not(equalTo(testFilm2)));
        assertThat(testFilm1.hashCode(),not(is(testFilm2.hashCode())));
    }
    
    @Test
    public void testNotEqualsThema() throws MalformedURLException
    {
        Film testFilm1 = createTestFilm1();
        
        Film testFilm2 = new Film(testFilm1.getUuid(), testFilm1.getGeoLocations(), testFilm1.getSender(), testFilm1.getTitel(), "testNotEqualsThema", 
            testFilm1.getTime(), testFilm1.getDuration(),testFilm1.getWebsite());
        testFilm2.setBeschreibung(testFilm1.getBeschreibung());
        testFilm1.getUrls().entrySet().forEach(e -> testFilm2.addUrl(e.getKey(),e.getValue()));
        
        assertThat(testFilm1,not(equalTo(testFilm2)));
        assertThat(testFilm1.hashCode(),not(is(testFilm2.hashCode())));
    }
    
    @Test
    public void testNotEqualsDuration() throws MalformedURLException
    {
        Film testFilm1 = createTestFilm1();
        
        Film testFilm2 = new Film(testFilm1.getUuid(), testFilm1.getGeoLocations(), testFilm1.getSender(), testFilm1.getTitel(), testFilm1.getThema(), 
            testFilm1.getTime(), Duration.of(42, ChronoUnit.MINUTES),testFilm1.getWebsite());
        testFilm2.setBeschreibung(testFilm1.getBeschreibung());
        testFilm1.getUrls().entrySet().forEach(e -> testFilm2.addUrl(e.getKey(),e.getValue()));
        
        assertThat(testFilm1,not(equalTo(testFilm2)));
        assertThat(testFilm1.hashCode(),not(is(testFilm2.hashCode())));
    }
    
    public Film createTestFilm1() throws MalformedURLException
    {
        final Film testFilm1 = new Film(UUID.randomUUID(), new ArrayList<>(), Sender.ARD, "TestTitel", "TestThema",
                    LocalDateTime.parse("2017-01-01T23:55:00"), Duration.of(10, ChronoUnit.MINUTES),
                    new URL("http://www.example.org/"));
            testFilm1.setBeschreibung("Test beschreibung.");
            testFilm1.addUrl(Resolution.SMALL, new FilmUrl(new URL("http://example.org/klein.mp4"), 42l));
            testFilm1.addUrl(Resolution.NORMAL, new FilmUrl(new URL("http://example.org/Test.mp4"), 42l));
            testFilm1.addUrl(Resolution.HD, new FilmUrl(new URL("http://example.org/hd.mp4"), 42l));
        return testFilm1;
    }
}
