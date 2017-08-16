package de.mediathekview.mlib.filmlisten;

import java.net.URL;
import java.nio.file.Path;

import de.mediathekview.mlib.daten.Filmlist;

public class FilmlistManager {
    private static FilmlistManager instance;
    public static FilmlistManager getInstance()
    {
        if(instance==null)
        {
            instance = new FilmlistManager();
        }
        return instance;
    }
    
    private FilmlistManager()
    {
        super();
    }
    
    public boolean save(FilmlistOutputFormats aFormat,Filmlist aFilmlist, Path aSavePath)
    {
        switch(aFormat)
        {
            case JSON:
                //TODO
            break;
            
            case COMPRESSED_JSON:
                //TODO
            break;
            
            default:
            return false;
        }
       return true;
    }
    
    public Filmlist importList(Path aFilePath)
    {
        //TODO
        return null;
    }
    
    public Filmlist importList(URL aUrl)
    {
        //TODO
        return null;
    }
    
    
    
}
