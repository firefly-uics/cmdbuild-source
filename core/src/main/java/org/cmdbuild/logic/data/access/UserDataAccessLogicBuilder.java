package org.cmdbuild.logic.data.access;

import org.cmdbuild.auth.user.OperationUser;
import org.cmdbuild.dao.view.CMDataView;
import org.cmdbuild.data.store.lookup.LookupStore;
import org.cmdbuild.logic.data.access.lock.LockCardManager;
import org.cmdbuild.spring.annotations.LogicComponent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

@LogicComponent
public class UserDataAccessLogicBuilder extends DataAccessLogicBuilder {

	@Autowired
	public UserDataAccessLogicBuilder( //
			@Qualifier("system") final CMDataView systemDataView, //
			final LookupStore lookupStore, //
			@Qualifier("user") final CMDataView dataView, //
			final OperationUser operationUser, //
			@Qualifier("user") final LockCardManager lockCardManager //
	) {
		super(systemDataView, lookupStore, dataView, operationUser, lockCardManager);
	}

}
