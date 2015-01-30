package org.cmdbuild.logic.data.access;

import static com.google.common.collect.FluentIterable.from;
import static org.cmdbuild.dao.query.clause.QueryAliasAttribute.attribute;

import org.cmdbuild.common.collect.Mapper;
import org.cmdbuild.dao.entrytype.CMEntryType;
import org.cmdbuild.dao.query.clause.QueryAliasAttribute;

import com.google.common.base.Function;
import com.google.common.base.Predicate;

public class AttributeSubsetMapper implements Mapper<Iterable<? extends String>, Iterable<QueryAliasAttribute>> {

	private final CMEntryType entryType;

	public AttributeSubsetMapper(final CMEntryType entryType) {
		this.entryType = entryType;
	}

	@Override
	public Iterable<QueryAliasAttribute> map(final Iterable<? extends String> jsonAttributes) {
		final Predicate<String> existingAttributes = new Predicate<String>() {

			@Override
			public boolean apply(final String input) {
				return (entryType.getAttribute(input) != null);
			}

		};
		final Function<String, QueryAliasAttribute> toQueryAliasAttribute = new Function<String, QueryAliasAttribute>() {

			@Override
			public QueryAliasAttribute apply(final String input) {
				return attribute(entryType, input);
			}

		};
		return from(jsonAttributes) //
				.filter(existingAttributes) //
				.transform(toQueryAliasAttribute);
	}
}
