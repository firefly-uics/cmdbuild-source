package org.cmdbuild.services.soap.operation;

import java.util.List;
import java.util.Map;

import org.cmdbuild.elements.interfaces.ICard;
import org.cmdbuild.logger.Log;
import org.cmdbuild.logic.data.access.DataAccessLogic;
import org.cmdbuild.model.data.Card;
import org.cmdbuild.services.soap.types.Attribute;

import com.google.common.collect.Maps;

public class DataAccessLogicHelper {

	private final DataAccessLogic dataAccessLogic;

	public DataAccessLogicHelper(final DataAccessLogic datAccessLogic) {
		this.dataAccessLogic = datAccessLogic;
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
