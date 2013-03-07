package org.cmdbuild.logic.data.access;

import java.util.Iterator;

public class FetchCardListResponse implements Iterable<CardDTO> {

	private final Iterable<CardDTO> fetchedCards;
	private final int totalSize; // for pagination

	FetchCardListResponse(final Iterable<CardDTO> cards, final int totalSize) {
		this.totalSize = totalSize;
		this.fetchedCards = cards;
	}

	@Override
	public Iterator<CardDTO> iterator() {
		return fetchedCards.iterator();
	}

	public Iterable<CardDTO> getPaginatedCards() {
		return fetchedCards;
	}

	public int getTotalNumberOfCards() {
		return totalSize;
	}

}
