package de.mediathekview.mlib.filmlisten;

import java.util.Optional;

public enum FilmlistFormats
{

    JSON("Json", "json"),
    OLD_JSON("Old Json", "json"),
    JSON_COMPRESSED("Json + XZ", "xz"),
    OLD_JSON_COMPRESSED("Old Json compressed", "xz");

    private String description;
    private String fileExtension;

    FilmlistFormats(final String aDescription, final String aFileExtension)
    {
        description = aDescription;
        fileExtension = aFileExtension;
    }

    public String getDescription()
    {
        return description;
    }

    public String getFileExtension()
    {
        return fileExtension;
    }

    public static Optional<FilmlistFormats> getByDescription(final String aDescription)
    {
        for (final FilmlistFormats format : values())
        {
            if (format.getDescription().equals(aDescription))
            {
                return Optional.of(format);
            }
        }
        return Optional.empty();
    }
}
