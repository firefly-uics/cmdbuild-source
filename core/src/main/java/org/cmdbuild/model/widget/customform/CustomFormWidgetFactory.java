package org.cmdbuild.model.widget.customform;

import static java.lang.String.format;
import static org.apache.commons.lang3.BooleanUtils.toBoolean;
import static org.apache.commons.lang3.StringUtils.defaultIfBlank;
import static org.apache.commons.lang3.StringUtils.defaultString;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

import java.util.Map;

import org.apache.commons.lang3.Validate;
import org.cmdbuild.dao.view.CMDataView;
import org.cmdbuild.model.widget.Widget;
import org.cmdbuild.model.widget.customform.CustomForm.Capabilities;
import org.cmdbuild.model.widget.customform.CustomForm.Serialization;
import org.cmdbuild.model.widget.customform.CustomForm.Serialization.Configuration;
import org.cmdbuild.model.widget.customform.CustomForm.TextConfiguration;
import org.cmdbuild.notification.Notifier;
import org.cmdbuild.services.meta.MetadataStoreFactory;
import org.cmdbuild.services.template.store.TemplateRepository;
import org.cmdbuild.workflow.widget.ValuePairWidgetFactory;

public class CustomFormWidgetFactory extends ValuePairWidgetFactory {

	private static final String WIDGET_NAME = "customForm";

	public static final String //
			REQUIRED = "Required", //
			READ_ONLY = "ReadOnly", //
			CONFIGURATION_TYPE = "ConfigurationType", //
			FORM = "Form", //
			CLASSNAME = "ClassName", //
			FUNCTIONNAME = "FunctionName", //
			LAYOUT = "Layout", //
			ADD_DISABLED = "AddRowDisabled", //
			DELETE_DISABLED = "DeleteRowDisabled", //
			IMPORT_DISABLED = "ImportCsvDisabled", //
			MODIFY_DISABLED = "ModifyRowDisabled", //
			SERIALIZATION_TYPE = "SerializationType", //
			KEY_VALUE_SEPARATOR = "KeyValueSeparator", //
			ATTRIBUTES_SEPARATOR = "AttributesSeparator", //
			ROWS_SEPARATOR = "RowsSeparator";

	private static final String[] KNOWN_PARAMETERS = { BUTTON_LABEL, REQUIRED, READ_ONLY, //
			CONFIGURATION_TYPE, //
			FORM, //
			CLASSNAME, //
			LAYOUT, //
			ADD_DISABLED, DELETE_DISABLED, IMPORT_DISABLED, MODIFY_DISABLED, //
			SERIALIZATION_TYPE, KEY_VALUE_SEPARATOR, ATTRIBUTES_SEPARATOR, ROWS_SEPARATOR //
	};

	private static final String //
			TYPE_FORM = "form", //
			TYPE_CLASS = "class", //
			TYPE_FUNCTION = "function";

	private static final String //
			JSON_SERIALIZATION = "json", //
			TEXT_SERIALIZATION = "text";

	public static final String //
			DEFAULT_KEY_VALUE_SEPARATOR = "=", //
			DEFAULT_ATTRIBUTES_SEPARATOR = ",", //
			DEFAULT_ROWS_SEPARATOR = "\n";

	private final CMDataView dataView;
	private final MetadataStoreFactory metadataStoreFactory;

	public CustomFormWidgetFactory(final TemplateRepository templateRespository, final Notifier notifier,
			final CMDataView dataView, final MetadataStoreFactory metadataStoreFactory) {
		super(templateRespository, notifier);
		this.dataView = dataView;
		this.metadataStoreFactory = metadataStoreFactory;
	}

	@Override
	public String getWidgetName() {
		return WIDGET_NAME;
	}

