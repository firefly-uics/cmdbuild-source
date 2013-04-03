package org.cmdbuild.logic.widget;

import java.util.List;

import org.cmdbuild.data.converter.WidgetConverter;
import org.cmdbuild.data.store.DataViewStore;
import org.cmdbuild.data.store.Store;
import org.cmdbuild.data.store.Store.Storable;
import org.cmdbuild.logic.Logic;
import org.cmdbuild.logic.TemporaryObjectsBeforeSpringDI;
import org.cmdbuild.model.widget.Widget;

public class WidgetLogic implements Logic {

	private final DataViewStore<Widget> widgetStore;

	public WidgetLogic() {
		widgetStore = getWidgetStore();
	}

	public List<Widget> getAllWidgets() {
		final List<Widget> fetchedWidgets = widgetStore.list();
		return fetchedWidgets;
	}

	public Widget getWidget(final Long widgetId) {
		return widgetStore.read(new Storable() {
			@Override
			public String getIdentifier() {
				return widgetId.toString();
			}
		});
	}

	public Widget createWidget(final Widget widgetToCreate) {
		return widgetStore.read(widgetStore.create(widgetToCreate));
	}

	public void updateWidget(final Widget widgetToUpdate) {
		widgetStore.update(widgetToUpdate);
	}

	public void deleteWidget(final Long widgetId) {
		final Storable storableToDelete = new Store.Storable() {
			@Override
			public String getIdentifier() {
				return Long.toString(widgetId);
			}
		};
		widgetStore.delete(storableToDelete);
	}

	private DataViewStore<Widget> getWidgetStore() {
		final WidgetConverter converter = new WidgetConverter();
		return new DataViewStore<Widget>(TemporaryObjectsBeforeSpringDI.getSystemView(), converter);
	}

	public void executeWidget() {
		throw new UnsupportedOperationException();
	}

}
