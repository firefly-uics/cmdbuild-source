package org.cmdbuild.servlets.json.serializers.translations.commons;

import static org.apache.commons.lang3.StringUtils.EMPTY;

import java.util.Collections;

import org.cmdbuild.logic.data.access.DataAccessLogic.AttributesQuery;
import org.cmdbuild.model.view.View;
import org.cmdbuild.services.store.FilterStore.Filter;

import com.google.common.base.Predicate;

public class Constants {
	
	public static final String IDENTIFIER = "identifier";
	public static final String DESCRIPTION = "description";
	public static final String DEFAULT = "default";
	public static final String TYPE = "type";
	public static final String OWNER = "owner";
	public static final String KEY_SEPARATOR = ".";
	public static final String NO_OWNER = EMPTY;
	
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
	
	public static Predicate<Filter> matchFilterByName(final String name) {
		return new Predicate<Filter>() {
			@Override
			public boolean apply(final Filter input) {
				return name.equals(input.getName());
			}
		};
	}

	public static Predicate<View> matchViewByName(final String name) {
		return new Predicate<View>() {
			@Override
			public boolean apply(final View input) {
				return name.equals(input.getName());
			}
		};
	}
	
	private Constants() {
		// prevents instantiation
	}
	
}
