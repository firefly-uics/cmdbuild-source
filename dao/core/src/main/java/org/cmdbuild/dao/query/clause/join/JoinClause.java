package org.cmdbuild.dao.query.clause.join;

import static com.google.common.collect.Sets.newHashSet;

import java.util.Set;

import org.apache.commons.lang.Validate;
import org.cmdbuild.common.Builder;
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

	public static class JoinClauseBuilder implements Builder<JoinClause> {

		private final CMDataView view;
		private final CMClass source;

		private Alias targetAlias;
		private Alias domainAlias;
		private final Set<CMClass> targets;
		private final Set<QueryDomain> queryDomains;
		private boolean domainHistory;
		private boolean left;

		private JoinClauseBuilder(final CMDataView view, final CMClass source) {
			Validate.notNull(source);
			this.view = view;
			this.source = source;
			this.queryDomains = newHashSet();
			this.targets = newHashSet();
		}

		public JoinClauseBuilder withDomain(CMDomain domain, final Alias domainAlias) {
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

		public JoinClauseBuilder withDomain(final QueryDomain queryDomain, final Alias domainAlias) {
			Validate.notNull(queryDomain);
			Validate.notNull(domainAlias);
			addQueryDomain(queryDomain);
			this.domainAlias = domainAlias;
			return this;
		}

		public JoinClauseBuilder withTarget(final CMClass target, final Alias targetAlias) {
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

		public JoinClauseBuilder left() {
			this.left = true;
			return this;
		}

		@Override
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

	public static final JoinClauseBuilder newJoinClause(final CMDataView view, final CMClass source) {
		return new JoinClauseBuilder(view, source);
	}

	private final Alias targetAlias;
	private final Alias domainAlias;
	private final boolean domainHistory;
	private final boolean left;

	private final Set<CMClass> targets;
	private final Set<QueryDomain> queryDomains;

	private JoinClause(final JoinClauseBuilder builder) {
		this.targetAlias = builder.targetAlias;
		this.domainAlias = builder.domainAlias;
		this.targets = builder.targets;
		this.queryDomains = builder.queryDomains;
		this.domainHistory = builder.domainHistory;
		this.left = builder.left;
	}

	public Alias getTargetAlias() {
		return targetAlias;
	}

	public Alias getDomainAlias() {
		return domainAlias;
	}

	public boolean hasTargets() {
		return !targets.isEmpty();
	}

	public Iterable<CMClass> getTargets() {
		return targets;
	}

	public boolean hasQueryDomains() {
		return !queryDomains.isEmpty();
	}

	public Iterable<QueryDomain> getQueryDomains() {
		return queryDomains;
	}

	public boolean isDomainHistory() {
		return domainHistory;
	}

	public boolean isLeft() {
		return left;
	}

}
