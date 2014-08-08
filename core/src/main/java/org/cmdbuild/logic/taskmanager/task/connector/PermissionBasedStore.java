package org.cmdbuild.logic.taskmanager.task.connector;

import org.cmdbuild.services.sync.store.Entry;
import org.cmdbuild.services.sync.store.ForwardingStore;
import org.cmdbuild.services.sync.store.Store;
import org.cmdbuild.services.sync.store.Type;

class PermissionBasedStore extends ForwardingStore {

	public static interface Permission {

		boolean allowsCreate(Entry<? extends Type> entry);

		boolean allowsUpdate(Entry<? extends Type> entry);

		boolean allowsDelete(Entry<? extends Type> entry);

	}

	private final PermissionBasedStore.Permission permission;

	public PermissionBasedStore(final Store delegate, final PermissionBasedStore.Permission permission) {
		super(delegate);
		this.permission = permission;
	}

	@Override
	public void create(final Entry<? extends Type> entry) {
		if (permission.allowsCreate(entry)) {
			super.create(entry);
		}
	}

	@Override
	public void update(final Entry<? extends Type> entry) {
		if (permission.allowsUpdate(entry)) {
			super.update(entry);
		}
	}

	@Override
	public void delete(final Entry<? extends Type> entry) {
		if (permission.allowsDelete(entry)) {
			super.delete(entry);
		}
	}

}