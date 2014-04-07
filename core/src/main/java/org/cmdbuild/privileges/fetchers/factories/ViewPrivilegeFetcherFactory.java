package org.cmdbuild.privileges.fetchers.factories;

import org.apache.commons.lang.Validate;
import org.cmdbuild.dao.view.DBDataView;
import org.cmdbuild.privileges.fetchers.PrivilegeFetcher;
import org.cmdbuild.privileges.fetchers.ViewPrivilegeFetcher;

public class ViewPrivilegeFetcherFactory implements PrivilegeFetcherFactory {

	private final DBDataView dbView;
	private Long groupId;

	public ViewPrivilegeFetcherFactory(final DBDataView view) {
		this.dbView = view;
	}

	@Override
	public PrivilegeFetcher create() {
		Validate.notNull(groupId);
		return new ViewPrivilegeFetcher(dbView, groupId);
	}

	@Override
	public void setGroupId(final Long groupId) {
		this.groupId = groupId;
	}

}
