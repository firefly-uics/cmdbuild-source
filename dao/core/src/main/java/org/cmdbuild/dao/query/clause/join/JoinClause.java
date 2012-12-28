package org.cmdbuild.dao.query.clause.join;

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang.Validate;
import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.dao.entrytype.CMDomain;
import org.cmdbuild.dao.query.clause.AnyClass;
import org.cmdbuild.dao.query.clause.AnyDomain;
import org.cmdbuild.dao.query.clause.DomainHistory;
import org.cmdbuild.dao.query.clause.QueryDomain;
import org.cmdbuild.dao.query.clause.QueryDomain.Source;
import org.cmdbuild.dao.query.clause.alias.Alias;
import org.cmdbuild.dao.view.CMDataView;

public class JoinClause {

	private final Alias targetAlias;
	private final Alias domainAlias;
	private final boolean domainHistory;

	private final Set<CMClass> targets;
	private final Set<QueryDomain> queryDomains;

	private JoinClause(final Builder builder) {
		this.targetAlias = builder.targetAlias;
		this.domainAlias = builder.domainAlias;
		this.targets = builder.targets;
		this.queryDomains = builder.queryDomains;
		this.domainHistory = builder.domainHistory;
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

	public boolean isDomainHistory() {
		return domainHistory;
	}

	/*
	 * Builder
	 */

	public static class Builder {

		private final CMDataView view;
		private final CMClass source;

		private Alias targetAlias;
		private Alias domainAlias;
		private final Set<CMClass> targets;
		private final Set<QueryDomain> queryDomains;
		private boolean domainHistory;

		public Builder(final CMDataView view, final CMClass source) {
			Validate.notNull(source);
			this.view = view;
			this.source = source;
			this.queryDomains = new HashSet<QueryDomain>();
			this.targets = new HashSet<CMClass>();
		}

		public Builder domain(CMDomain domain, final Alias domainAlias) {
			Validate.notNull(domain);
			Validate.notNull(domainAlias);
			if (domain instanceof DomainHistory) {
				domain = ((DomainHistory) domain).getDomain();
				domainHistory = true;
			}
			if (domain instanceof AnyDomain) {
				addAllDomains();
			} else {
				addDomain(domain);
			}
			this.domainAlias = domainAlias;
			return this;
		}

		public Builder domain(final QueryDomain queryDomain, final Alias domainAlias) {
			Validate.notNull(queryDomain);
			Validate.notNull(domainAlias);
			addQueryDomain(queryDomain);
			this.domainAlias = domainAlias;
			return this;
		}

		public Builder target(final CMClass target, final Alias targetAlias) {
			Validate.notNull(target);
			Validate.notNull(targetAlias);
			if (target instanceof AnyClass) {
				addAnyTarget();
			} else {
				addTarget(target);
			}
			this.targetAlias = targetAlias;
			return this;
		}

		public JoinClause build() {
			return new JoinClause(this);
		}

		private void addAllDomains() {
			for (final CMDomain domain : view.findDomainsFor(source)) {
				addDomain(domain);
			}
		}

		private final void addDomain(final CMDomain domain) {
			addQueryDomain(new QueryDomain(domain, Source._1));
			addQueryDomain(new QueryDomain(domain, Source._2));
		}

		private final void addQueryDomain(final QueryDomain qd) {
			if (qd.getSourceClass().isAncestorOf(source)) {
				queryDomains.add(qd);
			}
		}

		private void addAnyTarget() {
			for (final QueryDomain qd : queryDomains) {
				addTargetLeaves(qd.getTargetClass());
			}
		}

		private void addTarget(final CMClass target) {
			for (final QueryDomain qd : queryDomains) {
				if (qd.getTargetClass().isAncestorOf(target)) {
					addTargetLeaves(target);
				}
			}
		}

		private void addTargetLeaves(final CMClass targetDomainClass) {
			for (final CMClass leaf : targetDomainClass.getLeaves()) {
				targets.add(leaf);
			}
		}
	}
}
