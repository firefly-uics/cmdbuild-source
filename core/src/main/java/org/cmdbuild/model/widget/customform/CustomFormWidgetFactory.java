package org.cmdbuild.model.widget.customform;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.Collections.emptyList;
import static org.apache.commons.lang3.StringUtils.defaultString;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.cmdbuild.exception.CMDBWorkflowException.WorkflowExceptionType.WF_CANNOT_CONFIGURE_CMDBEXTATTR;

import java.util.Collection;
import java.util.Map;

import org.cmdbuild.exception.CMDBWorkflowException;
import org.cmdbuild.model.widget.Widget;
import org.cmdbuild.model.widget.customform.CustomForm.Attribute;
import org.cmdbuild.notification.Notifier;
import org.cmdbuild.services.template.store.TemplateRepository;
import org.cmdbuild.workflow.widget.ValuePairWidgetFactory;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

public class CustomFormWidgetFactory extends ValuePairWidgetFactory {

	private static interface AttributesFetcher {

		Iterable<Attribute> attributes();

	}

	private static class NullAttributeFetcher implements AttributesFetcher {

		private static Iterable<Attribute> empty = emptyList();

		@Override
		public Iterable<Attribute> attributes() {
			return empty;
		}

	}

	private static class RawAttributeFetcher implements AttributesFetcher {

		private static final ObjectMapper mapper = new ObjectMapper();

		private static final TypeReference<Collection<? extends Attribute>> TYPE_REFERENCE = new TypeReference<Collection<? extends Attribute>>() {
		};

		private final String expression;

		public RawAttributeFetcher(final String expression) {
			this.expression = expression;
		}

		@Override
		public Iterable<Attribute> attributes() {
			try {
				logger.debug(MARKER, "parsing expression '{}'", expression);
				return mapper.readValue(expression, TYPE_REFERENCE);
			} catch (final Exception e) {
				logger.error(MARKER, "error parsing expression", e);
				throw new RuntimeException(e);
			}
		}

	}

	private static final Marker MARKER = MarkerFactory.getMarker(CustomFormWidgetFactory.class.getName());

	private static final String WIDGET_NAME = "customForm";

	public static final String REQUIRED = "Required";
	public static final String CONFIGURATION_TYPE = "ConfigurationType";
	public static final String RAW_ATTRIBUTES = "RawAttributes";

	private static final String[] KNOWN_PARAMETERS = { REQUIRED, CONFIGURATION_TYPE, RAW_ATTRIBUTES };

	private final Notifier notifier;

	public CustomFormWidgetFactory(final TemplateRepository templateRespository, final Notifier notifier) {
		super(templateRespository, notifier);
		this.notifier = notifier;
	}

	@Override
	public String getWidgetName() {
		return WIDGET_NAME;
	}

	@Override
	protected Widget createWidget(final Map<String, Object> valueMap) {
		final CustomForm widget = new CustomForm();
		widget.setRequired(readBooleanFalseIfMissing(valueMap.get(REQUIRED)));
		widget.setAttributes(newArrayList(attributesFetcherOf(valueMap).attributes()));
		widget.setVariables(extractUnmanagedParameters(valueMap, KNOWN_PARAMETERS));
		return widget;
	}

	private AttributesFetcher attributesFetcherOf(final Map<String, Object> valueMap) {
		final AttributesFetcher attributeFetcher;
		final String configurationType = defaultString(String.class.cast(valueMap.get(CONFIGURATION_TYPE)));
		if ("raw".equalsIgnoreCase(configurationType)) {
			final String expression = defaultString(String.class.cast(valueMap.get(RAW_ATTRIBUTES)));
			if (isBlank(expression)) {
				logger.warn(MARKER, "invalid value '{}' for '{}'", expression, RAW_ATTRIBUTES);
				notifier.warn(new CMDBWorkflowException(WF_CANNOT_CONFIGURE_CMDBEXTATTR, getWidgetName()));
				attributeFetcher = new NullAttributeFetcher();
			} else {
				attributeFetcher = new RawAttributeFetcher(expression);
			}
		} else {
			logger.warn(MARKER, "undefined type '{}'", configurationType);
			notifier.warn(new CMDBWorkflowException(WF_CANNOT_CONFIGURE_CMDBEXTATTR, getWidgetName()));
			attributeFetcher = new NullAttributeFetcher();
		}
		return attributeFetcher;
	}

}
