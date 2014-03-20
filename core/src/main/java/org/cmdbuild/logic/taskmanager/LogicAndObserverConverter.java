package org.cmdbuild.logic.taskmanager;

import org.cmdbuild.data.view.ObservableDataView.Observer;

public interface LogicAndObserverConverter {

	interface LogicAsSourceConverter {

		Observer toObserver();

	}

	LogicAsSourceConverter from(SynchronousEventTask source);

}