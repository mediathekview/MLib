package de.mediathekview.mlib.daten;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

import java.util.Optional;
import org.junit.jupiter.api.Test;

public class GeoLocationsTest {

  @Test
  public void testFindSAT() {
    Optional<GeoLocations> actual = GeoLocations.find("SAT");

    assertThat(actual.isPresent()).isEqualTo(true);
    assertThat(actual.get()).isEqualTo(GeoLocations.GEO_DE_AT_CH_EU);
  }

  @Test
  public void testFindEBU() {
    Optional<GeoLocations> actual = GeoLocations.find("EBU");

    assertThat(actual.isPresent()).isEqualTo(true);
    assertThat(actual.get()).isEqualTo(GeoLocations.GEO_DE_AT_CH_EU);
  }

  @Test
  public void testFindEBUSmall() {
    Optional<GeoLocations> actual = GeoLocations.find("ebu");

    assertThat(actual.isPresent()).isEqualTo(true);
    assertThat(actual.get()).isEqualTo(GeoLocations.GEO_DE_AT_CH_EU);
  }
}
