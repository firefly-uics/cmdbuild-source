package org.cmdbuild.service.rest.cxf;

import static com.google.common.collect.FluentIterable.from;
import static com.google.common.collect.Maps.transformEntries;

import java.util.List;
import java.util.Map;

import javax.ws.rs.core.Context;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriInfo;

import org.cmdbuild.dao.entry.CMCard;
import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.exception.NotFoundException;
import org.cmdbuild.logic.data.QueryOptions;
import org.cmdbuild.logic.data.access.FetchCardListResponse;
import org.cmdbuild.model.data.Card;
import org.cmdbuild.service.rest.Cards;
import org.cmdbuild.service.rest.dto.CardListResponse;
import org.cmdbuild.service.rest.dto.CardResponse;
import org.cmdbuild.service.rest.dto.DetailResponseMetadata;
import org.cmdbuild.service.rest.dto.NewCardResponse;
import org.cmdbuild.service.rest.serialization.FromCMCardToCardDetail;
import org.cmdbuild.service.rest.serialization.FromCardToCardDetail;
import org.cmdbuild.service.rest.serialization.FromSomeKindOfCardToMap;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.common.collect.Maps.EntryTransformer;
import com.mchange.util.AssertException;

public class CxfCards extends CxfService implements Cards {

	private static final EntryTransformer<String, List<String>, String> FIRST_ELEMENT = new EntryTransformer<String, List<String>, String>() {

		@Override
		public String transformEntry(final String key, final List<String> value) {
			return value.isEmpty() ? null : value.get(0);
		}

	};

	@Context
	protected UriInfo uriInfo;

	@Override
	public NewCardResponse create(final String name, final MultivaluedMap<String, String> formParam) {
		final CMClass targetClass = userDataView().findClass(name);
		if (targetClass == null) {
			errorHandler().classNotFound(name);
		}
		final Map<String, String> attributes = transformEntries(formParam, FIRST_ELEMENT);
		final Card card = Card.newInstance(targetClass) //
				.withAllAttributes(attributes) //
				.build();
		final Long id = userDataAccessLogic().createCard(card);
		return NewCardResponse.newInstance() //
				.withElement(id) //
				.build();
	}

	@Override
	public CardResponse read(final String name, final Long id) {
		// TODO inject error management within logic
		if (userDataView().findClass(name) == null) {
			errorHandler().classNotFound(name);
		}
		try {
			final CMCard fetched = userDataAccessLogic().fetchCMCard(name, id);
			final Map<String, Object> elements = FromCMCardToCardDetail.newInstance() //
					.withDataView(userDataView()) //
					.withErrorHandler(errorHandler()) //
					.build() //
					.apply(fetched);
			return CardResponse.newInstance() //
					.withElement(elements) //
					.build();
		} catch (final NotFoundException e) {
			errorHandler().cardNotFound(id);
			throw new AssertException("should not come here");
		}

	}

	@Override
	public CardListResponse readAll(final String name, final String filter, final Integer limit, final Integer offset) {
		final QueryOptions queryOptions = QueryOptions.newQueryOption() //
				.filter(safeJsonObject(filter)) //
				.limit(limit) //
				.offset(offset) //
				.build();
		final FetchCardListResponse response = userDataAccessLogic().fetchCards(name, queryOptions);

		final FromSomeKindOfCardToMap<Card> toCardDetail = FromCardToCardDetail.newInstance() //
				.withDataView(systemDataView()) //
				.withErrorHandler(errorHandler()) //
				.build();
		final Iterable<Map<String, Object>> elements = from(response.elements()) //
				.transform(toCardDetail);
		return CardListResponse.newInstance() //
				.withElements(elements) //
				.withMetadata(DetailResponseMetadata.newInstance() //
						.withTotal(response.totalSize()) //
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
		final CMClass targetClass = userDataView().findClass(name);
		if (targetClass == null) {
			errorHandler().classNotFound(name);
		}
		// TODO check for missing id (inside logic, please)
		final Map<String, String> attributes = transformEntries(formParam, FIRST_ELEMENT);
		final Card card = Card.newInstance(targetClass) //
				.withId(id) //
				.withAllAttributes(attributes) //
				.build();
		userDataAccessLogic().updateCard(card);
	}

	@Override
	public void delete(final String name, final Long id) {
		final CMClass targetClass = userDataView().findClass(name);
		if (targetClass == null) {
			errorHandler().classNotFound(name);
		}
		// TODO check for missing id (inside logic, please)
		userDataAccessLogic().deleteCard(name, id);
	}

}
