package org.cmdbuild.dao.driver.postgres.query;

import java.util.ArrayList;
import java.util.List;

import org.cmdbuild.dao.driver.postgres.Utils;
import org.cmdbuild.dao.entrytype.CMEntryType;

public class PartCreator {
	protected final StringBuilder sb;
	private final List<Object> params;

	protected PartCreator() {
		sb = new StringBuilder();
		params = new ArrayList<Object>();
	}

	public final String getPart() {
		return sb.toString();
	}

	// TODO Handle CMDBuild and Geographic types conversion
	protected final String param(final Object o) {
		params.add(o);
		return "?";
	}

	public final List<Object> getParams() {
		return params;
	}

	protected final String quoteType(final CMEntryType type) {
		return Utils.quoteType(type, params);
	}
}
