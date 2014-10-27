package org.cmdbuild.logic.taskmanager.task.connector;

import org.cmdbuild.services.sync.logging.LoggingSupport;
import org.cmdbuild.services.sync.store.Entry;
import org.cmdbuild.services.sync.store.ForwardingStore;
import org.cmdbuild.services.sync.store.Store;
import org.cmdbuild.services.sync.store.Type;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

class PermissionBasedStore extends ForwardingStore implements LoggingSupport {

	private static final Marker marker = MarkerFactory.getMarker(PermissionBasedStore.class.getName());

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
		} else {
			logger.debug(marker, "create not allowed");
		}
	}

	@Override
	public void update(final Entry<? extends Type> entry) {
		if (permission.allowsUpdate(entry)) {
			super.update(entry);
		} else {
			logger.debug(marker, "update not allowed");
		}
	}

	@Override
	public void delete(final Entry<? extends Type> entry) {
		if (permission.allowsDelete(entry)) {
			super.delete(entry);
		} else {
			logger.debug(marker, "delete not allowed");
		}
	}

}