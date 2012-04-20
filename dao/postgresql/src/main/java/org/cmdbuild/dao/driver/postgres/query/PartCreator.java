package org.cmdbuild.dao.driver.postgres.query;

import java.util.ArrayList;
import java.util.List;

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
}
