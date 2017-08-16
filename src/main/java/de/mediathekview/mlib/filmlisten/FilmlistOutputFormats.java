package de.mediathekview.mlib.filmlisten;

public enum FilmlistOutputFormats {
    
    JSON("Json","json"),
    COMPRESSED_JSON("Json + XZ", "xz");
    
    private String description;
    private String fileExtension;
    
    FilmlistOutputFormats(String aDescription, String aFileExtension)
    {
      description=aDescription;
      fileExtension=aFileExtension;
    }
    
    public String getDescription() {
        return description;
    }
    
    public String getFileExtension() {
        return fileExtension;
    }
}
