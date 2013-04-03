package org.cmdbuild.logic.data.lookup;

import java.util.Set;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.cmdbuild.data.store.lookup.LookupDto;
import org.cmdbuild.data.store.lookup.LookupTypeDto;
import org.slf4j.Logger;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Sets;

class Util {

	private static final Logger logger = LookupLogic.logger;

	private Util() {
		// prevents instantiation
	}

	public static final Function<LookupDto, LookupTypeDto> toLookupType() {
		logger.debug("converting from lookups to lookup types");
		return new Function<LookupDto, LookupTypeDto>() {

			@Override
			public LookupTypeDto apply(final LookupDto input) {
				return input.type;
			}

		};
	}

	public static Predicate<LookupTypeDto> uniques() {
		logger.debug("filtering unique lookup types");
		return new Predicate<LookupTypeDto>() {

			private final Set<LookupTypeDto> uniques = Sets.newHashSet();

			@Override
			public boolean apply(final LookupTypeDto input) {
				return uniques.add(input);
			}

		};
	}

	public static Predicate<LookupTypeDto> typesWith(final String name) {
		logger.debug("filtering lookup types with name '{}'");
		return new Predicate<LookupTypeDto>() {

			@Override
			public boolean apply(final LookupTypeDto input) {
				return input.name.equals(name);
			}

		};
	}

	public static Predicate<LookupTypeDto> typesWith(final String name, final String parent) {
		logger.debug("filtering lookup types with name '{}' and parent '{}'", name, parent);
		return new Predicate<LookupTypeDto>() {

			@Override
			public boolean apply(final LookupTypeDto input) {
				return new EqualsBuilder() //
						.append(name, input.name) //
						.append(parent, input.parent) //
						.isEquals();
			}

		};
	}

	public static Predicate<LookupDto> withType(final LookupTypeDto type) {
		logger.debug("filtering lookups with type '{}'", type);
		return new Predicate<LookupDto>() {

			@Override
			public boolean apply(final LookupDto input) {
				return input.type.equals(type);
			}

		};
	}

	public static Predicate<LookupDto> withId(final Long id) {
		logger.debug("filtering lookups with id '{}'", id);
		return new Predicate<LookupDto>() {
			@Override
			public boolean apply(final LookupDto input) {
				return input.id.equals(id);
			}
		};
	}

	public static Predicate<LookupDto> actives(final boolean activeOnly) {
		logger.debug("filtering actives lookups (actives only '{}')", activeOnly);
		return new Predicate<LookupDto>() {

			@Override
			public boolean apply(final LookupDto input) {
				return !activeOnly || input.active;
			}

		};
	}

	public static Predicate<LookupDto> limited(final int start, final int limit) {
		logger.debug("filtering lookups starting at '{}' and limited at '{}'", start, limit);
		return new Predicate<LookupDto>() {

			private final int end = limit > 0 ? limit + start : Integer.MAX_VALUE;
			private int i = 0;

			@Override
			public boolean apply(final LookupDto input) {
				i++;
				return (start <= i && i < end);
			}

		};
	}
}
