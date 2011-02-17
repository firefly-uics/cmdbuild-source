package org.cmdbuild.connector.differ;

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang.Validate;
import org.apache.log4j.Logger;
import org.cmdbuild.connector.logger.Log;

abstract class AbstractDiffer<T, V> implements Differ<T, V> {

	protected static final Logger logger = Log.DIFFER;

	enum DifferAction {
		ADD, REMOVE, MODIFY
	}

	protected final T customerItem;
	protected final T cmdbuildItem;

	protected final Set<DifferListener<V>> listeners;

	public AbstractDiffer(final T customerItem, final T cmdbuildItem) {
		Validate.notNull(customerItem, "null customer item");
		Validate.notNull(cmdbuildItem, "null cmdbuild item");
		this.customerItem = customerItem;
		this.cmdbuildItem = cmdbuildItem;
		this.listeners = new HashSet<DifferListener<V>>();
	}

	@Override
	public void addListener(final DifferListener<V> listener) {
		if (listener != null) {
			listeners.add(listener);
		}
	}

	@Override
	public void removeListener(final DifferListener<V> listener) {
		listeners.remove(listener);
	}

	protected void fireAddItem(final V item) {
		Validate.notNull(item, "null item");
		final DifferEvent<V> event = new DifferEvent<V>(this, item);
		fireItemAction(DifferAction.ADD, event);
	}

	protected void fireRemoveItem(final V item) {
		Validate.notNull(item, "null item");
		final DifferEvent<V> event = new DifferEvent<V>(this, item);
		fireItemAction(DifferAction.REMOVE, event);
	}

	protected void fireModifyItem(final V item) {
		Validate.notNull(item, "null item");
		final DifferEvent<V> event = new DifferEvent<V>(this, item);
		fireItemAction(DifferAction.MODIFY, event);
	}

	protected void fireItemAction(final DifferAction action, final DifferEvent<V> event) {
		for (final DifferListener<V> listener : listeners) {
			switch (action) {
			case ADD:
				listener.addItem(event);
				break;
			case REMOVE:
				listener.removeItem(event);
				break;
			case MODIFY:
				listener.modifyItem(event);
				break;
			}
		}
	}

}
