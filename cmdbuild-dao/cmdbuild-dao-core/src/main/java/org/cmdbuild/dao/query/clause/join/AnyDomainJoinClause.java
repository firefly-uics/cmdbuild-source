package org.cmdbuild.dao.query.clause.join;

import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.dao.entrytype.CMDomain;
import org.cmdbuild.dao.query.clause.alias.Alias;
import org.cmdbuild.dao.view.CMDataView;

public class AnyDomainJoinClause extends JoinClause {

	public AnyDomainJoinClause(final CMDataView view, final CMClass source, final CMClass target, final Alias targetAlias, final Alias domainAlias) {
		super(targetAlias, domainAlias);
		addAllQueryDomainsAndTargetClasses(view, source, target);
	}

	private void addAllQueryDomainsAndTargetClasses(final CMDataView view, final CMClass source, final CMClass target) {
		for (CMDomain d : view.findDomains(source)) {
			addQueryDomain(source, d, target);
		}
	}
}
