package org.cmdbuild.model.widget.customform;

import static com.google.common.base.Splitter.on;
import static java.lang.String.format;
import static org.apache.commons.lang3.StringUtils.defaultIfBlank;
import static org.apache.commons.lang3.StringUtils.defaultString;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

import java.util.Map;

import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.builder.Builder;
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
			ADD_DISABLED = "AddDisabled", //
			ATTRIBUTES_SEPARATOR = "AttributesSeparator", //
			CLASS_ATTRIBUTES = "ClassAttributes", //
			CLASS_MODEL = "ClassModel", //
			CLONE_DISABLED = "CloneDisabled", //
			DATA_TYPE = "DataType", //
			DELETE_DISABLED = "DeleteDisabled", //
			FORM_MODEL = "FormModel", //
			FUNCTION_ATTRIBUTES = "FunctionAttributes", //
			FUNCTION_DATA = "FunctionData", //
			FUNCTION_MODEL = "FunctionModel", //
			IMPORT_DISABLED = "ImportDisabled", //
			KEY_VALUE_SEPARATOR = "KeyValueSeparator", //
			LAYOUT = "Layout", //
			MODEL_TYPE = "ModelType", //
			MODIFY_DISABLED = "ModifyDisabled", //
			RAW_DATA = "RawData", //
			READ_ONLY = "ReadOnly", //
			REQUIRED = "Required", //
			ROWS_SEPARATOR = "RowsSeparator", //
			SERIALIZATION_TYPE = "SerializationType", //
			TEMPLATE_RESOLVER = "TemplateResolver" // TODO use meaningful name
			;

	private static final String[] KNOWN_PARAMETERS = { BUTTON_LABEL, REQUIRED, //
			MODEL_TYPE, FORM_MODEL, CLASS_MODEL, CLASS_ATTRIBUTES, FUNCTION_MODEL, FUNCTION_ATTRIBUTES, //
			DATA_TYPE, RAW_DATA, FUNCTION_DATA, //
			TEMPLATE_RESOLVER, //
			LAYOUT, //
			READ_ONLY, ADD_DISABLED, DELETE_DISABLED, IMPORT_DISABLED, MODIFY_DISABLED, CLONE_DISABLED, //
			SERIALIZATION_TYPE, KEY_VALUE_SEPARATOR, ATTRIBUTES_SEPARATOR, ROWS_SEPARATOR //
	};

	private static final String //
			TYPE_FORM = "form", //
			TYPE_CLASS = "class", //
			TYPE_FUNCTION = "function", //
			TYPE_RAW = "raw", //
			TYPE_RAW_JSON = "raw_json", //
			TYPE_RAW_TEXT = "raw_text";

	private static final String //
			JSON_SERIALIZATION = "json", //
			TEXT_SERIALIZATION = "text";

	public static final String //
			DEFAULT_ATTRIBUTES_SEPARATOR = ",", //
			DEFAULT_KEY_VALUE_SEPARATOR = "=", //
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
		widget.setOutputName(readString(valueMap.get(OUTPUT_KEY)));
		widget.setRequired(readBooleanFalseIfMissing(valueMap.get(REQUIRED)));
		widget.setModel(modelOf(valueMap).build());
		widget.setData(dataOf(valueMap).build());
		widget.setFunctionData(functionDataOf(valueMap).build());
		widget.setLayout(String.class.cast(valueMap.get(LAYOUT)));
		widget.setCapabilities(capabilitiesOf(valueMap));
		widget.setSerialization(serializationOf(valueMap));
		widget.setVariables(extractUnmanagedParameters(valueMap, KNOWN_PARAMETERS));
		return widget;
	}

	private ModelBuilder modelOf(final Map<String, Object> valueMap) {
		final ModelBuilder output;
		final String value = String.class.cast(valueMap.get(MODEL_TYPE));
		if (TYPE_FORM.equalsIgnoreCase(value)) {
			final String expression = defaultString(String.class.cast(valueMap.get(FORM_MODEL)));
			Validate.isTrue(isNotBlank(expression), "invalid value for '%s'", FORM_MODEL);
			output = new FallbackOnExceptionModelBuilder(new JsonStringModelBuilder(expression),
					new IdentityModelBuilder(expression));
		} else if (TYPE_CLASS.equalsIgnoreCase(value)) {
			final String className = String.class.cast(valueMap.get(CLASS_MODEL));
			Validate.isTrue(isNotBlank(className), "invalid value for '%s'", CLASS_MODEL);
			final Iterable<String> attributes = on(DEFAULT_ATTRIBUTES_SEPARATOR) //
					.trimResults() //
					.omitEmptyStrings() //
					.split(defaultString(String.class.cast(valueMap.get(CLASS_ATTRIBUTES))));
			output = new ClassModelBuilder(dataView, metadataStoreFactory, className, attributes);
		} else if (TYPE_FUNCTION.equalsIgnoreCase(value)) {
			final String functionName = String.class.cast(valueMap.get(FUNCTION_MODEL));
			Validate.isTrue(isNotBlank(functionName), "invalid value for '%s'", FUNCTION_MODEL);
			final Iterable<String> attributes = on(DEFAULT_ATTRIBUTES_SEPARATOR) //
					.trimResults() //
					.omitEmptyStrings() //
					.split(defaultString(String.class.cast(valueMap.get(FUNCTION_ATTRIBUTES))));
			output = new FunctionModelBuilder(dataView, functionName, attributes);
		} else {
			output = new InvalidModelBuilder(format("'%s' is not a valid value for '%s'", value, MODEL_TYPE));
		}
		return output;
	}

	private DataBuilder dataOf(final Map<String, Object> valueMap) {
		final DataBuilder output;
		final String value = String.class.cast(valueMap.get(DATA_TYPE));
		if (TYPE_RAW.equalsIgnoreCase(value) || TYPE_RAW_JSON.equalsIgnoreCase(value)) {
			final String expression = defaultString(String.class.cast(valueMap.get(RAW_DATA)));
			output = new IdentityDataBuilder(expression);
		} else if (TYPE_RAW_TEXT.equalsIgnoreCase(value)) {
			final String expression = defaultString(String.class.cast(valueMap.get(RAW_DATA)));
			output = new TextDataBuilder(expression, textConfigurationOf(valueMap));
		} else if (TYPE_FUNCTION.equalsIgnoreCase(value) && !readBoolean(valueMap.get(TEMPLATE_RESOLVER), true)) {
			final String functionName = defaultString(String.class.cast(valueMap.get(FUNCTION_DATA)));
			Validate.isTrue(isNotBlank(functionName), "invalid value for '%s'", FUNCTION_DATA);
			output = new FunctionDataBuilder(dataView, functionName, valueMap);
		} else {
			output = new IdentityDataBuilder(null);
		}
		return output;
	}

	private Builder<String> functionDataOf(final Map<String, Object> valueMap) {
		final DataBuilder output;
		final String value = String.class.cast(valueMap.get(DATA_TYPE));
		if (TYPE_FUNCTION.equalsIgnoreCase(value) && readBoolean(valueMap.get(TEMPLATE_RESOLVER), true)) {
			final String functionName = defaultString(String.class.cast(valueMap.get(FUNCTION_DATA)));
			Validate.isTrue(isNotBlank(functionName), "invalid value for '%s'", FUNCTION_DATA);
			output = new IdentityDataBuilder(functionName);
		} else {
			output = new IdentityDataBuilder(null);
		}
		return output;
	}

	private Capabilities capabilitiesOf(final Map<String, Object> valueMap) {
		final Capabilities output = new Capabilities();
		output.setReadOnly(readBooleanFalseIfMissing(valueMap.get(READ_ONLY)));
		output.setAddDisabled(readBooleanFalseIfMissing(valueMap.get(ADD_DISABLED)));
		output.setDeleteDisabled(readBooleanFalseIfMissing(valueMap.get(DELETE_DISABLED)));
		output.setImportDisabled(readBooleanFalseIfMissing(valueMap.get(IMPORT_DISABLED)));
		output.setModifyDisabled(readBooleanFalseIfMissing(valueMap.get(MODIFY_DISABLED)));
		output.setCloneDisabled(readBooleanFalseIfMissing(valueMap.get(CLONE_DISABLED)));
		return output;
	}

	private Serialization serializationOf(final Map<String, Object> valueMap) {
		final Serialization output = new Serialization();
		final String type = defaultIfBlank(String.class.cast(valueMap.get(SERIALIZATION_TYPE)), TEXT_SERIALIZATION);
		final Configuration configuration;
		if (JSON_SERIALIZATION.equals(type)) {
			configuration = null;
		} else if (TEXT_SERIALIZATION.equals(type)) {
			final TextConfiguration textConfiguration = textConfigurationOf(valueMap);
			configuration = textConfiguration;
		} else {
			configuration = null;
		}
		output.setType(type);
		output.setConfiguration(configuration);
		return output;
	}

	private TextConfiguration textConfigurationOf(final Map<String, Object> valueMap) {
		final TextConfiguration textConfiguration = new TextConfiguration();
		textConfiguration.setKeyValueSeparator(defaultIfBlank(String.class.cast(valueMap.get(KEY_VALUE_SEPARATOR)),
				DEFAULT_KEY_VALUE_SEPARATOR));
		textConfiguration.setAttributesSeparator(defaultIfBlank(String.class.cast(valueMap.get(ATTRIBUTES_SEPARATOR)),
				DEFAULT_ATTRIBUTES_SEPARATOR));
		textConfiguration.setRowsSeparator(defaultIfBlank(String.class.cast(valueMap.get(ROWS_SEPARATOR)),
				DEFAULT_ROWS_SEPARATOR));
		return textConfiguration;
	}

}
