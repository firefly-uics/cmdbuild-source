package org.cmdbuild.service.rest.cxf;

import static com.google.common.collect.FluentIterable.from;

import org.cmdbuild.dao.entry.CMCard;
import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.dao.view.CMDataView;
import org.cmdbuild.exception.NotFoundException;
import org.cmdbuild.logic.data.QueryOptions;
import org.cmdbuild.logic.data.access.DataAccessLogic;
import org.cmdbuild.logic.data.access.FetchCardListResponse;
import org.cmdbuild.service.rest.Cards;
import org.cmdbuild.service.rest.cxf.serialization.FromCMCardToCard;
import org.cmdbuild.service.rest.cxf.serialization.FromCardToCard;
import org.cmdbuild.service.rest.cxf.serialization.ToCardFunction;
import org.cmdbuild.service.rest.dto.Card;
import org.cmdbuild.service.rest.dto.DetailResponseMetadata;
import org.cmdbuild.service.rest.dto.ListResponse;
import org.cmdbuild.service.rest.dto.SimpleResponse;
import org.json.JSONException;
import org.json.JSONObject;

import com.mchange.util.AssertException;

public class CxfCards implements Cards {

	private final ErrorHandler errorHandler;
	private final DataAccessLogic userDataAccessLogic;
	private final CMDataView systemDataView;
	private final CMDataView userDataView;

	public CxfCards(final ErrorHandler errorHandler, final DataAccessLogic userDataAccessLogic,
			final CMDataView systemDataView, final CMDataView userDataView) {
		this.errorHandler = errorHandler;
		this.userDataAccessLogic = userDataAccessLogic;
		this.systemDataView = systemDataView;
		this.userDataView = userDataView;
	}

	@Override
	public SimpleResponse<Long> create(final String type, final Card card) {
		final CMClass targetClass = userDataAccessLogic.findClass(type);
		if (targetClass == null) {
			errorHandler.classNotFound(type);
		}
		final org.cmdbuild.model.data.Card _card = org.cmdbuild.model.data.Card.newInstance(targetClass) //
				.withAllAttributes(card.getValues()) //
				.build();
		final Long id = userDataAccessLogic.createCard(_card);
		return SimpleResponse.<Long> newInstance() //
				.withElement(id) //
				.build();
	}

	@Override
	public SimpleResponse<Card> read(final String type, final Long id) {
		// TODO inject error management within logic
		if (userDataAccessLogic.findClass(type) == null) {
			errorHandler.classNotFound(type);
		}
		try {
			final CMCard fetched = userDataAccessLogic.fetchCMCard(type, id);
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
	public ListResponse<Card> read(final String type, final String filter, final Integer limit, final Integer offset) {
		final QueryOptions queryOptions = QueryOptions.newQueryOption() //
				.filter(safeJsonObject(filter)) //
				.limit(limit) //
				.offset(offset) //
				.build();
		final FetchCardListResponse response = userDataAccessLogic.fetchCards(type, queryOptions);
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
	public void update(final String type, final Long id, final Card card) {
		final CMClass targetClass = userDataAccessLogic.findClass(type);
		if (targetClass == null) {
			errorHandler.classNotFound(type);
		}
		final org.cmdbuild.model.data.Card _card = org.cmdbuild.model.data.Card.newInstance(targetClass) //
				.withId(card.getId()) //
				.withAllAttributes(card.getValues()) //
				.build();
		userDataAccessLogic.updateCard(_card);
	}

	@Override
	public void delete(final String type, final Long id) {
		final CMClass targetClass = userDataAccessLogic.findClass(type);
		if (targetClass == null) {
			errorHandler.classNotFound(type);
		}
		// TODO check for missing id (inside logic, please)
		userDataAccessLogic.deleteCard(type, id);
	}

}
