package org.cmdbuild.service.rest.cxf;

import static org.cmdbuild.service.rest.constants.Serialization.UNDERSCORED_TYPE;

import javax.ws.rs.core.MultivaluedMap;

import org.cmdbuild.service.rest.Cards;
import org.cmdbuild.service.rest.ClassCards;
import org.cmdbuild.service.rest.dto.Card;
import org.cmdbuild.service.rest.dto.ListResponse;
import org.cmdbuild.service.rest.dto.SimpleResponse;
import org.cmdbuild.service.rest.serialization.ErrorHandler;

public class CxfCards implements Cards {

	private final ErrorHandler errorHandler;
	private final ClassCards delegate;

	public CxfCards(final ErrorHandler errorHandler, final ClassCards delegate) {
		this.errorHandler = errorHandler;
		this.delegate = delegate;
	}

	@Override
	public SimpleResponse<Long> create(final MultivaluedMap<String, String> formParam) {
		final String name = formParam.getFirst(UNDERSCORED_TYPE);
		if (name == null) {
			errorHandler.missingParam(UNDERSCORED_TYPE);
		}
		formParam.remove(UNDERSCORED_TYPE);
		return delegate.create(name, formParam);
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
	public void update(final Long id, final MultivaluedMap<String, String> formParam) {
		final String name = formParam.getFirst(UNDERSCORED_TYPE);
		if (name == null) {
			errorHandler.missingParam(UNDERSCORED_TYPE);
		}
		formParam.remove(UNDERSCORED_TYPE);
		delegate.update(name, id, formParam);
	}

	@Override
	public void delete(final String type, final Long id) {
		delegate.delete(type, id);
	}

}
