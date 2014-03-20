package org.cmdbuild.logic.taskmanager;

import org.cmdbuild.dao.entry.CMCard;
import org.cmdbuild.data.view.ObservableDataView.Observer;
import org.cmdbuild.logic.taskmanager.DefaultLogicAndObserverConverter.ObserverFactory;
import org.slf4j.Logger;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

public class DefaultObserverFactory implements ObserverFactory {

	private static final Logger logger = TaskManagerLogic.logger;
	private static final Marker marker = MarkerFactory.getMarker(DefaultLogicAndObserverConverter.class.getName());

	@Override
	public Observer create(final SynchronousEventTask task) {
		return new Observer() {

			@Override
			public void afterCreate(final CMCard card) {
				logger.info(marker, "card created - '{}', '{}'", task, card);
			}

			@Override
			public void beforeUpdate(CMCard actual, CMCard next) {
				logger.info(marker, "card will be updated - '{}', actual '{}', next '{}'", task, actual, next);
			}

			@Override
			public void afterUpdate(CMCard previous, CMCard actual) {
				logger.info(marker, "card updated - '{}', actual '{}', previous '{}'", task, actual, previous);
			}

			@Override
			public void beforeDelete(CMCard card) {
				logger.info(marker, "card will be deleted - '{}', '{}'", task, card);
			}

		};
	}

}
