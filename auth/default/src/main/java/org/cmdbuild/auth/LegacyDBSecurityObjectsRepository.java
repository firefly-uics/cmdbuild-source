package org.cmdbuild.auth;

import org.cmdbuild.dao.view.CMDataView;


public class LegacyDBSecurityObjectsRepository extends LegacyDBUserFetcher implements SecurityObjectsRepository {

	public LegacyDBSecurityObjectsRepository(final CMDataView view) {
		super(view);
	}

}
