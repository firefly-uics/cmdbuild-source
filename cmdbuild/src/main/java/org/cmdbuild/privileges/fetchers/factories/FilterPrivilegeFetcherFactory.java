package org.cmdbuild.privileges.fetchers.factories;

import org.apache.commons.lang.Validate;
import org.cmdbuild.dao.view.DBDataView;
import org.cmdbuild.privileges.fetchers.CMClassPrivilegeFetcher;
import org.cmdbuild.privileges.fetchers.FilterPrivilegeFetcher;
import org.cmdbuild.privileges.fetchers.PrivilegeFetcher;

public class FilterPrivilegeFetcherFactory implements PrivilegeFetcherFactory {

	private final DBDataView dbView;
	private Long groupId;

	public FilterPrivilegeFetcherFactory(final DBDataView dbView) {
		this.dbView = dbView;
	}

	@Override
	public PrivilegeFetcher create() {
		Validate.notNull(groupId);
		return new FilterPrivilegeFetcher(dbView, groupId);
	}

	@Override
	public void setGroupId(final Long groupId) {
		this.groupId = groupId;
	}

}
