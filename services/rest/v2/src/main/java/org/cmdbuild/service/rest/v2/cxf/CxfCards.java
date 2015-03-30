package org.cmdbuild.service.rest.v2.cxf;

import static com.google.common.collect.FluentIterable.from;
import static com.google.common.collect.Maps.newHashMap;
import static com.google.common.collect.Maps.transformEntries;
import static org.cmdbuild.service.rest.v2.cxf.util.Json.safeJsonArray;
import static org.cmdbuild.service.rest.v2.cxf.util.Json.safeJsonObject;
import static org.cmdbuild.service.rest.v2.model.Models.newCard;
import static org.cmdbuild.service.rest.v2.model.Models.newMetadata;
import static org.cmdbuild.service.rest.v2.model.Models.newResponseMultiple;
import static org.cmdbuild.service.rest.v2.model.Models.newResponseSingle;

import java.util.Map;
import java.util.Map.Entry;

import org.cmdbuild.dao.entry.CMCard;
import org.cmdbuild.dao.entrytype.CMAttribute;
import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.dao.entrytype.attributetype.CMAttributeType;
import org.cmdbuild.exception.NotFoundException;
import org.cmdbuild.logic.data.QueryOptions;
import org.cmdbuild.logic.data.access.DataAccessLogic;
import org.cmdbuild.logic.data.access.FetchCardListResponse;
import org.cmdbuild.service.rest.v2.Cards;
import org.cmdbuild.service.rest.v2.cxf.serialization.DefaultConverter;
import org.cmdbuild.service.rest.v2.logging.LoggingSupport;
import org.cmdbuild.service.rest.v2.model.Card;
import org.cmdbuild.service.rest.v2.model.ResponseMultiple;
import org.cmdbuild.service.rest.v2.model.ResponseSingle;

import com.google.common.base.Function;
import com.google.common.collect.Maps.EntryTransformer;
import com.mchange.util.AssertException;

public class CxfCards implements Cards, LoggingSupport {

	private final ErrorHandler errorHandler;
	private final DataAccessLogic dataAccessLogic;

	public CxfCards(final ErrorHandler errorHandler, final DataAccessLogic dataAccessLogic) {
		this.errorHandler = errorHandler;
		this.dataAccessLogic = dataAccessLogic;
	}

	@Override
	public ResponseSingle<Long> create(final String classId, final Card card) {
		final CMClass targetClass = assureClass(classId);
		final org.cmdbuild.model.data.Card _card = org.cmdbuild.model.data.Card.newInstance(targetClass) //
				.withAllAttributes(adaptInputValues(targetClass, card)) //
				.build();
		final Long id = dataAccessLogic.createCard(_card);
		return newResponseSingle(Long.class) //
				.withElement(id) //
				.build();
	}

	@Override
	public ResponseSingle<Card> read(final String classId, final Long id) {
		final CMClass targetClass = assureClass(classId);
		try {
			final CMCard fetched = dataAccessLogic.fetchCMCard(targetClass.getName(), id);
			final Card element = newCard() //
					.withType(fetched.getType().getName()) //
					.withId(fetched.getId()) //
					.withValues(adaptOutputValues(targetClass, fetched)) //
					.build();
			return newResponseSingle(Card.class) //
					.withElement(element) //
					.build();
		} catch (final NotFoundException e) {
			errorHandler.cardNotFound(id);
			throw new AssertException("should not come here");
		}

	}

	@Override
	public ResponseMultiple<Card> read(final String classId, final String filter, final String sort,
			final Integer limit, final Integer offset) {
		final CMClass targetClass = assureClass(classId);
		final QueryOptions queryOptions = QueryOptions.newQueryOption() //
				.filter(safeJsonObject(filter)) //
				.orderBy(safeJsonArray(sort)) //
				.limit(limit) //
				.offset(offset) //
				.build();
		final FetchCardListResponse response = dataAccessLogic.fetchCards(targetClass.getName(), queryOptions);
		final Iterable<Card> elements = from(response.elements()) //
				.transform(new Function<org.cmdbuild.model.data.Card, Card>() {

					@Override
					public Card apply(final org.cmdbuild.model.data.Card input) {
						return newCard() //
								.withType(input.getType().getName()) //
								.withId(input.getId()) //
								.withValues(adaptOutputValues(targetClass, input)) //
								.build();
					}

				});
		return newResponseMultiple(Card.class) //
				.withElements(elements) //
				.withMetadata(newMetadata() //
						.withTotal(Long.valueOf(response.totalSize())) //
						.build()) //
				.build();
	}

	@Override
	public void update(final String classId, final Long id, final Card card) {
		final CMClass targetClass = assureClass(classId);
		final org.cmdbuild.model.data.Card _card = org.cmdbuild.model.data.Card.newInstance(targetClass) //
				.withId(id) //
				.withAllAttributes(adaptInputValues(targetClass, card)) //
				.build();
		dataAccessLogic.updateCard(_card);
	}

	@Override
	public void delete(final String classId, final Long id) {
		final CMClass targetClass = assureClass(classId);
		// TODO check for missing id (inside logic, please)
		dataAccessLogic.deleteCard(targetClass.getName(), id);
	}

	private CMClass assureClass(final String classId) {
		final CMClass targetClass = dataAccessLogic.findClass(classId);
		if (targetClass == null) {
			errorHandler.classNotFound(classId);
		}
		if (dataAccessLogic.isProcess(targetClass)) {
			errorHandler.classNotFoundClassIsProcess(classId);
		}
		return targetClass;
	}

	private Map<String, Object> adaptInputValues(final CMClass targetClass, final Card card) {
		return transformEntries(card.getValues(), new EntryTransformer<String, Object, Object>() {

			@Override
			public Object transformEntry(final String key, final Object value) {
				final CMAttribute attribute = targetClass.getAttribute(key);
				final Object _value;
				if (attribute == null) {
					_value = value;
				} else {
					final CMAttributeType<?> attributeType = attribute.getType();
					_value = DefaultConverter.newInstance() //
							.build() //
							.fromClient() //
							.convert(attributeType, value);
				}
				return _value;
			}

		});
	}

	private Iterable<? extends Entry<String, Object>> adaptOutputValues(final CMClass targetClass, final CMCard card) {
		final Map<String, Object> values = newHashMap();
		for (final Entry<String, Object> entry : card.getValues()) {
			values.put(entry.getKey(), entry.getValue());
		}
		return adaptOutputValues(targetClass, values);
	}

	private Iterable<? extends Entry<String, Object>> adaptOutputValues(final CMClass targetClass,
			final org.cmdbuild.model.data.Card card) {
		return adaptOutputValues(targetClass, card.getAttributes());
	}

	private Iterable<? extends Entry<String, Object>> adaptOutputValues(final CMClass targetClass,
			final Map<String, Object> values) {
		return transformEntries(values, new EntryTransformer<String, Object, Object>() {

			@Override
			public Object transformEntry(final String key, final Object value) {
				final CMAttribute attribute = targetClass.getAttribute(key);
				final Object _value;
				if (attribute == null) {
					_value = value;
				} else {
					final CMAttributeType<?> attributeType = attribute.getType();
					_value = DefaultConverter.newInstance() //
							.build() //
							.toClient() //
							.convert(attributeType, value);
				}
				return _value;
			}

		}).entrySet();
	}
}
