package org.cmdbuild.logic.data.access;

import org.cmdbuild.auth.user.OperationUser;
import org.cmdbuild.dao.view.CMDataView;
import org.cmdbuild.data.store.lookup.LookupStore;
import org.cmdbuild.logic.data.access.lock.LockCardManager;

public class UserDataAccessLogicBuilder extends DataAccessLogicBuilder {

	public UserDataAccessLogicBuilder( //
			final CMDataView systemDataView, //
			final LookupStore lookupStore, //
			final CMDataView dataView, //
			final OperationUser operationUser, //
			final LockCardManager lockCardManager //
	) {
		super(systemDataView, lookupStore, dataView, operationUser, lockCardManager);
	}

}
