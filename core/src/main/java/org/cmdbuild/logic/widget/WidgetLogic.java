package org.cmdbuild.logic.widget;

import static org.cmdbuild.data.store.Storables.storableOf;

import java.util.Collection;

import org.cmdbuild.data.store.Storable;
import org.cmdbuild.data.store.Store;
import org.cmdbuild.logic.Logic;
import org.cmdbuild.model.widget.Widget;

public class WidgetLogic implements Logic {

	private final Store<Widget> store;

	public WidgetLogic(final Store<Widget> store) {
		this.store = store;
	}

	public Collection<Widget> getAllWidgets() {
		return store.readAll();
	}

	public Widget getWidget(final Long widgetId) {
		return store.read(storableOf(widgetId));
	}

	public Widget createWidget(final Widget widgetToCreate) {
		return store.read(store.create(widgetToCreate));
	}

	public void updateWidget(final Widget widgetToUpdate) {
		store.update(widgetToUpdate);
	}

	public void deleteWidget(final Long widgetId) {
		final Storable storableToDelete = storableOf((widgetId));
		store.delete(storableToDelete);
	}

}
