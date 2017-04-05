package de.mediathekview.mlib.filmlisten;

import com.google.gson.*;
import de.mediathekview.mlib.daten.Film;
import de.mediathekview.mlib.daten.Sender;

import java.lang.reflect.Type;
import java.net.URI;
import java.time.Duration;
import java.util.*;

/**
 * Created by nicklas on 05.04.17.
 */
public class FakeJsonDeserializer implements JsonDeserializer<List<Film>>
{
    @Override
    public List<Film> deserialize(final JsonElement aJsonElement, final Type aTypeOfT, final JsonDeserializationContext aContext) throws JsonParseException
    {
        List<Film> filmList = new ArrayList<>();

        final Set<Map.Entry<String, JsonElement>> entries = aJsonElement.getAsJsonObject().entrySet();
        JsonArray baseInfo = entries.iterator().next().getValue().getAsJsonArray();

        Film filmEntryBefore = null;
        for(Map.Entry<String,JsonElement> entry : entries)
        {
            if("X".equals(entry.getKey()))
            {
                JsonArray filmArry = entry.getValue().getAsJsonArray();
                filmEntryBefore = arrayToFilm(filmArry,filmEntryBefore);
                filmList.add(filmEntryBefore);
            }
        }
        return filmList;
    }

    private Film arrayToFilm(final JsonArray aFilmArry, Film aFilmEntryBefore)
    {
        try
        {
            String senderText = aFilmArry.get(0).getAsString();
            Sender sender;
            if(senderText.isEmpty() && aFilmEntryBefore!=null)
            {
                sender = aFilmEntryBefore.getSender();
            }else {
                sender = Sender.valueOf(senderText);
            }

            String thema = aFilmArry.get(1).getAsString();
            if(thema.isEmpty() && aFilmEntryBefore!=null)
            {
                thema=aFilmEntryBefore.getThema();
            }

            String titel = aFilmArry.get(2).getAsString();
            if(titel.isEmpty() && aFilmEntryBefore!=null)
            {
                titel=aFilmEntryBefore.getTitel();
            }

            Duration dauer = Duration.parse(aFilmArry.get(5).getAsString());
            long groesse = Long.parseLong(aFilmArry.get(6).getAsString());
            String beschreibung = aFilmArry.get(7).getAsString();

            URI urlNormal = new URI(aFilmArry.get(8).getAsString());
            URI urlWebseite = new URI(aFilmArry.get(9).getAsString());

        }catch(Exception exception)
        {
            //TODO
        }
        return null;
    }
}
