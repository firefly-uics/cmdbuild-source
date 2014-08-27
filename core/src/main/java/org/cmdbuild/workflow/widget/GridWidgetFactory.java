package org.cmdbuild.workflow.widget;

import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;
import static org.apache.commons.lang3.StringUtils.defaultIfBlank;
import static org.cmdbuild.model.widget.Grid.DEFAULT_ENTRY_SEPARATOR;
import static org.cmdbuild.model.widget.Grid.DEFAULT_KEYVALUE_SEPARATOR;
import static org.cmdbuild.model.widget.Grid.*;
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

	public static final String CLASS_NAME = "ClassName";
	public static final String CARD_SEPARATOR = "CardSeparator";
	public static final String ATTRIBUTE_SEPARATOR = "AttributeSeparator";
	public static final String KEY_VALUE_SEPARATOR = "KeyValueSeparator";
	public static final String SERIALIZATION_TYPE = "SerializationType";
	public static final String WRITE_ON_ADVANCE = "WriteOnAdvance";
	public static final String PRESETS = "Presets";
	public static final String PRESETS_TYPE = "PresetsType";

	private static final String[] KNOWN_PARAMETERS = { BUTTON_LABEL, CLASS_NAME, CARD_SEPARATOR, ATTRIBUTE_SEPARATOR,
			KEY_VALUE_SEPARATOR, SERIALIZATION_TYPE, WRITE_ON_ADVANCE, PRESETS, PRESETS_TYPE };

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
		widget.setOutputName(readString(valueMap.get(OUTPUT_KEY)));
		widget.setCardSeparator(defaultIfBlank(readString(valueMap.get(CARD_SEPARATOR)), DEFAULT_MAP_SEPARATOR));
		widget.setAttributeSeparator(defaultIfBlank(readString(valueMap.get(ATTRIBUTE_SEPARATOR)), DEFAULT_ENTRY_SEPARATOR));
		widget.setKeyValueSeparator(defaultIfBlank(readString(valueMap.get(KEY_VALUE_SEPARATOR)),
				DEFAULT_KEYVALUE_SEPARATOR));
		widget.setSerializationType(defaultIfBlank(readString(valueMap.get(SERIALIZATION_TYPE)), DEFAULT_SERIALIZATION));
		widget.setWriteOnAdvance(defaultIfNull(readBooleanTrueIfTrue(valueMap.get(WRITE_ON_ADVANCE)),
				DEFAULT_WRITE_ON_ADVANCE));
		widget.setPresets(readString(valueMap.get(PRESETS)));
		widget.setPresetsType(defaultIfBlank(readString(valueMap.get(PRESETS_TYPE)), DEFAULT_PRESETS_TYPE));
		widget.setVariables(extractUnmanagedParameters(valueMap, KNOWN_PARAMETERS));
		return widget;
	}

}
