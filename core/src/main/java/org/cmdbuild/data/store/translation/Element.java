package org.cmdbuild.data.store.translation;

import static org.cmdbuild.data.store.translation.Constants.ELEMENT;

import org.cmdbuild.data.store.Groupable;

public class Element implements Groupable {

	public static Element of(final String value) {
		return new Element(value);
	}

	private final String value;

	private Element(final String value) {
		this.value = value;
	}

	@Override
	public String getGroupAttributeName() {
		return ELEMENT;
	}

	@Override
	public Object getGroupAttributeValue() {
		return value;
	}

}
