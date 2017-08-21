package de.mediathekview.mlib.progress;

import de.mediathekview.mlib.daten.Sender;

/**
 * A abstract Sender specific listener for listeners which get progress updates.
 */
public interface SenderProgressListener
{
    void updateProgess(Sender aSender, Progress aCrawlerProgress);
}
