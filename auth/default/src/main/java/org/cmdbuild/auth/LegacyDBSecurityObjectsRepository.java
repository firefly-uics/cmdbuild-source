package org.cmdbuild.auth;

import org.cmdbuild.dao.view.CMDataView;


public class LegacyDBSecurityObjectsRepository extends LegacyDBUserFetcher {

	public LegacyDBSecurityObjectsRepository(final CMDataView view) {
		super(view);
	}

	/*
	 * Methods to manage users, group and privileges go here
	 */

}
