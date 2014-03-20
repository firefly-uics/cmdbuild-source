package org.cmdbuild.logic.taskmanager;

import org.cmdbuild.dao.entry.CMCard;
import org.cmdbuild.data.view.ObservableDataView.Observer;
import org.slf4j.Logger;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

public class DefaultLogicAndObserverConverter implements LogicAndObserverConverter {

	private static final Logger logger = TaskManagerLogic.logger;
	private static final Marker marker = MarkerFactory.getMarker(DefaultLogicAndObserverConverter.class.getName());

	private static class DefaultLogicAsSourceConverter implements LogicAsSourceConverter {

		private final SynchronousEventTask source;

		public DefaultLogicAsSourceConverter(final SynchronousEventTask source) {
			this.source = source;
		}

		@Override
		public Observer toObserver() {
			return new Observer() {

				@Override
				public void afterCreate(final CMCard card) {
					logger.info(marker, "card created - '{}', '{}'", source, card);
				}

				@Override
				public void beforeUpdate(CMCard actual, CMCard next) {
					logger.info(marker, "card will be updated - '{}', actual '{}', next '{}'", source, actual, next);
				}

				@Override
				public void afterUpdate(CMCard previous, CMCard actual) {
					logger.info(marker, "card updated - '{}', actual '{}', previous '{}'", source, actual, previous);
				}

				@Override
				public void beforeDelete(CMCard card) {
					logger.info(marker, "card will be deleted - '{}', '{}'", source, card);
				}

			};
		}
	}

	@Override
	public LogicAsSourceConverter from(final SynchronousEventTask source) {
		return new DefaultLogicAsSourceConverter(source);
	}

}
