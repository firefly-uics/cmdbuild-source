package org.cmdbuild.service.rest.cxf;

import static com.google.common.collect.FluentIterable.from;
import static com.google.common.collect.Maps.transformEntries;

import java.util.List;
import java.util.Map;

import javax.ws.rs.core.MultivaluedMap;

import org.cmdbuild.dao.entry.CMCard;
import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.dao.view.CMDataView;
import org.cmdbuild.exception.NotFoundException;
import org.cmdbuild.logic.data.QueryOptions;
import org.cmdbuild.logic.data.access.DataAccessLogic;
import org.cmdbuild.logic.data.access.FetchCardListResponse;
import org.cmdbuild.service.rest.ClassCards;
import org.cmdbuild.service.rest.dto.Card;
import org.cmdbuild.service.rest.dto.DetailResponseMetadata;
import org.cmdbuild.service.rest.dto.ListResponse;
import org.cmdbuild.service.rest.dto.SimpleResponse;
import org.cmdbuild.service.rest.serialization.ErrorHandler;
import org.cmdbuild.service.rest.serialization.FromCMCardToCard;
import org.cmdbuild.service.rest.serialization.FromCardToCard;
import org.cmdbuild.service.rest.serialization.ToCardFunction;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.common.collect.Maps.EntryTransformer;
import com.mchange.util.AssertException;

public class CxfClassCards implements ClassCards {

	private static final EntryTransformer<String, List<String>, String> FIRST_ELEMENT = new EntryTransformer<String, List<String>, String>() {

		@Override
		public String transformEntry(final String key, final List<String> value) {
			return value.isEmpty() ? null : value.get(0);
		}

	};

	private final ErrorHandler errorHandler;
	private final DataAccessLogic userDataAccessLogic;
	private final CMDataView systemDataView;
	private final CMDataView userDataView;

	public CxfClassCards(final ErrorHandler errorHandler, final DataAccessLogic userDataAccessLogic,
			final CMDataView systemDataView, final CMDataView userDataView) {
		this.errorHandler = errorHandler;
		this.userDataAccessLogic = userDataAccessLogic;
		this.systemDataView = systemDataView;
		this.userDataView = userDataView;
	}

	@Override
	public SimpleResponse<Long> create(final String name, final MultivaluedMap<String, String> formParam) {
		final CMClass targetClass = userDataView.findClass(name);
		if (targetClass == null) {
			errorHandler.classNotFound(name);
		}
		final Map<String, String> attributes = transformEntries(formParam, FIRST_ELEMENT);
		final org.cmdbuild.model.data.Card card = org.cmdbuild.model.data.Card.newInstance(targetClass) //
				.withAllAttributes(attributes) //
				.build();
		final Long id = userDataAccessLogic.createCard(card);
		return SimpleResponse.<Long> newInstance() //
				.withElement(id) //
				.build();
	}

	@Override
	public SimpleResponse<Card> read(final String name, final Long id) {
		// TODO inject error management within logic
		if (userDataView.findClass(name) == null) {
			errorHandler.classNotFound(name);
		}
		try {
			final CMCard fetched = userDataAccessLogic.fetchCMCard(name, id);
			final Card element = FromCMCardToCard.newInstance() //
					.withDataView(userDataView) //
					.withErrorHandler(errorHandler) //
					.build() //
					.apply(fetched);
			return SimpleResponse.newInstance(Card.class) //
					.withElement(element) //
					.build();
		} catch (final NotFoundException e) {
			errorHandler.cardNotFound(id);
			throw new AssertException("should not come here");
		}

	}

	@Override
	public ListResponse<Card> readAll(final String name, final String filter, final Integer limit, final Integer offset) {
		final QueryOptions queryOptions = QueryOptions.newQueryOption() //
				.filter(safeJsonObject(filter)) //
				.limit(limit) //
				.offset(offset) //
				.build();
		final FetchCardListResponse response = userDataAccessLogic.fetchCards(name, queryOptions);
		final ToCardFunction<org.cmdbuild.model.data.Card> toCardDetail = FromCardToCard.newInstance() //
				.withDataView(systemDataView) //
				.withErrorHandler(errorHandler) //
				.build();
		final Iterable<Card> elements = from(response.elements()) //
				.transform(toCardDetail);
		return ListResponse.newInstance(Card.class) //
				.withElements(elements) //
				.withMetadata(DetailResponseMetadata.newInstance() //
						.withTotal(Long.valueOf(response.totalSize())) //
						.build()) //
				.build();
	}

	private JSONObject safeJsonObject(final String filter) {
		try {
			return (filter == null) ? new JSONObject() : new JSONObject(filter);
		} catch (final JSONException e) {
			// TODO log
			return null;
		}
	}

	@Override
	public void update(final String name, final Long id, final MultivaluedMap<String, String> formParam) {
		final CMClass targetClass = userDataView.findClass(name);
		if (targetClass == null) {
			errorHandler.classNotFound(name);
		}
		// TODO check for missing id (inside logic, please)
		final Map<String, String> attributes = transformEntries(formParam, FIRST_ELEMENT);
		final org.cmdbuild.model.data.Card card = org.cmdbuild.model.data.Card.newInstance(targetClass) //
				.withId(id) //
				.withAllAttributes(attributes) //
				.build();
		userDataAccessLogic.updateCard(card);
	}

	@Override
	public void delete(final String name, final Long id) {
		final CMClass targetClass = userDataView.findClass(name);
		if (targetClass == null) {
			errorHandler.classNotFound(name);
		}
		// TODO check for missing id (inside logic, please)
		userDataAccessLogic.deleteCard(name, id);
	}

}
