package org.cmdbuild.logic.widget;

import java.util.List;

import org.cmdbuild.dao.view.CMDataView;
import org.cmdbuild.data.converter.WidgetConverter;
import org.cmdbuild.data.store.DataViewStore;
import org.cmdbuild.data.store.Store;
import org.cmdbuild.data.store.Store.Storable;
import org.cmdbuild.logic.Logic;
import org.cmdbuild.model.widget.Widget;
import org.springframework.beans.factory.annotation.Autowired;

public class WidgetLogic implements Logic {

	private final DataViewStore<Widget> widgetStore;

	@Autowired
	public WidgetLogic(final CMDataView dataView) {
		final WidgetConverter converter = new WidgetConverter();
		widgetStore = new DataViewStore<Widget>(dataView, converter);
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

	public void executeWidget() {
		throw new UnsupportedOperationException();
	}

}
