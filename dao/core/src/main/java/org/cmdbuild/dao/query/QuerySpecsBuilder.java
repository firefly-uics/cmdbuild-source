package org.cmdbuild.dao.query;

import static org.cmdbuild.dao.query.clause.AnyClass.anyClass;
import static org.cmdbuild.dao.query.clause.QueryAliasAttribute.attribute;
import static org.cmdbuild.dao.query.clause.alias.Alias.as;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;

import net.jcip.annotations.NotThreadSafe;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.dao.entrytype.CMEntryType;
import org.cmdbuild.dao.query.clause.NamedAttribute;
import org.cmdbuild.dao.query.clause.QueryAliasAttribute;
import org.cmdbuild.dao.query.clause.QueryAttribute;
import org.cmdbuild.dao.query.clause.alias.Alias;
import org.cmdbuild.dao.query.clause.join.JoinClause;
import org.cmdbuild.dao.query.clause.join.Over;
import org.cmdbuild.dao.query.clause.where.EmptyWhereClause;
import org.cmdbuild.dao.query.clause.where.WhereClause;
import org.cmdbuild.dao.view.QueryExecutorDataView;

@NotThreadSafe
public class QuerySpecsBuilder {

	private class AliasLibrary {

		private final Set<Alias> aliasSet;
		private CMEntryType fromType;
		private Alias fromAlias;

		AliasLibrary() {
			aliasSet = new HashSet<Alias>();
		}

		public void addAlias(final Alias alias) {
			if (aliasSet.contains(alias)) {
				throw new IllegalArgumentException("Duplicate alias");
			}
			aliasSet.add(alias);
		}

		public void setFrom(final CMEntryType type, final Alias alias) {
			this.aliasSet.remove(this.fromAlias);
			addAlias(alias);
			this.fromType = type;
			this.fromAlias = alias;
		}

		public CMEntryType getFrom() {
			return fromType;
		}

		public CMClass getFromClass() {
			// FIXME
			try {
				return (CMClass) getFrom();
			} catch (final ClassCastException e) {
				return null;
			}
		}

		public Alias getFromAlias() {
			return fromAlias;
		}

		public void checkAlias(final Alias alias) {
			if (!aliasSet.contains(alias)) {
				throw new NoSuchElementException("Alias " + alias + " was not found");
			}
		}

		public Alias getDefaultAlias() {
			if (aliasSet.size() == 1) {
				return aliasSet.iterator().next();
			} else {
				throw new IllegalStateException("Unable to determine the default alias");
			}
		}
	}

	private static final Alias DEFAULT_ANYCLASS_ALIAS = Alias.as("_*");

	private List<QueryAttribute> attributes;
	private final List<JoinClause> joinClauses;
	private WhereClause whereClause;
	private Long offset;
	private Long limit;

	private final AliasLibrary aliases;

	private final QueryExecutorDataView view;

	public QuerySpecsBuilder(final QueryExecutorDataView view) {
		this.view = view;
		aliases = new AliasLibrary();
		select();
		from(anyClass(), DEFAULT_ANYCLASS_ALIAS);
		joinClauses = new ArrayList<JoinClause>();
		whereClause = new EmptyWhereClause();
	}

	public QuerySpecsBuilder select(final Object... attrDef) {
		this.attributes = new ArrayList<QueryAttribute>(attrDef.length);
		for (final Object a : attrDef) {
			if (a instanceof QueryAttribute) {
				attributes.add((QueryAttribute) a);
			} else if (a instanceof String) {
				attributes.add(new NamedAttribute((String) a));
			} else {
				throw new IllegalArgumentException();
			}
		}
		return this;
	}

	public QuerySpecsBuilder from(final CMEntryType from, final Alias alias) {
		aliases.setFrom(from, alias);
		return this;
	}

	public QuerySpecsBuilder from(final CMClass fromClass) {
		return from(fromClass, as(fromClass.getName()));
	}

	/*
	 * TODO: Consider more join levels (join with join tables)
	 */
	public QuerySpecsBuilder join(final CMClass joinClass, final Over overClause) {
		return join(joinClass, as(joinClass.getName()), overClause);
	}

	public QuerySpecsBuilder join(final CMClass joinClass, final Alias joinClassAlias, final Over overClause) {
		final CMClass fromClass = aliases.getFromClass();
		if (fromClass == null) {
			throw new IllegalStateException("No from clause specified or not a class");
		}
		final JoinClause join = new JoinClause.Builder(view, fromClass)
				.domain(overClause.getDomain(), overClause.getAlias()).target(joinClass, joinClassAlias).build();
		joinClauses.add(join);
		aliases.addAlias(joinClassAlias);
		aliases.addAlias(overClause.getAlias());
		return this;
	}

	public QuerySpecsBuilder where(final WhereClause clause) {
		whereClause = (clause == null) ? new EmptyWhereClause() : clause;
		return this;
	}

	public QuerySpecsBuilder offset(final Number offset) {
		this.offset = offset.longValue();
		return this;
	}

	public QuerySpecsBuilder limit(final Number limit) {
		this.limit = limit.longValue();
		return this;
	}

	public QuerySpecs build() {
		final QuerySpecsImpl qs = new QuerySpecsImpl(aliases.getFrom(), aliases.getFromAlias());

		for (final JoinClause jc : joinClauses) {
			if (jc.getTargets().isEmpty()) {
				return new EmptyQuerySpecs();
			}
			qs.addJoin(jc);
		}
		for (final QueryAttribute qa : attributes) {
			QueryAliasAttribute attribute;
			// FIXME: Implement it with a QueryAttribute visitor
			if (qa instanceof NamedAttribute) {
				final Alias alias = aliasForNamedAttribute((NamedAttribute) qa);
				attribute = attribute(alias, qa.getName());
			} else if (qa instanceof QueryAliasAttribute) {
				attribute = (QueryAliasAttribute) qa;
			} else {
				throw new UnsupportedOperationException("Unsupported attribute class");
			}
			aliases.checkAlias(attribute.getEntryTypeAlias());
			qs.addSelectAttribute(attribute);
		}
		qs.setWhereClause(whereClause);
		qs.setOffset(offset);
		qs.setLimit(limit);
		return qs;
	}

	private Alias aliasForNamedAttribute(final NamedAttribute na) {
		final String aliasName = na.getEntryTypeAliasName();
		if (aliasName == null) {
			return aliases.getDefaultAlias();
		} else {
			return as(aliasName);
		}
	}

	public CMQueryResult run() {
		return view.executeQuery(build());
	}

	/*
	 * Object
	 */

	@Override
	public String toString() {
		return super.toString(); // TODO
	}

	@Override
	public boolean equals(final Object obj) {
		return EqualsBuilder.reflectionEquals(this, obj);
	}

	@Override
	public int hashCode() {
		return HashCodeBuilder.reflectionHashCode(this);
	}
}
