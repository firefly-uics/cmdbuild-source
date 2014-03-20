package org.cmdbuild.data.view;

import org.cmdbuild.dao.entry.CMCard;
import org.cmdbuild.dao.entry.CMCard.CMCardDefinition;
import org.cmdbuild.dao.entry.ForwardingCardDefinition;
import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.dao.view.CMDataView;
import org.cmdbuild.dao.view.ForwardingDataView;

public class ObservableDataView extends ForwardingDataView {

	public static interface Observer {

		void afterCreate(CMCard card);

		void beforeUpdate(CMCard actual, CMCard next);

		void afterUpdate(CMCard previous, CMCard actual);

		void beforeDelete(CMCard card);

	}

	public static abstract class ForwardingObserver implements Observer {

		private final Observer delegate;

		protected ForwardingObserver(final Observer delegate) {
			this.delegate = delegate;
		}

		@Override
		public void afterCreate(final CMCard card) {
			delegate.afterCreate(card);
		}

		@Override
		public void beforeUpdate(final CMCard actual, final CMCard next) {
			delegate.beforeUpdate(actual, next);
		}

		@Override
		public void afterUpdate(final CMCard previous, final CMCard actual) {
			delegate.afterUpdate(previous, actual);
		}

		@Override
		public void beforeDelete(final CMCard card) {
			delegate.beforeDelete(card);
		}

	}

	private abstract static class ObservableCardDefinition extends ForwardingCardDefinition {

		protected final Observer observer;

		protected ObservableCardDefinition(final CMCardDefinition delegate, final Observer observer) {
			super(delegate);
			this.observer = observer;
		}

	}

	private static class ObservableNewCardDefinition extends ObservableCardDefinition {

		protected ObservableNewCardDefinition(final CMCardDefinition delegate, final Observer observer) {
			super(delegate, observer);
		}

		@Override
		public CMCard save() {
			final CMCard card = super.save();
			observer.afterCreate(card);
			return card;
		}

	}

	private static class ObservableExistingCardDefinition extends ObservableCardDefinition {

		private final CMCard actual;

		protected ObservableExistingCardDefinition(final CMCard actual, final CMCardDefinition delegate,
				final Observer observer) {
			super(delegate, observer);
			this.actual = actual;
		}

		@Override
		public CMCard save() {
			final CMCard card = super.save();
			observer.beforeUpdate(actual, card);
			observer.afterUpdate(actual, card);
			return card;
		}
	}

	private final Observer observer;

	public ObservableDataView(final CMDataView delegate, final Observer observer) {
		super(delegate);
		this.observer = observer;
	}

	@Override
	public CMCardDefinition createCardFor(final CMClass type) {
		return new ObservableNewCardDefinition(super.createCardFor(type), observer);
	}

	@Override
	public CMCardDefinition update(final CMCard card) {
		return new ObservableExistingCardDefinition(card, super.update(card), observer);
	}

	@Override
	public void delete(final CMCard card) {
		observer.beforeDelete(card);
		super.delete(card);
	}

}
