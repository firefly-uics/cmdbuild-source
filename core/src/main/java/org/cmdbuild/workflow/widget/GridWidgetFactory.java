package org.cmdbuild.workflow.widget;

import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;
import static org.apache.commons.lang3.StringUtils.defaultIfBlank;
import static org.cmdbuild.model.widget.Grid.DEFAULT_ENTRY_SEPARATOR;
import static org.cmdbuild.model.widget.Grid.DEFAULT_KEYVALUE_SEPARATOR;
import static org.cmdbuild.model.widget.Grid.DEFAULT_MAP_SEPARATOR;
import static org.cmdbuild.model.widget.Grid.DEFAULT_SERIALIZATION;
import static org.cmdbuild.model.widget.Grid.DEFAULT_WRITE_ON_ADVANCE;

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
	public static final String SERIALIZATION_TYPE = "SerializationType";
	public static final String WRITE_ON_ADVANCE = "WriteOnAdvance";
	
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
		Validate.notEmpty(className, "{} is required", CLASS_NAME);
		final Grid widget = new Grid();
		widget.setClassName(className);
		widget.setPreset(extractUnmanagedParameters(valueMap, KNOWN_PARAMETERS));
		widget.setOutputName(readString(valueMap.get(OUTPUT_KEY)));
		widget.setMapSeparator(defaultIfBlank(readString(valueMap.get(MAP_SEPARATOR)),DEFAULT_MAP_SEPARATOR));
		widget.setEntrySeparator(defaultIfBlank(readString(valueMap.get(ENTRY_SEPARATOR)),DEFAULT_ENTRY_SEPARATOR));
		widget.setKeyValueSeparator(defaultIfBlank(readString(valueMap.get(KEY_VALUE_SEPARATOR)),DEFAULT_KEYVALUE_SEPARATOR));
		widget.setSerializationType(defaultIfBlank(readString(valueMap.get(SERIALIZATION_TYPE)),DEFAULT_SERIALIZATION));
		widget.setWriteOnAdvance(defaultIfNull(readBooleanTrueIfTrue(valueMap.get(WRITE_ON_ADVANCE)), DEFAULT_WRITE_ON_ADVANCE));
		return widget;
	}

}
