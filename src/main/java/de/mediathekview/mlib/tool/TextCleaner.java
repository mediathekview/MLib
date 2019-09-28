package de.mediathekview.mlib.tool;

import org.apache.commons.text.StringEscapeUtils;

public class TextCleaner {
  private static final String HTML_TAG_REGEX = "<.*?>";
  private static final String TAB = "\r";
  private static final String NEW_LINE = "\n";
  private static final int MAX_BESCHREIBUNG = 400;
  private static final String[] GERMAN_GEOBLOCKING_TEXTS = {
    "+++ Aus rechtlichen Gründen ist der Film nur innerhalb von Deutschland abrufbar. +++",
    "+++ Aus rechtlichen Gründen ist diese Sendung nur innerhalb von Deutschland abrufbar. +++",
    "+++ Aus rechtlichen Gründen ist dieses Video nur innerhalb von Deutschland abrufbar. +++",
    "+++ Aus rechtlichen Gründen ist dieses Video nur innerhalb von Deutschland verfügbar. +++",
    "+++ Aus rechtlichen Gründen kann das Video nur innerhalb von Deutschland abgerufen werden. +++ Due to legal reasons the video is only available in Germany.+++",
    "+++ Aus rechtlichen Gründen kann das Video nur innerhalb von Deutschland abgerufen werden. +++",
    "+++ Due to legal reasons the video is only available in Germany.+++",
    "+++ Aus rechtlichen Gründen kann das Video nur in Deutschland abgerufen werden. +++"
  };

  private TextCleaner() {
    super();
  }

  private static String removeHtmlTags(final String text) {
    return text.replaceAll(HTML_TAG_REGEX, "");
  }

  private static String unescape(final String text) {
    String unescapedText;
    unescapedText = StringEscapeUtils.unescapeXml(text);
    unescapedText = StringEscapeUtils.unescapeHtml4(unescapedText);
    unescapedText = StringEscapeUtils.unescapeJava(unescapedText);
    unescapedText = unescapedText.replaceAll(TAB, " ").trim();
    unescapedText = unescapedText.replaceAll(NEW_LINE, " ").trim();
    return unescapedText;
  }

  public static String clean(final String text) {
    return unescape(removeHtmlTags(text));
  }

  private static String cleanGeoBlockingTexts(final String text) {
    String textTemp = text;
    for (final String geoBlockingText : GERMAN_GEOBLOCKING_TEXTS) {
      if (text.contains(geoBlockingText)) {
        textTemp = textTemp.replace(geoBlockingText, ""); // steht
        // auch
        // mal
        // in
        // der
        // Mitte
      }
    }
    return textTemp;
  }

  private static String shortenBeschreibung(final String text) {
    if (text.length() > MAX_BESCHREIBUNG) {
      return text.substring(0, MAX_BESCHREIBUNG) + "\n.....";
    }
    return text;
  }

  private static String beschreibungCleanUp(
      final String text, final String titel, final String thema) {
    String textTemp = text;
    if (textTemp.startsWith(titel)) {
      textTemp = textTemp.substring(titel.length()).trim();
    }
    if (textTemp.startsWith(thema)) {
      textTemp = textTemp.substring(thema.length()).trim();
    }
    if (textTemp.startsWith("|")) {
      textTemp = textTemp.substring(1).trim();
    }
    if (textTemp.startsWith("Video-Clip")) {
      textTemp = textTemp.substring("Video-Clip".length()).trim();
    }
    if (textTemp.startsWith(titel)) {
      textTemp = textTemp.substring(titel.length()).trim();
    }
    if (textTemp.startsWith(":")) {
      textTemp = textTemp.substring(1).trim();
    }
    if (textTemp.startsWith(",")) {
      textTemp = textTemp.substring(1).trim();
    }
    if (textTemp.startsWith("\n")) {
      textTemp = textTemp.substring(1).trim();
    }
    if (textTemp.contains("\\\"")) { // wegen " in json-Files
      textTemp = textTemp.replace("\\\"", "\"");
    }
    return cleanGeoBlockingTexts(textTemp);
  }

  public static String shortAndCleanBeschreibung(
      final String beschreibung, final String titel, final String thema) {
    return shortenBeschreibung(beschreibungCleanUp(clean(beschreibung), titel, thema));
  }
}
