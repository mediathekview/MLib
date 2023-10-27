package de.mediathekview.mlib.filmlisten.reader;

import de.mediathekview.mlib.daten.Film;
import de.mediathekview.mlib.daten.FilmUrl;
import de.mediathekview.mlib.daten.Filmlist;
import de.mediathekview.mlib.daten.GeoLocations;
import de.mediathekview.mlib.daten.Resolution;
import de.mediathekview.mlib.daten.Sender;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.DateTimeException;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;

import static java.lang.String.format;
import static java.time.format.FormatStyle.MEDIUM;

public class FilmlistOldFormatReader extends AbstractFilmlistReader {
  private static final Logger LOG = LogManager.getLogger(FilmlistOldFormatReader.class);
  private static final DateTimeFormatter DATE_FORMATTER =
      DateTimeFormatter.ofLocalizedDate(MEDIUM).withLocale(Locale.GERMANY);
  private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm[:ss]", Locale.GERMANY);
  
  private static final LocalDate DEFAULT_DATE = LocalDate.parse("01.01.1970", DATE_FORMATTER);
  private static final DateTimeFormatter FILMLIST_CREATIONDATE_PATTERN = DateTimeFormatter.ofPattern("dd.MM.yyyy, HH:mm", Locale.GERMANY);
  private static final char GEO_SPLITTERATOR = '-';
  private static final String URL_SPLITTERATOR = "\\|";
  private String sender = "";
  private String thema = "";
  private String debug = "";
  int cnt = 0;
  
  public static void main(String[] args) throws FileNotFoundException {
    new FilmlistOldFormatReader().read(new FileInputStream("C:/Users/steph/Desktop/Mediathek/oldFormat/Filmliste-akt-oldformat.json"));
  }
    
  @Override
  public Optional<Filmlist> read(InputStream aInputStream) {
    long start = System.currentTimeMillis();
    /*
    String bigFile = "C:/Users/steph/Desktop/Mediathek/oldFormat/Filmliste-akt-oldformat.json";
    String medium = "C:/Users/steph/Desktop/Mediathek/oldFormat/Filmlist - Kopie.json";
    String smallFile = "C:/Users/steph/Desktop/Mediathek/oldFormat/Filmlist.json";
    String broken = "C:/Users/steph/Desktop/Mediathek/oldFormat/Filmlist-broken.json";
    */
    Filmlist filmlist = new Filmlist();
    int cnt = 0;
    debug = "LINE " + cnt;
    //
    
    try (JsonReader jsonReader = new JsonReader(new InputStreamReader(aInputStream, StandardCharsets.UTF_8)))
    {
      headerMeta(jsonReader, filmlist);
      headerColumns(jsonReader);
      while (jsonReader.peek() != JsonToken.END_OBJECT) 
      {
        try {
          readRecrod(jsonReader).ifPresent(aFilm -> {filmlist.add(aFilm);});
        } catch (Exception e) {
          if (!recoverParser(jsonReader)) {
            System.out.println("error after " + ((System.currentTimeMillis()-start)/1000) + " on " + cnt + " elements (" + filmlist.getFilms().size()+")");
            throw(e);
          }
        }
        cnt++;
      }
    } catch (IOException e) {
      LOG.error(e);
      return Optional.of(filmlist);
    }
    LOG.debug("done reading in " + ((System.currentTimeMillis()-start)/1000) + "sec for " + cnt + " elements (" + filmlist.getFilms().size()+")");
    return Optional.of(filmlist);
    
  }
  
  private boolean recoverParser(JsonReader jsonReader) {
    int maxTry = 25;
    try {
      while (maxTry > 0 && !JsonToken.END_ARRAY.equals(jsonReader.peek())) {
        jsonReader.nextString();
        maxTry--;
      }
      jsonReader.endArray();
      return true;
    } catch (Exception e) {
      // TODO: handle exception
    }
    return false;
  }
  
  
  private void headerMeta(JsonReader jsonReader, Filmlist filmlist) throws IOException {
    jsonReader.beginObject();
    jsonReader.nextName();
    jsonReader.beginArray();
    filmlist.setCreationDate(readHeader01CreationDate(jsonReader.nextString())); // localdatetime
    jsonReader.nextString(); // localdatetime UTC
    readHeader03Version(jsonReader.nextString()); // Version
    jsonReader.nextString(); // Version
    filmlist.setListId(readHeader05Hash(jsonReader.nextString())); // hash
    jsonReader.endArray();
    
  }
  
