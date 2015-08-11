package org.cmdbuild.model.widget.customform;

import static java.lang.String.format;
import static org.apache.commons.lang3.BooleanUtils.toBoolean;
import static org.apache.commons.lang3.StringUtils.defaultString;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

import java.util.Collection;
import java.util.Map;

import org.apache.commons.lang3.Validate;
import org.cmdbuild.dao.view.CMDataView;
import org.cmdbuild.model.widget.Widget;
import org.cmdbuild.notification.Notifier;
import org.cmdbuild.services.meta.MetadataStoreFactory;
import org.cmdbuild.services.template.store.TemplateRepository;
import org.cmdbuild.workflow.widget.ValuePairWidgetFactory;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

public class CustomFormWidgetFactory extends ValuePairWidgetFactory {

	static final Marker MARKER = MarkerFactory.getMarker(CustomFormWidgetFactory.class.getName());

	private static final String WIDGET_NAME = "customForm";

	public static final String //
			REQUIRED = "Required", //
			READ_ONLY = "ReadOnly", //
			CONFIGURATION_TYPE = "ConfigurationType", //
			FORM = "Form", //
			CLASSNAME = "ClassName", //
			FUNCTIONNAME = "FunctionName", //
			LAYOUT = "Layout", //
			DISABLE_ADD_ROW = "AddRowDisabled", //
			DISABLE_IMPORT_FROM_CSV = "ImportCsvDisabled", //
			DISABLE_DELETE_ROW = "DeleteRowDisabled";

	private static final String[] KNOWN_PARAMETERS = { BUTTON_LABEL, REQUIRED, READ_ONLY, //
			CONFIGURATION_TYPE, //
			FORM, //
			CLASSNAME, //
			LAYOUT, //
			DISABLE_ADD_ROW, DISABLE_IMPORT_FROM_CSV, DISABLE_DELETE_ROW //
	};

	private static final String //
			TYPE_FORM = "form", //
			TYPE_CLASS = "class", //
			TYPE_FUNCTION = "function";

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
		widget.setAddRowDisabled(toBoolean(String.class.cast(valueMap.get(DISABLE_ADD_ROW))));
		widget.setDeleteRowDisabled(toBoolean(String.class.cast(valueMap.get(DISABLE_DELETE_ROW))));
		widget.setImportCsvDisabled(toBoolean(String.class.cast(valueMap.get(DISABLE_IMPORT_FROM_CSV))));
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

	static String writeJsonString(final Collection<Attribute> attributes) {
		try {
			final ObjectMapper mapper = new ObjectMapper();
			return mapper.writeValueAsString(attributes);
		} catch (final Exception e) {
			throw new RuntimeException(e);
		}
	}

}
