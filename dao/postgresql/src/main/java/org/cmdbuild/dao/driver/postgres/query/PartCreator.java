package org.cmdbuild.dao.driver.postgres.query;

import java.util.ArrayList;
import java.util.List;

import org.cmdbuild.dao.driver.postgres.Utils;
import org.cmdbuild.dao.driver.postgres.Utils.ParamAdder;
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

	protected final String param(final Object o) {
		return param(o, null);
	}

	// TODO Handle CMDBuild and Geographic types conversion
	protected final String param(final Object o, final String cast) {
		if (o instanceof List) {
			final List<Object> l = (List<Object>) o;
			final StringBuilder sb = new StringBuilder("(");
			int i = 1;
			for (final Object value : l) {
				sb.append("?");
				if (i < l.size()) {
					sb.append(",");
					i++;
				}
				params.add(value);
			}
			sb.append(")");
			return sb.toString();
		}
		params.add(o);
		return "?" + (cast != null ? "::" + cast : "");
	}

	public final List<Object> getParams() {
		return params;
	}

	protected final String quoteType(final CMEntryType type) {
		return Utils.quoteType(type, new ParamAdder() {

			@Override
			public void add(final Object value) {
				params.add(value);
			}

		});
	}
}
