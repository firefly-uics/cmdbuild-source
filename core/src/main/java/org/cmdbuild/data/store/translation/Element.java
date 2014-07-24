package org.cmdbuild.data.store.translation;

import static org.cmdbuild.data.store.Groupables.nameAndValue;
import static org.cmdbuild.data.store.translation.Constants.ELEMENT;

import org.cmdbuild.data.store.ForwardingGroupable;
import org.cmdbuild.data.store.Groupable;

public class Element extends ForwardingGroupable {

	public static Element of(final String value) {
		return new Element(nameAndValue(ELEMENT, value));
	}

	private Element(final Groupable delegate) {
		super(delegate);
	}

}
