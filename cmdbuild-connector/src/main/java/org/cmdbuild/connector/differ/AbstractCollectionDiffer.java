package org.cmdbuild.connector.differ;

import java.util.Iterator;
import java.util.SortedSet;

public abstract class AbstractCollectionDiffer<T extends Comparable<T>> extends AbstractDiffer<SortedSet<T>, T> {

	public AbstractCollectionDiffer(final SortedSet<T> customerItem, final SortedSet<T> cmdbuildItem) {
		super(customerItem, cmdbuildItem);
	}

	@Override
	public void diff() throws DifferException {
		diff(customerItem.iterator(), cmdbuildItem.iterator());
	}

	private void diff(final Iterator<T> customerIterator, final Iterator<T> cmdbuildIterator) throws DifferException {

		final LoggerHelper loggerHelper = new LoggerHelper();

		preDiffCollections(customerIterator, cmdbuildIterator);

		while (customerIterator.hasNext() || cmdbuildIterator.hasNext()) {
			if (!customerIterator.hasNext()) {
				logger.debug("customer queue ended, removing cmdbuild element");
				fireRemoveItem(cmdbuildIterator.next());
				loggerHelper.increaseCMDBuild();
				loggerHelper.log();
			} else if (!cmdbuildIterator.hasNext()) {
				logger.debug("cmdbuild queue ended, adding customer element");
				fireAddItem(customerIterator.next());
				loggerHelper.increaseExternal();
				loggerHelper.log();
			}

			T customerLast = null;
			T cmdbuildLast = null;
			while (customerIterator.hasNext() && cmdbuildIterator.hasNext()) {
				final T customerCurrent = (customerLast != null) ? customerLast : customerIterator.next();
				final T cmdbuildCurrent = (cmdbuildLast != null) ? cmdbuildLast : cmdbuildIterator.next();

				customerLast = null;
				cmdbuildLast = null;

				preCompareElements(customerCurrent, cmdbuildCurrent);

				loggerHelper.log();
				final int compare = customerCurrent.compareTo(cmdbuildCurrent);
				if (compare < 0) {
					logger.debug("customer element lower than cmdbuild element, adding customer element");
					fireAddItem(customerCurrent);
					cmdbuildLast = cmdbuildCurrent;
					loggerHelper.increaseExternal();
				} else if (compare > 0) {
					logger.debug("customer element greater than cmdbuild element, removing cmdbuild element");
					fireRemoveItem(cmdbuildCurrent);
					customerLast = customerCurrent;
					loggerHelper.increaseCMDBuild();
				} else {
					logger.debug("customer element equals cmdbuild element, calculating differences");
					preDiffElements(customerCurrent, cmdbuildCurrent);
					diff(customerCurrent, cmdbuildCurrent);
					loggerHelper.increaseAll();
				}
			}
		}
	}

	protected void preDiffCollections(final Iterator<T> customerIterator, final Iterator<T> cmdbuildIterator) {
		// stub
	}

	protected void preCompareElements(final T customerElement, final T cmdbuildElement) {
		// stub
	}

	protected void preDiffElements(final T customerElement, final T cmdbuildElement) {
		// stub
	}

	private void diff(final T customerElement, final T cmdbuildElement) throws DifferException {
		final AbstractItemDiffer<T> differ = getItemDiffer(customerElement, cmdbuildElement);
		differ.addListener(new DifferAdapter<T>() {

			@Override
			public void addItem(final DifferEvent<T> event) {
				fireAddItem(event.getValue());
			}

			@Override
			public void removeItem(final DifferEvent<T> event) {
				fireRemoveItem(event.getValue());
			}

			@Override
			public void modifyItem(final DifferEvent<T> event) {
				fireModifyItem(event.getValue());
			}

		});
		differ.diff();
	}

	protected abstract AbstractItemDiffer<T> getItemDiffer(final T customerElement, final T cmdbuildElement);

	private class LoggerHelper {

		private int externalCount;
		private int cmdbuildCount;

		public LoggerHelper() {
			externalCount = 0;
			cmdbuildCount = 0;
		}

		public void increaseExternal() {
			externalCount++;
		}

		public void increaseCMDBuild() {
			cmdbuildCount++;
		}

		public void increaseAll() {
			increaseExternal();
			increaseCMDBuild();
		}

		public void log() {
			logger.debug("external cards count #" + (externalCount));
			logger.debug("cmdbuild cards count #" + (cmdbuildCount));
		}

	}

}
