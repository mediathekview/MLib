package de.mediathekview.mlib.filmlisten;

public class FilmlistenManager
{
    private static FilmlistenManager instance;
    public FilmlistenManager getInstance()
    {
        if(instance==null)
        {
            instance=new FilmlistenManager();
        }
        return instance;
    }

    private FilmlistenManager()
    {

    }


}
