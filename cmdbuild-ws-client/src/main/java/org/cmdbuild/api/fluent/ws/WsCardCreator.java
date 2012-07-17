package org.cmdbuild.api.fluent.ws;

import static org.cmdbuild.common.Constants.CODE_ATTRIBUTE;
import static org.cmdbuild.common.Constants.DESCRIPTION_ATTRIBUTE;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.cmdbuild.api.fluent.CardCreator;
import org.cmdbuild.api.fluent.CardDescriptor;
import org.cmdbuild.services.soap.Attribute;
import org.cmdbuild.services.soap.Card;
import org.cmdbuild.services.soap.Private;

public class WsCardCreator extends WsExecutor implements CardCreator {

	private String className;
	private final Map<String, String> attributes;

	WsCardCreator(final Private proxy) {
		super(proxy);

		attributes = new HashMap<String, String>();
	}

	public CardCreator forClass(final String className) {
		this.className = className;
		return this;
	}

	public CardCreator withCode(final String value) {
		return with(CODE_ATTRIBUTE, value);
	}

	public CardCreator withDescription(final String value) {
		return with(DESCRIPTION_ATTRIBUTE, value);
	}

	public CardCreator with(final String name, final String value) {
		return withAttribute(name, value);
	}

	public CardCreator withAttribute(final String name, final String value) {
		attributes.put(name, value);
		return this;
	}

	public CardDescriptor create() {
		final Card wsCard = new Card();
		fillClassName(wsCard);
		fillAttributes(wsCard);
		final int id = proxy().createCard(wsCard);
		return new CardDescriptor(className, id);
	}

	private void fillClassName(final Card wsCard) {
		wsCard.setClassName(className);
	}

	private void fillAttributes(final Card wsCard) {
		final List<Attribute> attributeList = wsCard.getAttributeList();
		for (final Entry<String, String> attributeEntry : attributes.entrySet()) {
			final Attribute attribute = new Attribute();
			attribute.setName(attributeEntry.getKey());
			attribute.setValue(attributeEntry.getValue());
			attributeList.add(attribute);
		}
	}

}
