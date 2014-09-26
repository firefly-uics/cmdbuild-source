package org.cmdbuild.service.rest.cxf;

import static com.google.common.collect.FluentIterable.from;
import static org.cmdbuild.service.rest.model.Builders.newMetadata;
import static org.cmdbuild.service.rest.model.Builders.newResponseMultiple;
import static org.cmdbuild.service.rest.model.Builders.newResponseSingle;

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
import org.cmdbuild.service.rest.model.Card;
import org.cmdbuild.service.rest.model.ResponseMultiple;
import org.cmdbuild.service.rest.model.ResponseSingle;
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
	public ResponseSingle<Long> create(final Long classId, final Card card) {
		final CMClass targetClass = userDataAccessLogic.findClass(classId);
		if (targetClass == null) {
			errorHandler.classNotFound(classId);
		}
		final org.cmdbuild.model.data.Card _card = org.cmdbuild.model.data.Card.newInstance(targetClass) //
				.withAllAttributes(card.getValues()) //
				.build();
		final Long id = userDataAccessLogic.createCard(_card);
		return newResponseSingle(Long.class) //
				.withElement(id) //
				.build();
	}

	@Override
	public ResponseSingle<Card> read(final Long classId, final Long id) {
		// TODO inject error management within logic
		final CMClass targetClass = userDataAccessLogic.findClass(classId);
		if (targetClass == null) {
			errorHandler.classNotFound(classId);
		}
		try {
			final CMCard fetched = userDataAccessLogic.fetchCMCard(targetClass.getName(), id);
			final Card element = FromCMCardToCard.newInstance() //
					.withDataView(userDataView) //
					.withErrorHandler(errorHandler) //
					.build() //
					.apply(fetched);
			return newResponseSingle(Card.class) //
					.withElement(element) //
					.build();
		} catch (final NotFoundException e) {
			errorHandler.cardNotFound(id);
			throw new AssertException("should not come here");
		}

	}

	@Override
	public ResponseMultiple<Card> read(final Long classId, final String filter, final Integer limit,
			final Integer offset) {
		final CMClass targetClass = userDataAccessLogic.findClass(classId);
		if (targetClass == null) {
			errorHandler.classNotFound(classId);
		}
		final QueryOptions queryOptions = QueryOptions.newQueryOption() //
				.filter(safeJsonObject(filter)) //
				.limit(limit) //
				.offset(offset) //
				.build();
		final FetchCardListResponse response = userDataAccessLogic.fetchCards(targetClass.getName(), queryOptions);
		final ToCardFunction<org.cmdbuild.model.data.Card> toCardDetail = FromCardToCard.newInstance() //
				.withDataView(systemDataView) //
				.withErrorHandler(errorHandler) //
				.build();
		final Iterable<Card> elements = from(response.elements()) //
				.transform(toCardDetail);
		return newResponseMultiple(Card.class) //
				.withElements(elements) //
				.withMetadata(newMetadata() //
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
	public void update(final Long classId, final Long id, final Card card) {
		final CMClass targetClass = userDataAccessLogic.findClass(classId);
		if (targetClass == null) {
			errorHandler.classNotFound(classId);
		}
		final org.cmdbuild.model.data.Card _card = org.cmdbuild.model.data.Card.newInstance(targetClass) //
				.withId(id) //
				.withAllAttributes(card.getValues()) //
				.build();
		userDataAccessLogic.updateCard(_card);
	}

	@Override
	public void delete(final Long classId, final Long id) {
		final CMClass targetClass = userDataAccessLogic.findClass(classId);
		if (targetClass == null) {
			errorHandler.classNotFound(classId);
		}
		// TODO check for missing id (inside logic, please)
		userDataAccessLogic.deleteCard(targetClass.getName(), id);
	}

}
