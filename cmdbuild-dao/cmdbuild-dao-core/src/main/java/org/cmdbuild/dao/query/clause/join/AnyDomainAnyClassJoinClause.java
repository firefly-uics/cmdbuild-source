package org.cmdbuild.dao.query.clause.join;

import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.dao.entrytype.CMDomain;
import org.cmdbuild.dao.query.clause.alias.Alias;
import org.cmdbuild.dao.view.CMDataView;

public class AnyDomainAnyClassJoinClause extends JoinClause {

	public AnyDomainAnyClassJoinClause(final CMDataView view, final CMClass source, final Alias targetAlias, final Alias domainAlias) {
		super(targetAlias, domainAlias);
		addAllQueryDomainsAndTargetClasses(view, source);
	}

	private void addAllQueryDomainsAndTargetClasses(final CMDataView view, final CMClass source) {
		for (CMDomain d : view.findDomains(source)) {
			addQueryDomain(source, d);
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

	
}
