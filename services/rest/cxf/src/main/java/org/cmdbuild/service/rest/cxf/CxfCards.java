package org.cmdbuild.service.rest.cxf;

import static org.cmdbuild.service.rest.constants.Serialization.UNDERSCORED_CLASSNAME;

import java.util.Map;

import javax.ws.rs.core.MultivaluedMap;

import org.cmdbuild.service.rest.Cards;
import org.cmdbuild.service.rest.ClassCards;
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
		final String name = formParam.getFirst(UNDERSCORED_CLASSNAME);
		if (name == null) {
			errorHandler.missingParam(UNDERSCORED_CLASSNAME);
		}
		formParam.remove(UNDERSCORED_CLASSNAME);
		return delegate.create(name, formParam);
	}

	@Override
	public SimpleResponse<Map<String, Object>> read(final String name, final Long id) {
		return delegate.read(name, id);

	}

	@Override
	public ListResponse<Map<String, Object>> readAll(final String name, final String filter, final Integer limit,
			final Integer offset) {
		return delegate.readAll(name, filter, limit, offset);
	}

	@Override
	public void update(final Long id, final MultivaluedMap<String, String> formParam) {
		final String name = formParam.getFirst(UNDERSCORED_CLASSNAME);
		if (name == null) {
			errorHandler.missingParam(UNDERSCORED_CLASSNAME);
		}
		formParam.remove(UNDERSCORED_CLASSNAME);
		delegate.update(name, id, formParam);
	}

	@Override
	public void delete(final String name, final Long id) {
		delegate.delete(name, id);
	}

}
