package org.cmdbuild.privileges.fetchers.factories;

import org.apache.commons.lang.Validate;
import org.cmdbuild.auth.user.OperationUser;
import org.cmdbuild.dao.view.DBDataView;
import org.cmdbuild.privileges.fetchers.FilterPrivilegeFetcher;
import org.cmdbuild.privileges.fetchers.PrivilegeFetcher;

public class FilterPrivilegeFetcherFactory implements PrivilegeFetcherFactory {

	private final DBDataView dbView;
	private final OperationUser operationUser;
	private Long groupId;

	public FilterPrivilegeFetcherFactory(final DBDataView dbView, final OperationUser operationUser) {
		this.dbView = dbView;
		this.operationUser = operationUser;
	}

	@Override
	public PrivilegeFetcher create() {
		Validate.notNull(groupId);
		return new FilterPrivilegeFetcher(dbView, groupId, operationUser);
	}

	@Override
	public void setGroupId(final Long groupId) {
		this.groupId = groupId;
	}

}
