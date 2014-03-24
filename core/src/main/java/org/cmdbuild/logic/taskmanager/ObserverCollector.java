package org.cmdbuild.logic.taskmanager;

import org.cmdbuild.services.event.Observer;

public interface ObserverCollector {

	interface IdentifiableObserver extends Observer {

		String getIdentifier();

	}

	void add(IdentifiableObserver element);

	void remove(IdentifiableObserver element);

	public Observer allInOneObserver();

}