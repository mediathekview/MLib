package de.mediathekview.mlib.daten;

import java.util.Comparator;

public enum MediaResourceComperators {
	SENDER_COMPERAOR(Comparator.comparing(AbstractMediaResource::getSender)),
	TITEL_COMPERATOR(Comparator.comparing(AbstractMediaResource::getTitel)),
	THEMA_COMPERATOR(Comparator.comparing(AbstractMediaResource::getThema)),
	DATE_COMPERATOR(Comparator.comparing(AbstractMediaResource::getTime)),
	DEFAULT_COMPERATOR(createDefaultComperator());

	private Comparator<AbstractMediaResource<?>> comparator;

	private MediaResourceComperators(Comparator<AbstractMediaResource<?>> acomparator) {
		comparator = acomparator;
	}

	private static Comparator<AbstractMediaResource<?>> createDefaultComperator() {
		return SENDER_COMPERAOR.getComparator().thenComparing(THEMA_COMPERATOR.getComparator())
				.thenComparing(DATE_COMPERATOR.getComparator());
	}

	public Comparator<AbstractMediaResource<?>> getComparator() {
		return comparator;
	}

}
