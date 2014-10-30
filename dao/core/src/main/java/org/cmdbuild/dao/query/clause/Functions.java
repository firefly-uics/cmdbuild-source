package org.cmdbuild.dao.query.clause;

import com.google.common.base.Function;

public class Functions {

	public static final Function<QueryAliasAttribute, String> name() {
		return new Function<QueryAliasAttribute, String>() {

			@Override
			public String apply(final QueryAliasAttribute input) {
				return input.getName();
			}

		};
	}

	private Functions() {
		// prevents instantiation
	}

}
