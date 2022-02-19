package de.mediathekview.mlib.filmlisten.reader;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class RawFilm {

  String sender;
  String thema;
  String titel;
  String datum;
  String zeit;
  String dauer;
  String groesseMb;
  String beschreibung;
  String url;
  String website;
  String urlUntertitel;
  String urlKlein;
  String urlHd;
  String geo;
  String neu;
}