	@Override
	protected Widget createWidget(final Map<String, Object> valueMap) {
		final CustomForm widget = new CustomForm();
		widget.setRequired(readBooleanFalseIfMissing(valueMap.get(REQUIRED)));
		widget.setReadOnly(readBooleanFalseIfMissing(valueMap.get(READ_ONLY)));
		widget.setForm(formBuilderOf(valueMap).build());
		widget.setLayout(String.class.cast(valueMap.get(LAYOUT)));
		widget.setCapabilities(capabilitiesOf(valueMap));
		widget.setSerialization(serializationOf(valueMap));
		widget.setVariables(extractUnmanagedParameters(valueMap, KNOWN_PARAMETERS));
		return widget;
	}

	private FormBuilder formBuilderOf(final Map<String, Object> valueMap) {
		final FormBuilder output;
		final String configurationType = String.class.cast(valueMap.get(CONFIGURATION_TYPE));
		if (TYPE_FORM.equalsIgnoreCase(configurationType)) {
			final String expression = defaultString(String.class.cast(valueMap.get(FORM)));
			Validate.isTrue(isNotBlank(expression), "invalid value for '%s'", FORM);
			output = new FallbackOnExceptionFormBuilder(new JsonStringFormBuilder(expression), new IdentityFormBuilder(
					expression));
		} else if (TYPE_CLASS.equalsIgnoreCase(configurationType)) {
			final String className = String.class.cast(valueMap.get(CLASSNAME));
			Validate.isTrue(isNotBlank(className), "invalid value for '%s'", CLASSNAME);
			output = new ClassFormBuilder(dataView, metadataStoreFactory, className);
		} else if (TYPE_FUNCTION.equalsIgnoreCase(configurationType)) {
			final String functionName = String.class.cast(valueMap.get(FUNCTIONNAME));
			Validate.isTrue(isNotBlank(functionName), "invalid value for '%s'", FUNCTIONNAME);
			output = new FunctionFormBuilder(dataView, functionName);
		} else {
			output = new InvalidFormBuilder(format("'%s' is not a valid value for '%s'", CONFIGURATION_TYPE));
		}
		return output;
	}

	private Capabilities capabilitiesOf(final Map<String, Object> valueMap) {
		final Capabilities output = new Capabilities();
		output.setAddDisabled(toBoolean(String.class.cast(valueMap.get(ADD_DISABLED))));
		output.setDeleteDisabled(toBoolean(String.class.cast(valueMap.get(DELETE_DISABLED))));
		output.setImportDisabled(toBoolean(String.class.cast(valueMap.get(IMPORT_DISABLED))));
		output.setModifyDisabled(toBoolean(String.class.cast(valueMap.get(MODIFY_DISABLED))));
		return output;
	}

	private Serialization serializationOf(final Map<String, Object> valueMap) {
		final Serialization output = new Serialization();
		final String type = defaultIfBlank(String.class.cast(valueMap.get(SERIALIZATION_TYPE)), TEXT_SERIALIZATION);
		final Configuration configuration;
		if (JSON_SERIALIZATION.equals(type)) {
			configuration = null;
		} else if (TEXT_SERIALIZATION.equals(type)) {
			final TextConfiguration textConfiguration = new TextConfiguration();
			textConfiguration.setKeyValueSeparator(defaultIfBlank(String.class.cast(valueMap.get(KEY_VALUE_SEPARATOR)),
					DEFAULT_KEY_VALUE_SEPARATOR));
			textConfiguration.setAttributesSeparator(defaultIfBlank(
					String.class.cast(valueMap.get(ATTRIBUTES_SEPARATOR)), DEFAULT_ATTRIBUTES_SEPARATOR));
			textConfiguration.setRowsSeparator(defaultIfBlank(String.class.cast(valueMap.get(ROWS_SEPARATOR)),
					DEFAULT_ROWS_SEPARATOR));
			configuration = textConfiguration;
		} else {
			configuration = null;
		}
		output.setType(type);
		output.setConfiguration(configuration);
		return output;
	}

}
