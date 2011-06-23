package org.cmdbuild.dao.query.clause.join;

import java.util.HashSet;
import java.util.Set;

import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.dao.entrytype.CMDomain;
import org.cmdbuild.dao.query.clause.QueryDomain;
import org.cmdbuild.dao.query.clause.alias.Alias;

public class JoinClause {

	private final Alias targetAlias;
	private final Alias domainAlias;

	protected final Set<CMClass> targets;
	protected final Set<QueryDomain> queryDomains;

	protected JoinClause(final Alias targetAlias, final Alias domainAlias) {
		this.targetAlias = targetAlias;
		this.domainAlias = domainAlias;
		this.targets = new HashSet<CMClass>();
		this.queryDomains = new HashSet<QueryDomain>();
	}

	public JoinClause(final CMClass source, final CMClass target, final CMDomain domain, final Alias targetAlias, final Alias domainAlias) {
		this(targetAlias, domainAlias);
		this.targets.add(target);
		addQueryDomain(source, domain);
	}

	protected final void addQueryDomain(final CMClass source, CMDomain d) {
		if (d.getClass1().isAncestorOf(source)) {
			queryDomains.add(new QueryDomain(d, true));
			addTargetLeaves(d.getClass2());
		}
		if (d.getClass2().isAncestorOf(source)) {
			queryDomains.add(new QueryDomain(d, false));
			addTargetLeaves(d.getClass1());
		}
	}

	protected void addTargetLeaves(CMClass targetDomainClass) {
		// We should not do it! Used in any domain join clause
	}

	public Alias getTargetAlias() {
		return targetAlias;
	}

	public Alias getDomainAlias() {
		return domainAlias;
	}

	public Set<CMClass> getTargets() {
		return targets;
	}

	public Set<QueryDomain> getQueryDomains() {
		return queryDomains;
	}
}
