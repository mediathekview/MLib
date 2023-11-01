package de.mediathekview.mlib.filmlisten;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.mediathekview.mlib.daten.Filmlist;
import de.mediathekview.mlib.filmlisten.reader.FilmlistOldFormatReader;
import de.mediathekview.mlib.filmlisten.writer.FilmlistOldFormatWriter;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;


import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class FilmlistOldFormatWriterTest {
  @TempDir
  Path tempDir;
  
  @Test
  void readFilmlistOldFormatIncludingBrokenRecords()
      throws IOException {
    ClassLoader classLoader = getClass().getClassLoader();
    final Path testFilePath = new File(classLoader.getResource("TestFilmlistOldFormatWriter.json").getFile()).toPath();
    Optional<Filmlist> testFilmlist = new FilmlistOldFormatReader().read(new FileInputStream(testFilePath.toString()));
    assertTrue( testFilmlist.isPresent());
    //
    Path tempFile = Files.createTempFile(tempDir, "TestFilmlistOldFormatWriter", ".out");
    new FilmlistOldFormatWriter().write(testFilmlist.get(), tempFile);
    
    assertTrue( Files.exists(tempFile));
    //
    String actualData = Files.readString(tempFile, StandardCharsets.UTF_16).substring(100);
    String expectedData = Files.readString(testFilePath, StandardCharsets.UTF_16).substring(100);
    assertEquals(expectedData, actualData, "Filmlisten stimmen überein.");
    //
    Files.deleteIfExists(tempFile);

  }
  
  
  
}
