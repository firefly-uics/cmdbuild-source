package org.cmdbuild.logic.widget;

import static org.cmdbuild.data.store.Storables.storableOf;

import java.util.Collection;

import org.cmdbuild.dao.view.CMDataView;
import org.cmdbuild.data.converter.WidgetConverter;
import org.cmdbuild.data.store.Storable;
import org.cmdbuild.data.store.dao.DataViewStore;
import org.cmdbuild.logic.Logic;
import org.cmdbuild.model.widget.Widget;

public class WidgetLogic implements Logic {

	private final DataViewStore<Widget> widgetStore;

	public WidgetLogic(final CMDataView dataView) {
		final WidgetConverter converter = new WidgetConverter();
		widgetStore = DataViewStore.newInstance(dataView, converter);
	}

	public Collection<Widget> getAllWidgets() {
		return widgetStore.readAll();
	}

	public Widget getWidget(final Long widgetId) {
		return widgetStore.read(storableOf(widgetId));
	}

	public Widget createWidget(final Widget widgetToCreate) {
		return widgetStore.read(widgetStore.create(widgetToCreate));
	}

	public void updateWidget(final Widget widgetToUpdate) {
		widgetStore.update(widgetToUpdate);
	}

	public void deleteWidget(final Long widgetId) {
		final Storable storableToDelete = storableOf((widgetId));
		widgetStore.delete(storableToDelete);
	}

	public void executeWidget() {
		throw new UnsupportedOperationException();
	}

}
