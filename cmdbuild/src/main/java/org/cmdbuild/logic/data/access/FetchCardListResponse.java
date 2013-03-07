package org.cmdbuild.logic.data.access;

import java.util.Iterator;

import org.cmdbuild.model.data.Card;

public class FetchCardListResponse implements Iterable<Card> {

	private final Iterable<Card> fetchedCards;
	private final int totalSize; // for pagination

	FetchCardListResponse(final Iterable<Card> cards, final int totalSize) {
		this.totalSize = totalSize;
		this.fetchedCards = cards;
	}

	@Override
	public Iterator<Card> iterator() {
		return fetchedCards.iterator();
	}

	public Iterable<Card> getPaginatedCards() {
		return fetchedCards;
	}

	public int getTotalNumberOfCards() {
		return totalSize;
	}

}
