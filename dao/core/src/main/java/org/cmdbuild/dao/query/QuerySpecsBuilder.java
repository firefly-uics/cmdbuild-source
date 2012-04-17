package org.cmdbuild.dao.query;

import static org.cmdbuild.dao.query.clause.AnyClass.anyClass;
import static org.cmdbuild.dao.query.clause.QueryAliasAttribute.attribute;
import static org.cmdbuild.dao.query.clause.alias.Alias.as;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.dao.entrytype.CMEntryType;
import org.cmdbuild.dao.query.clause.NamedAttribute;
import org.cmdbuild.dao.query.clause.QueryAliasAttribute;
import org.cmdbuild.dao.query.clause.QueryAttribute;
import org.cmdbuild.dao.query.clause.alias.Alias;
import org.cmdbuild.dao.query.clause.alias.ClassAlias;
import org.cmdbuild.dao.query.clause.join.JoinClause;
import org.cmdbuild.dao.query.clause.join.Over;
import org.cmdbuild.dao.query.clause.where.EmptyWhereClause;
import org.cmdbuild.dao.query.clause.where.SimpleWhereClause;
import org.cmdbuild.dao.query.clause.where.SimpleWhereClause.Operator;
import org.cmdbuild.dao.query.clause.where.WhereClause;
import org.cmdbuild.dao.view.QueryExecutorDataView;

/*
 * Note: Not thread safe
 */
public class QuerySpecsBuilder {

	private class AliasLibrary {

		private Map<Alias, CMEntryType> aliasMap;
		private Alias fromAlias;

		AliasLibrary() {
			aliasMap = new HashMap<Alias, CMEntryType>();
		}

		public void addEntryType(final CMEntryType entryType, final Alias alias) {
			if (aliasMap.containsKey(alias)) {
				throw new IllegalArgumentException("Duplicate alias");
			}
			aliasMap.put(alias, entryType);
		}

		@Deprecated
		public void setFrom(final CMClass fromClass, final Alias fromAlias) {
			this.aliasMap.remove(this.fromAlias);
			addEntryType(fromClass, fromAlias);
			this.fromAlias = fromAlias;
		}

		@Deprecated
		public CMClass getFromClass() {
			return (CMClass) aliasMap.get(fromAlias);
		}

		@Deprecated
		public ClassAlias getFromClassAlias() {
			return new ClassAlias(getFromClass(), fromAlias);
		}

		public void checkAlias(final Alias alias) {
			 if (!aliasMap.containsKey(alias)) {
				 throw new NoSuchElementException("Alias "+ alias + " was not found");
			 }
		}

		public Alias getDefaultAlias() {
			if (aliasMap.size() == 1) {
				return aliasMap.keySet().iterator().next();
			} else {
				throw new IllegalStateException("Unable to determine the default alias");
			}
		}
	}

	private static final Alias DEFAULT_ANYCLASS_ALIAS = Alias.as("_*");

	private List<QueryAttribute> attributes;
	private List<JoinClause> joinClauses;
	private WhereClause whereClause;
	private Long offset;
	private Long limit;

	private AliasLibrary aliases;

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
		for (Object a : attrDef) {
			if (a instanceof QueryAttribute) {
				attributes.add((QueryAttribute)a);
			} else if (a instanceof String) {
				attributes.add(new NamedAttribute((String)a));
			} else {
				throw new IllegalArgumentException();
			}
		}
		return this;
	}

	public QuerySpecsBuilder from(final CMClass fromClass, final Alias fromAlias) {
		aliases.setFrom(fromClass, fromAlias);
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
		final JoinClause join = new JoinClause.Builder(view, aliases.getFromClass())
			.domain(overClause.getDomain(), overClause.getAlias())
			.target(joinClass, joinClassAlias)
			.build();
		joinClauses.add(join);
		aliases.addEntryType(joinClass, joinClassAlias); // What for?
		aliases.addEntryType(overClause.getDomain(), overClause.getAlias()); // What for?
		return this;
	}

	/*
	 * TODO: This should handle the more generic cases of "expression operator expression"
	 */
	public QuerySpecsBuilder where(final QueryAliasAttribute attribute, final Operator operator, final Object value) {
		if (whereClause instanceof EmptyWhereClause) {
			aliases.checkAlias(attribute.getEntryTypeAlias());
			whereClause = new SimpleWhereClause(attribute, operator, value);
		} else {
			throw new UnsupportedOperationException("Only one single where expression is supported at this time");
		}
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

	private QuerySpecs build() {
		final QuerySpecsImpl qs = new QuerySpecsImpl();
		qs.setFrom(aliases.getFromClassAlias());
		for (JoinClause jc : joinClauses) {
			if (jc.getTargets().isEmpty()) {
				return new EmptyQuerySpecs();
			}
			qs.addJoin(jc);
		}
		for (QueryAttribute qa : attributes) {
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

	private Alias aliasForNamedAttribute(NamedAttribute na) {
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
	 * String representation... totally incomplete!
	 */
	
	public String toCQL2() {
		String[] queryParts = { toCql2Select(), toCql2From() };
		return StringUtils.join(queryParts, " ");
	}

	private String toCql2Select() {
		final List<String> attributeNames = new ArrayList<String>(attributes.size());
		for (QueryAttribute qa : attributes) {
			attributeNames.add(qa.getName());
		}
		return "SELECT " + StringUtils.join(attributeNames, ", ");
	}

	private String toCql2From() {
		return "FROM " + aliases.getFromClass().getName();
	}

	public String toString() {
		return toCQL2();
	}

	/*
	 * Object
	 */

	@Override
	public boolean equals(Object obj) {
		return EqualsBuilder.reflectionEquals(this, obj);
	}

	@Override
	public int hashCode() {
		return HashCodeBuilder.reflectionHashCode(this);
	}
}
