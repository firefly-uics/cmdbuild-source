package org.cmdbuild.logic.data.access;

import org.cmdbuild.common.utils.PagedElements;
import org.cmdbuild.model.data.Card;

public class FetchCardListResponse extends PagedElements<Card> {

	public FetchCardListResponse(final Iterable<Card> elements, final int totalSize) {
		super(elements, totalSize);
	}

}
