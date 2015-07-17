package org.cmdbuild.logic.data.access;

import org.cmdbuild.auth.user.OperationUser;
import org.cmdbuild.common.Builder;
import org.cmdbuild.dao.view.CMDataView;
import org.cmdbuild.data.store.lookup.LookupStore;
import org.cmdbuild.logic.data.access.lock.LockManager;

public abstract class DataAccessLogicBuilder implements Builder<DataAccessLogic> {

	private final CMDataView systemDataView;
	private final LookupStore lookupStore;
	private final CMDataView dataView;
	private final CMDataView strictDataView;
	private final OperationUser operationUser;
	private final LockManager lockCardManager;

	protected DataAccessLogicBuilder( //
			final CMDataView systemDataView, //
			final LookupStore lookupStore, //
			final CMDataView dataView, //
			final CMDataView strictDataView, //
			final OperationUser operationUser, //
			final LockManager lockCardManager //
	) {
		this.systemDataView = systemDataView;
		this.lookupStore = lookupStore;
		this.dataView = dataView;
		this.strictDataView = strictDataView;
		this.operationUser = operationUser;
		this.lockCardManager = lockCardManager;
	}

	@Override
	public DataAccessLogic build() {
		return new DefaultDataAccessLogic( //
				systemDataView, //
				lookupStore, //
				dataView, //
				strictDataView, //
				operationUser, //
				lockCardManager);
	}

}
