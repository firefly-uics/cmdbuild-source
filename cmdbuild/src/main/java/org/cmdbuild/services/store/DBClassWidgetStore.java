package org.cmdbuild.services.store;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.cmdbuild.common.annotations.OldDao;
import org.cmdbuild.elements.interfaces.ICard;
import org.cmdbuild.elements.interfaces.ITable;
import org.cmdbuild.exception.ORMException.ORMExceptionType;
import org.cmdbuild.logger.Log;
import org.cmdbuild.model.widget.Widget;
import org.cmdbuild.services.meta.MetadataService;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;

public class DBClassWidgetStore {

	private static final String WIDGETS_META = MetadataService.SYSTEM_PREFIX + ".widgets";

	private static final Object GLOBAL_WIDGET_LOCK = new Object();

	@OldDao
	private final ITable table;

	public DBClassWidgetStore(final ITable table) {
		this.table = table;
	}

	public List<Widget> getWidgets() {
		return loadWidgets();
	}

	public Object executeAction(final String widgetId, final String action, final Map<String, Object> params,
			final ICard card) throws Exception {
		synchronized (GLOBAL_WIDGET_LOCK) {
			final List<Widget> widgets = loadWidgets();
			int index = findIndexById(widgets, widgetId);
			if (index < 0) {
				throw new IllegalArgumentException("Widget not found");
			}
			return widgets.get(index).executeAction(action, params, varMap(card));
		}
	}

	private Map<String, Object> varMap(ICard card) {
		final Map<String, Object> varMap = new HashMap<String, Object>();
		for (final String key : card.getAttributeValueMap().keySet()) {
			varMap.put(key, card.getValue(key));
		}
		return varMap;
	}

	public void saveWidget(final Widget widget) {
		if (widget.getId() == null) {
			final String newId = UUID.randomUUID().toString();
			widget.setId(newId);
			addWidget(widget);
		} else {
			modifyWidget(widget);
		}
	}

	public void addWidget(final Widget newWidget) {
		synchronized (GLOBAL_WIDGET_LOCK) {
			final List<Widget> widgets = loadWidgets();
			widgets.add(newWidget);
			saveWidgets(widgets);
		}
	}

	public void modifyWidget(final Widget modifiedWidget) {
		synchronized (GLOBAL_WIDGET_LOCK) {
			final List<Widget> widgets = loadWidgets();
			int index = findIndexById(widgets, modifiedWidget.getId());
			if (index < 0) {
				throw new IllegalArgumentException("Widget not found");
			}
			widgets.set(index, modifiedWidget);
			saveWidgets(widgets);
		}
	}

	private int findIndexById(final List<Widget> widgets, final String id) {
		int i = 0;
		for (Widget w : widgets) {
			if (w.getId().equals(id)) {
				return i;
			}
			++i;
		}
		return -1;
	}

	public void removeWidget(final String id) {
		synchronized (GLOBAL_WIDGET_LOCK) {
			final List<Widget> widgets = loadWidgets();
			for (Iterator<Widget> i = widgets.iterator(); i.hasNext();) {
				final Widget w = i.next();
				if (w.getId().equals(id)) {
					i.remove();
					break;
				}
			}
			saveWidgets(widgets);
		}
	}

	private List<Widget> loadWidgets() {
		List<Widget> widgets = new ArrayList<Widget>();
		final Object widgetMeta = MetadataService.of(table).getMetadata(WIDGETS_META);
		if (widgetMeta != null) {
			try {
				ObjectMapper mapper = new ObjectMapper();
				widgets = mapper.readValue(widgetMeta.toString(), new TypeReference<List<Widget>>() {
				});
			} catch (final Exception e) {
				Log.PERSISTENCE.warn("Unable to load widget list", e);
			}
		}
		return widgets;
	}

	private void saveWidgets(final List<Widget> widgets) {
		checkForUnicity(widgets);
		final String widgetString;
		if (widgets.isEmpty()) {
			widgetString = null;
		} else {
			try {
				final ObjectMapper mapper = new ObjectMapper();
				widgetString = mapper.writeValueAsString(widgets);
			} catch (Exception e) {
				Log.PERSISTENCE.error("Unable to serialize the class widgets");
				throw ORMExceptionType.ORM_GENERIC_ERROR.createException();
			}
		}
		MetadataService.of(table).updateMetadata(WIDGETS_META, widgetString);
	}

	private void checkForUnicity(List<Widget> widgets) throws IllegalStateException {
		final Set<String> idSet = new HashSet<String>();
		for (final Widget w : widgets) {
			if (idSet.contains(w.getId())) {
				throw new IllegalStateException("Duplicate widgets");
			}
			idSet.add(w.getId());
		}
	}
}
