package de.mediathekview.mlib.compression;

public enum CompressionType {
  XZ(".xz"), GZIP(".gz");

  private String fileEnding;

  private CompressionType(final String aFileEnding) {
    fileEnding = aFileEnding;
  }

  public String getFileEnding() {
    return fileEnding;
  }
}
