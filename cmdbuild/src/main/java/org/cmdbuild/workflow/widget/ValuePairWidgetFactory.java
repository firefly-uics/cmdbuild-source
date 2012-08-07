package org.cmdbuild.workflow.widget;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.Validate;
import org.cmdbuild.cql.compiler.impl.QueryImpl;
import org.cmdbuild.dao.entry.CMValueSet;
import org.cmdbuild.model.widget.Widget;
import org.cmdbuild.services.TemplateRepository;
import org.cmdbuild.utils.CQLFacadeCompiler;
import org.cmdbuild.workflow.CMActivity.CMActivityWidget;
import org.cmdbuild.workflow.xpdl.SingleActivityWidgetFactory;

/**
 * Single activity widget factory that knows how to decode a list of key/value
 * pairs.
 */
public abstract class ValuePairWidgetFactory implements SingleActivityWidgetFactory {

	public static final String BUTTON_LABEL = "ButtonLabel";

	/**
	 * Key in the value map that holds the output variable name.
	 */
	public static final String OUTPUT_KEY = null;

	private static final String LINE_SEPARATOR = "\r?\n";
	private static final String VALUE_SEPARATOR = "=";

	private static final String FILTER_KEY = "Filter";
	private static final String SINGLE_QUOTES = "'";
	private static final String DOUBLE_QUOTES = "\"";
	private static final String CLIENT_PREFIX = "client:";
	private static final String DB_TEMPLATE_PREFIX = "dbtmpl:";

	private final TemplateRepository templateRespository;

	protected ValuePairWidgetFactory(final TemplateRepository templateRespository) {
		Validate.notNull(templateRespository);
		this.templateRespository = templateRespository;
	}

	@Override
	public final CMActivityWidget createWidget(final String serialization, final CMValueSet processInstanceVariables) {
		final Map<String, Object> valueMap = deserialize(serialization, processInstanceVariables);
		final Widget widget = createWidget(valueMap);
		setWidgetId(widget, serialization);
		setWidgetLabel(widget, valueMap);
		return widget;
	}

	private void setWidgetId(Widget widget, String serialization) {
		final String id = String.format("widget-%x", serialization.hashCode());
		widget.setId(id);
	}

	private void setWidgetLabel(Widget widget, Map<String, Object> valueMap) {
		final String label = (String) valueMap.get(BUTTON_LABEL);
		if (label != null) {
			widget.setLabel(label);
		}
	}

	private Map<String, Object> deserialize(final String serialization, final CMValueSet processInstanceVariables) {
		final Map<String, Object> valueMap = new HashMap<String, Object>();
		for (final String line : serialization.split(LINE_SEPARATOR)) {
			addPair(valueMap, line, processInstanceVariables);
		}
		return valueMap;
	}

	private void addPair(final Map<String, Object> valueMap, final String line,
			final CMValueSet processInstanceVariables) {
		final String pair[] = line.split(VALUE_SEPARATOR, 2);
		if (pair.length > 0 && !pair[0].isEmpty()) {
			final String key = pair[0];
			if (pair.length == 1 || pair[1].isEmpty()) {
				valueMap.put(OUTPUT_KEY, key);
			} else {
				final Object value = interpretValue(key, pair[1], processInstanceVariables);
				valueMap.put(key, value);
			}
		}
	}

	private Object interpretValue(final String key, final String value, final CMValueSet processInstanceVariables) {
		if (FILTER_KEY.equals(key)) {
			return value;
		} else if (Character.isDigit(value.charAt(0))) {
			return readInteger(value);
		} else if (betweenQuotes(value)) {
			// Quoted values and the Filter (!) parameter are interpreted as strings
			return value.substring(1, value.length() - 1);
		} else if (value.startsWith(CLIENT_PREFIX)) {
			// "Client" variables are always interpreted by the
			// template resolver on the client side
			return String.format("{%s}", value);
		} else if (value.startsWith(DB_TEMPLATE_PREFIX)) {
			final String templateName = value.substring(DB_TEMPLATE_PREFIX.length());
			return templateRespository.getTemplate(templateName);
		} else {
			// Process variables are fetched from the process instance
			return processInstanceVariables.get(value);
		}
	}

	private boolean betweenQuotes(final String value) {
		return (value.startsWith(DOUBLE_QUOTES) && value.endsWith(DOUBLE_QUOTES))
				|| (value.startsWith(SINGLE_QUOTES) && value.endsWith(SINGLE_QUOTES));
	}

	protected abstract Widget createWidget(Map<String, Object> valueMap);

	protected final String readString(Object value) {
		if (value instanceof String) {
			return (String) value;
		} else if (value != null) {
			return value.toString();
		} else {
			return null;
		}
	}

	protected final boolean readBooleanTrueIfPresent(Object value) {
		return (value != null);
	}

	protected final boolean readBooleanTrueIfTrue(Object value) {
		if (value instanceof String) {
			return Boolean.parseBoolean((String) value);
		} else if (value instanceof Boolean) {
			return (Boolean) value;
		} else {
			return false;
		}
	}

	protected final Integer readInteger(Object value) {
		if (value instanceof String) {
			try {
				return Integer.parseInt((String) value);
			} catch (NumberFormatException e) {
				return null;
			}
		} else if (value instanceof Integer) {
			return (Integer) value;
		} else if (value instanceof Number) {
			return ((Number) value).intValue();
		} else {
			return null;
		}
	}

	protected final String readClassNameFromCQLFilter(Object filter) {
		if (filter instanceof String) {
			try {
				QueryImpl q = CQLFacadeCompiler.compileWithTemplateParams((String) filter);
				return q.getFrom().mainClass().getClassName();
			} catch (Exception e) {
				// return null later
			}
		}
		return null;
	}

	protected final Map<String, Object> extractUnmanagedParameters(Map<String, Object> valueMap,
			Set<String> managedParameters) {
		Map<String, Object> out = new HashMap<String, Object>();

		for (String key : valueMap.keySet()) {
			if (key == null || managedParameters.contains(key)) {
				continue;
			}
			out.put(key, valueMap.get(key));
		}

		return out;
	}

	protected final Map<String, Object> extractUnmanagedParameters(Map<String, Object> valueMap,
			String... managedParameters) {
		Set<String> parameters = new HashSet<String>();
		for (String s : managedParameters) {
			parameters.add(s);
		}
		return extractUnmanagedParameters(valueMap, parameters);
	}

	protected final Map<String, String> extractUnmanagedStringParameters(Map<String, Object> valueMap,
			Set<String> managedParameters) {
		final Map<String, Object> rawParameters = extractUnmanagedParameters(valueMap, managedParameters);
		final Map<String, String> stringParameters = new HashMap<String, String>();
		for (Map.Entry<String, Object> rawEntry : rawParameters.entrySet()) {
			stringParameters.put(rawEntry.getKey(), readString(rawEntry.getValue()));
		}
		return stringParameters;
	}

	protected final Map<String, String> extractUnmanagedStringParameters(Map<String, Object> valueMap,
			String... managedParameters) {
		Set<String> parameters = new HashSet<String>();
		for (String s : managedParameters) {
			parameters.add(s);
		}
		return extractUnmanagedStringParameters(valueMap, parameters);
	}
}
