package org.cmdbuild.service.rest.cxf;

import javax.ws.rs.core.MultivaluedMap;

import org.cmdbuild.service.rest.Cards;
import org.cmdbuild.service.rest.ClassCards;
import org.cmdbuild.service.rest.dto.Card;
import org.cmdbuild.service.rest.dto.ListResponse;
import org.cmdbuild.service.rest.dto.SimpleResponse;

public class CxfCards implements Cards {

	private final ClassCards delegate;

	public CxfCards(final ClassCards delegate) {
		this.delegate = delegate;
	}

	@Override
	public SimpleResponse<Long> create(final MultivaluedMap<String, String> formParams, final String type) {
		return delegate.create(type, formParams);
	}

	@Override
	public SimpleResponse<Card> read(final String type, final Long id) {
		return delegate.read(type, id);

	}

	@Override
	public ListResponse<Card> read(final String type, final String filter, final Integer limit, final Integer offset) {
		return delegate.read(type, filter, limit, offset);
	}

	@Override
	public void update(final Long id, final MultivaluedMap<String, String> formParams, final String type) {
		delegate.update(type, id, formParams);
	}

	@Override
	public void delete(final String type, final Long id) {
		delegate.delete(type, id);
	}

}
