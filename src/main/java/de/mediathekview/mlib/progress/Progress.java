package de.mediathekview.mlib.progress;

/**
 * A POJO to store the progress information.
 */
public class Progress
{
    private long maxCount;
    private long actualCount;
    private long errorCount;

    public Progress(long aMaxCount, long aActualCount, long aErrorCount)
    {
        super();
        maxCount = aMaxCount;
        actualCount = aActualCount;
        errorCount = aErrorCount;
    }

    public long getActualCount()
    {
        return actualCount;
    }

    public long getErrorCount()
    {
        return errorCount;
    }

    public long getMaxCount()
    {
        return maxCount;
    }

    /**
     * Calculates the actual progress in percent.
     * @return The actual progress in percent.
     */
    public float calcProgressInPercent()
    {
            return maxCount > 0 ? actualCount * 100f / maxCount : 0f;
    }

    /**
     * Calculates the error percentage of actual progress.
     * @return The error percentage of actual progress.
     */
    public float calcActualErrorQuoteInPercent()
    {
        return actualCount > 0 ? errorCount * 100f / actualCount : 0f;
    }

    /**
     * Calculates the total error percentage.
     * @return The total error percentage.
     */
    public float calcProgressErrorQuoteInPercent()
    {
        return maxCount > 0 ? errorCount * 100f / maxCount : 0f;
    }
}
