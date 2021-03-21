package de.mediathekview.mlib.daten;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalTime;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

/**
 * GsonDurationAdapter
 * Serialize/Deserialize java.time.Duration to string in format HH:MM:SS
 *
 */
public class GsonDurationAdapter extends TypeAdapter<Duration> {
  
    @Override
    public void write(JsonWriter out, Duration duration) throws IOException {
        if (duration == null) {
            out.nullValue();
            return;
        }
        out.value(String.format("%02d:%02d:%02d", 
            duration.toHours(), 
            duration.toMinutesPart(), 
            duration.toSecondsPart()));
    }

    @Override
    public Duration read(JsonReader in) throws IOException {
        if (in.peek() == JsonToken.NULL) {
            in.nextNull();
            return null;
        }
        return Duration.between ( 
            LocalTime.MIN,
            LocalTime.parse(in.nextString()) 
        );
        
    }
}

