package org.cmdbuild.dao.query.clause.join;

import java.util.HashSet;
import java.util.Set;

import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.dao.entrytype.CMDomain;
import org.cmdbuild.dao.query.clause.AnyClass;
import org.cmdbuild.dao.query.clause.QueryDomain;
import org.cmdbuild.dao.query.clause.QueryDomain.Source;
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
		addQueryDomain(source, domain, target);
	}

	protected final void addQueryDomain(final CMClass source, final CMDomain d, final CMClass target) {
		if (d.getClass1().isAncestorOf(source)) {
			queryDomains.add(new QueryDomain(d, Source._1));
			if (target instanceof AnyClass) {
				addTargetLeaves(d.getClass2());
			} else {
				this.targets.add(target);
			}
		}
		if (d.getClass2().isAncestorOf(source)) {
			queryDomains.add(new QueryDomain(d, Source._2));
			if (target instanceof AnyClass) {
				addTargetLeaves(d.getClass1());
			} else {
				this.targets.add(target);
			}
		}
	}

	protected void addTargetLeaves(final CMClass targetDomainClass) {
		if (targetDomainClass.isSuperclass()) {
			for (CMClass subclass : targetDomainClass.getChildren()) {
				addTargetLeaves(subclass);
			}
		} else {
			targets.add(targetDomainClass);
		}
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
