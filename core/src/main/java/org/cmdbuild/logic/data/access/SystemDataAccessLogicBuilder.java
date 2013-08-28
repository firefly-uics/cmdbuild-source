package org.cmdbuild.logic.data.access;

import org.cmdbuild.auth.user.OperationUser;
import org.cmdbuild.dao.view.CMDataView;
import org.cmdbuild.data.store.lookup.LookupStore;
import org.cmdbuild.logic.data.access.lock.LockCardManager;
import org.springframework.beans.factory.annotation.Autowired;

public class SystemDataAccessLogicBuilder extends DataAccessLogicBuilder {

	@Autowired
	public SystemDataAccessLogicBuilder( //
			final CMDataView systemDataView, //
			final LookupStore lookupStore, //
			final CMDataView dataView, //
			final OperationUser operationUser, //
			final LockCardManager lockCardManager //
	) {
		super(systemDataView, lookupStore, dataView, operationUser, lockCardManager);
	}

}
