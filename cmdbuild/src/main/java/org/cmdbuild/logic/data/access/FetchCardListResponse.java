package org.cmdbuild.logic.data.access;

import org.cmdbuild.common.utils.PaginatedElements;
import org.cmdbuild.model.data.Card;

public class FetchCardListResponse extends PaginatedElements<Card> {

	public FetchCardListResponse(final Iterable<Card> elements, final int totalSize) {
		super(elements, totalSize);
	}

	public Iterable<Card> getPaginatedCards() {
		return super.paginatedElements();
	}

	public int getTotalNumberOfCards() {
		return super.totalSize();
	}

}
