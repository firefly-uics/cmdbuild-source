package org.cmdbuild.dao.view.user.privileges;

import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.dao.query.clause.where.OrWhereClause;
import org.cmdbuild.dao.query.clause.where.SimpleWhereClause;
import org.cmdbuild.dao.query.clause.where.WhereClause;

public interface RowPrivilegeFetcher {

	/**
	 * This method fetches row privileges for the currently logged user.
	 * 
	 * @param entryType
	 *            is the name of the class for which
	 * @return a WhereClause that is a {@link SimpleWhereClause} if the user
	 *         belongs to only one group, and {@link OrWhereClause} if the user
	 *         belongs to more than one group AND has a default group
	 */
	WhereClause fetchPrivilegeFiltersFor(CMClass entryType);

}
