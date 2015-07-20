package org.cmdbuild.logic.data.access;

import org.cmdbuild.auth.user.OperationUser;
import org.cmdbuild.dao.view.CMDataView;
import org.cmdbuild.data.store.lookup.LookupStore;
import org.cmdbuild.logic.data.access.lock.LockManager;

public class SoapDataAccessLogicBuilder extends DataAccessLogicBuilder {

	public SoapDataAccessLogicBuilder( //
			final CMDataView systemDataView, //
			final LookupStore lookupStore, //
			final CMDataView dataView, //
			final CMDataView strictDataView, //
			final OperationUser operationUser, //
			final LockManager lockManager //
	) {
		super(systemDataView, lookupStore, dataView, strictDataView, operationUser, lockManager);
	}

}
