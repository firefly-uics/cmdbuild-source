package org.cmdbuild.services.soap.operation;

import static com.google.common.collect.FluentIterable.from;
import static org.cmdbuild.services.soap.operation.SerializationStuff.Functions.toAttributeSchema;

import java.util.List;
import java.util.Map;

import org.cmdbuild.logic.data.access.DataAccessLogic;
import org.cmdbuild.model.data.Card;
import org.cmdbuild.services.soap.structure.AttributeSchema;
import org.cmdbuild.services.soap.types.Attribute;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import com.google.common.collect.Maps;

public class DataAccessLogicHelper implements SoapLogicHelper {

	private static final Marker marker = MarkerFactory.getMarker(DataAccessLogicHelper.class.getName());

	private final DataAccessLogic dataAccessLogic;

	public DataAccessLogicHelper(final DataAccessLogic dataAccessLogic) {
		this.dataAccessLogic = dataAccessLogic;
	}

	public AttributeSchema[] getAttributeList(final String className) {
		logger.info(marker, "getting attributes schema for class '{}'", className);
		return from(dataAccessLogic.findClass(className).getActiveAttributes()) //
				.transform(toAttributeSchema()) //
				.toArray(AttributeSchema.class);
	}

	public int createCard(final org.cmdbuild.services.soap.types.Card card) {
		return dataAccessLogic.createCard(transform(card)).intValue();
	}

	public boolean updateCard(final org.cmdbuild.services.soap.types.Card card) {
		dataAccessLogic.updateCard(transform(card));
		return true;
	}

	public boolean deleteCard(final String className, final int cardId) {
		dataAccessLogic.deleteCard(className, Long.valueOf(cardId));
		return true;
	}

	private Card transform(final org.cmdbuild.services.soap.types.Card card) {
		final Card _card = Card.newInstance() //
				.withClassName(card.getClassName()) //
				.withId(Long.valueOf(card.getId())) //
				.withAllAttributes(transform(card.getAttributeList())) //
				.build();
		return _card;
	}

	private static Map<String, Object> transform(final List<Attribute> attributes) {
		final Map<String, Object> keysAndValues = Maps.newHashMap();
		for (final Attribute attribute : attributes) {
			final String name = attribute.getName();
			final String value = attribute.getValue();
			if (value != null) {
				keysAndValues.put(name, value);
			}
		}
		return keysAndValues;
	}

}
