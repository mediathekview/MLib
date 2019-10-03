/*
 * ResolutionTest.java
 *
 * Projekt    : MLib
 * erstellt am: 18.09.2017
 * Autor      : Sascha
 *
 * (c) 2017 by Sascha Wiegandt
 */
package de.mediathekview.mlib.daten;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

import java.util.List;
import java.util.NoSuchElementException;
import org.junit.jupiter.api.Test;

public class ResolutionTest {

  @Test
  public void testGetHigherResolutionThanVerySmall() {
    assertThat(Resolution.getNextHigherResolution(Resolution.VERY_SMALL)).isEqualTo(Resolution.SMALL);
  }

  @Test
  public void testGetHigherResolutionThanSmall() {
    assertThat(Resolution.getNextHigherResolution(Resolution.SMALL)).isEqualTo(Resolution.NORMAL);
  }

  @Test
  public void testGetHigherResolutionThanNormal() {
    assertThat(Resolution.getNextHigherResolution(Resolution.NORMAL)).isEqualTo(Resolution.HD);
  }

  @Test
  public void tryGettingHigherResolutionThanUhdWithStopAtUhd() {
    assertThat(Resolution.getNextHigherResolution(Resolution.UHD)).isEqualTo(Resolution.UHD);
  }

  @Test
  public void tryGettingHigherResolutionThanWqhd() {
    assertThat(Resolution.getNextHigherResolution(Resolution.WQHD)).isEqualTo(Resolution.UHD);
  }

  @Test
  public void tryGettingHigherResolutionThanHd() {
    assertThat(Resolution.getNextHigherResolution(Resolution.HD)).isEqualTo(Resolution.WQHD);
  }

  @Test
  public void testGetLowerResolutionThanHd() {
    assertThat(Resolution.getNextLowerResolution(Resolution.HD)).isEqualTo(Resolution.NORMAL);
  }

  @Test
  public void testGetLowerResolutionThanNormal() {
    assertThat(Resolution.getNextLowerResolution(Resolution.NORMAL)).isEqualTo(Resolution.SMALL);
  }

  @Test
  public void testGetLowerResolutionThanSmall() {
    assertThat(Resolution.getNextLowerResolution(Resolution.SMALL)).isEqualTo(Resolution.VERY_SMALL);
  }

  @Test
  public void tryGettingLowerResolutionThanVerySmallWithStopAtVerySmall() {
    assertThat(Resolution.getNextLowerResolution(Resolution.VERY_SMALL)).isEqualTo(Resolution.VERY_SMALL);
  }

  @Test
  public void testResolutionTextVerySmall() {
    assertThat(Resolution.VERY_SMALL.getDescription()).isEqualTo("Sehr klein");
  }

  @Test
  public void testResolutionTextSmall() {
    assertThat(Resolution.SMALL.getDescription()).isEqualTo("Klein");
  }

  @Test
  public void testResolutionTextNormal() {
    assertThat(Resolution.NORMAL.getDescription()).isEqualTo("Normal");
  }

  @Test
  public void testResolutionTextHd() {
    assertThat(Resolution.HD.getDescription()).isEqualTo("HD");
  }

  @Test
  public void testGetReversedListOfResoltions() {
    final List<Resolution> descendingList = Resolution.getFromBestToLowest();

    assertThat(descendingList)
        .hasSize(6)
        .containsSequence(Resolution.UHD,
            Resolution.WQHD,
            Resolution.HD,
            Resolution.NORMAL,
            Resolution.SMALL,
            Resolution.VERY_SMALL);

  }

  @Test
  public void testGetHighestResolution() {
    assertThat(Resolution.getHighestResolution()).isEqualTo(Resolution.UHD);
  }

  @Test
  public void testGetLowestResolution() {
    assertThat(Resolution.getLowestResolution()).isEqualTo(Resolution.VERY_SMALL);
  }

  @Test
  public void testGetUnknownResoultionByResolutionSize() {
    assertThatExceptionOfType(NoSuchElementException.class)
        .isThrownBy(() -> { Resolution.getResoultionByResolutionSize(42); })
        .withMessage("Resolution with ResolutionIndex 42 not found");
  }
}
