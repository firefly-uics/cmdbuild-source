package org.cmdbuild.api.utils;

import static org.apache.commons.lang.StringUtils.EMPTY;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.cmdbuild.services.soap.Attribute;

public class AttributeUtils {

	private AttributeUtils() {
		// prevents instantiation
	}

	public static List<Attribute> attributesFor(final Map<String, String> attributeMap) {
		final List<Attribute> attributeList = new ArrayList<Attribute>();
		for (final Entry<String, String> attributeEntry : attributeMap.entrySet()) {
			final Attribute attribute = attributeFor(attributeEntry);
			attributeList.add(attribute);
		}
		return attributeList;
	}

	private static Attribute attributeFor(final Entry<String, String> entry) {
		final Attribute attribute = new Attribute();
		attribute.setName(entry.getKey());
		attribute.setValue(safeValue(entry.getValue()));
		return attribute;
	}

	private static String safeValue(final String value) {
		return (value == null) ? EMPTY : value;
	}

}
