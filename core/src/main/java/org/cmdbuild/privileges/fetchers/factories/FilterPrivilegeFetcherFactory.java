package org.cmdbuild.privileges.fetchers.factories;

import org.apache.commons.lang3.Validate;
import org.cmdbuild.auth.user.OperationUser;
import org.cmdbuild.dao.view.CMDataView;
import org.cmdbuild.privileges.fetchers.FilterPrivilegeFetcher;
import org.cmdbuild.privileges.fetchers.PrivilegeFetcher;

public class FilterPrivilegeFetcherFactory implements PrivilegeFetcherFactory {

	private final CMDataView dataView;
	private final OperationUser operationUser;
	private Long groupId;

	public FilterPrivilegeFetcherFactory(final CMDataView dataView, final OperationUser operationUser) {
		this.dataView = dataView;
		this.operationUser = operationUser;
	}

	@Override
	public PrivilegeFetcher create() {
		Validate.notNull(groupId);
		return new FilterPrivilegeFetcher(dataView, groupId, operationUser);
	}

	@Override
	public void setGroupId(final Long groupId) {
		this.groupId = groupId;
	}

}
