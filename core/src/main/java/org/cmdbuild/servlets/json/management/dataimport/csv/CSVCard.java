package org.cmdbuild.servlets.json.management.dataimport.csv;

import java.util.Map;

import org.cmdbuild.dao.entry.CMCard;

import com.google.common.collect.Maps;

public class CSVCard {

	private final Map<String, Object> invalidAttributes = Maps.newHashMap();
	private final CMCard card;
	private final Long fakeId;

	public CSVCard(final CMCard card, final Long fakeId) {
		this.card = card;
		this.fakeId = fakeId;
	}

	public CMCard getCMCard() {
		return card;
	}

	public Long getFakeId() {
		return fakeId;
	}

	public Map<String, Object> getInvalidAttributes() {
		return invalidAttributes;
	}

	public void addInvalidAttribute(final String name, final Object value) {
		invalidAttributes.put(name, value);
	}

}
