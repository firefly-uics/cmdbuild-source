package org.cmdbuild.servlets.json.serializers.translations.commons;

import java.util.Collections;

import org.cmdbuild.logic.data.access.DataAccessLogic.AttributesQuery;

public class Constants {
	
	public static final String IDENTIFIER = "identifier";
	public static final String DESCRIPTION = "description";
	public static final String DEFAULT = "default";
	public static final String TYPE = "type";
	public static final String OWNER = "owner";
	public static final String KEY_SEPARATOR = ".";
	
	public static final AttributesQuery NO_LIMIT_AND_OFFSET = new AttributesQuery() {

		@Override
		public Integer limit() {
			return null;
		}

		@Override
		public Integer offset() {
			return null;
		}

	};
	
	public static <T> Iterable<T> nullableIterable(final Iterable<T> it) {
		return it != null ? it : Collections.<T> emptySet();
	}
	
	private Constants() {
		// prevents instantiation
	}
	
}