  private LocalDateTime readHeader01CreationDate(String in) {
    if (StringUtils.isNotBlank(in)) {
      try {
        return LocalDateTime.parse(in, FILMLIST_CREATIONDATE_PATTERN);
      } catch (DateTimeParseException e) {
        LOG.warn(format("Error readHeader01CreationDate format string %s on line %s throws %s", in, debug, e ));
      }
    }
    return LocalDateTime.now();
  }
  private String readHeader03Version(String in)  {
    return in;
  }
  private UUID readHeader05Hash(String in) {
    try {
      return UUID.fromString(in);
    } catch (Exception e) {
      LOG.warn("Error readHeader05Hash format string " + in);
    }
    return UUID.randomUUID();
  }
  
  private void headerColumns(JsonReader jsonReader) throws IOException {
    jsonReader.nextName();
    jsonReader.beginArray();
    jsonReader.nextString(); // Sender
    jsonReader.nextString(); // Thema
    jsonReader.nextString(); // Titel
    jsonReader.nextString(); // Datum
    jsonReader.nextString(); // Zeit
    jsonReader.nextString(); // Dauer
    jsonReader.nextString(); // Größe [MB]
    jsonReader.nextString(); // Beschreibung
    jsonReader.nextString(); // Url
    jsonReader.nextString(); // Website
    jsonReader.nextString(); // Url Untertitel
    jsonReader.nextString(); // Url RTMP
    jsonReader.nextString(); // Url Klein
    jsonReader.nextString(); // Url RTMP Klein
    jsonReader.nextString(); // Url HD
    jsonReader.nextString(); // Url RTMP HD
    jsonReader.nextString(); // DatumL
    jsonReader.nextString(); // Url History
    jsonReader.nextString(); // Geo
    jsonReader.nextString(); // neu
    jsonReader.endArray();
  };
  
  private Optional<Film> readRecrod(JsonReader jsonReader) throws IOException {
    cnt++;
    //
    Film f = new Film();
    f.setUuid(UUID.randomUUID());
    //    
    jsonReader.nextName();
    jsonReader.beginArray();
    sender = readRecord01Sender(jsonReader.nextString(), sender);
    f.setSender(Sender.getSenderByName(sender).get());
    //
    thema = readRecord02Thema(jsonReader.nextString(), thema);
    f.setThemaRaw(thema);
    //
    String titel = readRecord03Titel(jsonReader.nextString());
    f.setTitelRaw(titel);
    debug = sender + thema + titel;
    //
    f.setTime(readRecord04Datum(jsonReader.nextString()).atTime(readRecord05Zeit(jsonReader.nextString())));
    //
    f.setDuration(readRecord06Dauer(jsonReader.nextString()));
    //
    long size = readRecord07Groesse(jsonReader.nextString());
    //
    f.setBeschreibungRaw(readRecord08Beschreibung(jsonReader.nextString()));
    //
    URL urlNormal = readRecord09Url(jsonReader.nextString());
    //
    f.setWebsite(readRecord10Website(jsonReader.nextString()));
    //
    f.addSubtitle(readRecord11Untertitel(jsonReader.nextString()));
    //
    readRecord12UrlRTMP(jsonReader.nextString());
    //
    String urlKlein = readRecord13UrlKlein(jsonReader.nextString());
    //
    readRecord14UrlRTMPKlein(jsonReader.nextString());
    //
    String urlHd = readRecord15UrlHD(jsonReader.nextString());
    //
    readRecord16UrlRTMPHD(jsonReader.nextString());
    //
    readRecord17DatumL(jsonReader.nextString());
    //
    readRecord18UrlHistory(jsonReader.nextString());
    //
    f.setGeoLocations(readRecord19Geo(jsonReader.nextString()));
    //
    f.setNeu(readRecord20Neu(jsonReader.nextString()));
    //
    jsonReader.endArray();
    //
    f.setUrls(generateUrls(urlNormal, urlKlein, urlHd, size));
    //
    if (f.getUrls().size() > 0) {
      return Optional.of(f);
    } else {
      LOG.warn(format("Error no urls for film %s", debug));
      return Optional.empty();
    }
  }
  
  
  private Map<Resolution,FilmUrl> generateUrls(URL urlNormal, String urlSmall, String urlHd, long size) {
    Map<Resolution,FilmUrl> urls = new HashMap<>();
    if (urlNormal != null) {
      urls.put(Resolution.NORMAL, new FilmUrl(urlNormal, size));
      rebuildUrl(urlNormal, urlSmall).ifPresent( u -> {
        urls.put(Resolution.SMALL, new FilmUrl(u, 0L));  
      });
      rebuildUrl(urlNormal, urlHd).ifPresent( u -> {
        urls.put(Resolution.HD, new FilmUrl(u, 0L));  
      });
    }
    return urls;
  }
  
