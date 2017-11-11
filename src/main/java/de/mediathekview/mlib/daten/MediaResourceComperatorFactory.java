package de.mediathekview.mlib.daten;

import java.util.Comparator;

/**
 * A factory to build the film comperators.
 */
@SuppressWarnings("rawtypes")
public class MediaResourceComperatorFactory<T extends AbstractMediaResource> {
  /**
   * The aviable comperator types for {@link AbstractMediaResource}.
   */
  public enum MediaResourceComperatorTypes {
    SENDER_COMPERAOR, TITEL_COMPERATOR, THEMA_COMPERATOR, DATE_COMPERATOR, DEFAULT_COMPERATOR
  }

  private static MediaResourceComperatorFactory instance;

  private MediaResourceComperatorFactory() {
    super();
  }

  public static MediaResourceComperatorFactory getInstance() {
    if (instance == null) {
      instance = new MediaResourceComperatorFactory();
    }
    return instance;
  }

  public Comparator<T> getDefault() {
    return getFilmComperator(MediaResourceComperatorTypes.DEFAULT_COMPERATOR);
  }

  public Comparator<T> getFilmComperator(final MediaResourceComperatorTypes aFilmComperatorType) {
    switch (aFilmComperatorType) {
      case DATE_COMPERATOR:
        return Comparator.comparing(T::getTime);
      case SENDER_COMPERAOR:
        return Comparator.comparing(T::getSender);
      case THEMA_COMPERATOR:
        return Comparator.comparing(T::getThema);
      case TITEL_COMPERATOR:
        return Comparator.comparing(T::getTitel);
      default:
        return createDefaultComperator();
    }
  }

  private Comparator<T> createDefaultComperator() {
    return Comparator.comparing(T::getSender, Comparator.nullsLast(Comparator.naturalOrder()))
        .thenComparing(T::getThema, Comparator.nullsLast(Comparator.naturalOrder()))
        .thenComparing(T::getTime, Comparator.nullsLast(Comparator.naturalOrder()));
  }

}
