package org.cmdbuild.connector.differ;

import java.util.EventListener;

public interface DifferListener<T> extends EventListener {

	public void addItem(final DifferEvent<T> event);

	public void removeItem(final DifferEvent<T> event);

	public void modifyItem(final DifferEvent<T> event);

}