  private Optional<URL> rebuildUrl(URL urlNromal, String targetUrl) {
    if (!targetUrl.isBlank()) {
      try {
        final String[] splittedUrlText = targetUrl.split(URL_SPLITTERATOR);
        if (splittedUrlText.length == 2) {
          final int lengthOfOld = Integer.parseInt(splittedUrlText[0]);
          return Optional.of(new URL(urlNromal.toString().substring(0, lengthOfOld) + splittedUrlText[1]));
        }
        return Optional.of(new URL(targetUrl));
      } catch (Exception e) {
        LOG.warn(format("Error rebuildUrl format string %s on line %s throws %s", targetUrl, debug, e ));
      }
    }
    return Optional.empty();
  }
  
  
  
  ////////////////////////////////////////////////////////////

  protected String readRecord01Sender(String in, String sender) {
    if (!in.isBlank()) {
      sender = in;
    }
    return sender;
  }
  
  protected String readRecord02Thema(String in, String thema) {
    if (!in.isBlank()) {
      thema = in;
    }
    return thema;
  }
  
  protected String readRecord03Titel(String in) {
    return in;
  }
  
  protected LocalDate readRecord04Datum(String in) {
    if (StringUtils.isNotBlank(in)) {
      try {
        return LocalDate.parse(in, DATE_FORMATTER);
      } catch (DateTimeParseException e) {
        LOG.warn(format("Error readRecord04Datum format string %s on line %s throws %s", in, debug, e ));
      }
    }
    return DEFAULT_DATE;
  }
  
  protected LocalTime readRecord05Zeit(String in) {
      if (StringUtils.isNotBlank(in)) {
        try {
          return LocalTime.parse(in, TIME_FORMATTER);
        } catch (DateTimeParseException e) {
          LOG.warn(format("Error readRecord05Zeit format string %s on line %s throws %s", in, debug, e ));
        }
      }
      return LocalTime.MIDNIGHT;
  }
  
  protected Duration readRecord06Dauer(String in) {
    if (StringUtils.isNotBlank(in)) {
      try {
        return Duration.between(LocalTime.MIDNIGHT, LocalTime.parse(in));
      } catch (DateTimeException | ArithmeticException e) {
        LOG.info(format("Error readRecord06Dauer format string %s on line %s throws %s", in, debug, e ));
      }
    }
    return Duration.ZERO;
  }
  
  protected long readRecord07Groesse(String in) {
    if (StringUtils.isNotBlank(in)) {
      try {
        return Long.parseLong(in)*1024; // oldFilmlist format is MB - new DM is KB
      } catch (NumberFormatException e) {
        LOG.warn(format("Error readRecord07Groesse format string %s on line %s throws %s", in, debug, e ));
      }
    }
    return 0L;
  }

  protected String readRecord08Beschreibung(String in) {
    return in;
  }
  
  protected URL readRecord09Url(String in) {
    if (!in.isBlank()) {
      try {
        return new URL(in); 
      } catch (final MalformedURLException e) {
        LOG.warn(format("Error readRecord09Url format string %s on line %s throws %s", in, debug, e ));
      }
    }
    return null;
  }
  
  protected URL readRecord10Website(String in) {
    if (!in.isBlank() && in.startsWith("http")) {
      try {
        return new URL(in); 
      } catch (final MalformedURLException e) {
        LOG.warn(format("Error readRecord10Website format string %s on line %s throws %s", in, debug, e ));
      }
    }
    return null;
  }
  
  protected URL readRecord11Untertitel(String in) {
    if (!in.isBlank() && in.startsWith("http")) {
      try {
        return new URL(in);
      } catch (final MalformedURLException e) {
        LOG.warn(format("Error readRecord11Untertitel format string %s on line %s throws %s", in, debug, e ));
      }
    }
    return null;
  }
  
  protected String readRecord12UrlRTMP(String in) {
    return in;
  }
  
  protected String readRecord13UrlKlein(String in) {
    return in;
  }
  
  protected String readRecord14UrlRTMPKlein(String in) {
    return in;
  }
  
  protected String readRecord15UrlHD(String in) {
    return in;
  }
  
  protected String readRecord16UrlRTMPHD(String in) {
    return in;
  }
  
  protected String readRecord17DatumL(String in) {
    return in;
  }
  
  protected String readRecord18UrlHistory(String in) {
    return in;
  }
  
  protected Collection<GeoLocations> readRecord19Geo(String in) {
    final Collection<GeoLocations> geoLocations = new ArrayList<>();

    final GeoLocations singleGeoLocation = GeoLocations.getFromDescription(in);
    if (singleGeoLocation == GeoLocations.GEO_NONE) {
      for (final String geoText : in.split(String.valueOf(GEO_SPLITTERATOR))) {
        final GeoLocations geoLocation = GeoLocations.getFromDescription(geoText);
        if (geoLocation != GeoLocations.GEO_NONE) {
          geoLocations.add(geoLocation);
        }
      }
    } else {
      geoLocations.add(singleGeoLocation);
    }

    return geoLocations; 
  }

  protected Boolean readRecord20Neu(String in) {
    return Boolean.parseBoolean(in);
  }


  
  
}
