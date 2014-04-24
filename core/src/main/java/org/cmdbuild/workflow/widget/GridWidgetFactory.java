package org.cmdbuild.workflow.widget;

import java.util.Map;

import org.apache.commons.lang3.Validate;
import org.cmdbuild.model.widget.Grid;
import org.cmdbuild.model.widget.Widget;
import org.cmdbuild.notification.Notifier;
import org.cmdbuild.services.template.store.TemplateRepository;

public class GridWidgetFactory extends ValuePairWidgetFactory {

	private static final String WIDGET_NAME = "grid";

	private static final String CLASS_NAME = "ClassName";
	public static final String MAP_SEPARATOR = "CardSeparator";
	public static final String ENTRY_SEPARATOR = "AttributeSeparator";
	public static final String KEY_VALUE_SEPARATOR = "KeyValueSeparator";
	
	private static final String[] KNOWN_PARAMETERS = { BUTTON_LABEL, CLASS_NAME };

	public GridWidgetFactory(final TemplateRepository templateRespository, final Notifier notifier) {
		super(templateRespository, notifier);
	}

	@Override
	public String getWidgetName() {
		return WIDGET_NAME;
	}

	@Override
	protected Widget createWidget(final Map<String, Object> valueMap) {
		final String className = readString(valueMap.get(CLASS_NAME));
		Validate.notEmpty(className, CLASS_NAME + " is required");
		final Grid widget = new Grid();
		widget.setClassName(className);
		widget.setPreset(extractUnmanagedParameters(valueMap, KNOWN_PARAMETERS));
		widget.setOutputName(readString(valueMap.get(OUTPUT_KEY)));
		widget.setMapSeparator(readString(valueMap.get(MAP_SEPARATOR)));
		widget.setEntrySeparator(readString(valueMap.get(ENTRY_SEPARATOR)));
		widget.setKeyValueSeparator(readString(valueMap.get(KEY_VALUE_SEPARATOR)));
		return widget;
	}

}
