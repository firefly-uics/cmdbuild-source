package org.cmdbuild.dao.query;

import static org.cmdbuild.dao.query.clause.alias.UndefinedClassAlias.UNDEFINED_CLASS_ALIAS;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.cmdbuild.dao.query.clause.QueryAliasAttribute;
import org.cmdbuild.dao.query.clause.alias.Alias;
import org.cmdbuild.dao.query.clause.alias.ClassAlias;
import org.cmdbuild.dao.query.clause.join.JoinClause;
import org.cmdbuild.dao.query.clause.where.WhereClause;

/*
 * Used by QuerySpecsBuilder and by driver tests only
 */
public class QuerySpecs {

	private ClassAlias from;
	private final List<JoinClause> joinClauses;
	private final List<QueryAliasAttribute> attributes;
	private Integer offset;
	private Integer limit;
	private WhereClause whereClause;

	private Set<Alias> classAliases;
	private Set<Alias> domainAliases;

	protected QuerySpecs() {
		from = UNDEFINED_CLASS_ALIAS;
		joinClauses = new ArrayList<JoinClause>();
		attributes = new ArrayList<QueryAliasAttribute>();
		offset = null;
		limit = null;
		classAliases = new HashSet<Alias>();
		domainAliases = new HashSet<Alias>();
	}

	public void setFrom(final ClassAlias from) {
		this.from = from;
		classAliases.add(from.getAlias());
	}

	public ClassAlias getDBFrom() {
		return from;
	}

	public void addJoin(final JoinClause jc) {
		joinClauses.add(jc);
		domainAliases.add(jc.getDomainAlias());
		classAliases.add(jc.getTargetAlias());
	}

	public List<JoinClause> getJoins() {
		return joinClauses;
	}

	public Iterable<QueryAliasAttribute> getAttributes() {
		return this.attributes;
	}

	public void addSelectAttribute(final QueryAliasAttribute attribute) {
		attributes.add(attribute);
	}

	public void setWhereClause(final WhereClause whereClause) {
		this.whereClause = whereClause;
	}

	public WhereClause getWhereClause() {
		return whereClause;
	}

	public void setOffset(final Integer offset) {
		this.offset = offset;
	}

	public Integer getOffset() {
		return offset;
	}

	public void setLimit(final Integer limit) {
		this.limit = limit;
	}

	public Integer getLimit() {
		return limit;
	}

	public Set<Alias> getClassAliases() {
		return classAliases;
	}

	public Set<Alias> getDomainAliases() {
		return domainAliases;
	}
}
