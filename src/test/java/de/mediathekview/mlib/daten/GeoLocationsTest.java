package de.mediathekview.mlib.daten;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.Optional;
import org.junit.Test;

public class GeoLocationsTest {

  @Test
  public void testFindSAT() {
    Optional<GeoLocations> actual = GeoLocations.find("SAT");

    assertThat(actual.isPresent(), equalTo(true));
    assertThat(actual.get(), equalTo(GeoLocations.GEO_DE_AT_CH_EU));
  }

  @Test
  public void testFindEBU() {
    Optional<GeoLocations> actual = GeoLocations.find("EBU");

    assertThat(actual.isPresent(), equalTo(true));
    assertThat(actual.get(), equalTo(GeoLocations.GEO_DE_AT_CH_EU));
  }

  @Test
  public void testFindEBUSmall() {
    Optional<GeoLocations> actual = GeoLocations.find("ebu");

    assertThat(actual.isPresent(), equalTo(true));
    assertThat(actual.get(), equalTo(GeoLocations.GEO_DE_AT_CH_EU));
  }
}
