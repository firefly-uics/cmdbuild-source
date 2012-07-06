package org.cmdbuild.workflow.widget;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.cmdbuild.cql.compiler.impl.QueryImpl;
import org.cmdbuild.model.widget.Widget;
import org.cmdbuild.utils.CQLFacadeCompiler;
import org.cmdbuild.workflow.CMActivity.CMActivityWidget;
import org.cmdbuild.workflow.xpdl.SingleActivityWidgetFactory;

/**
 * Single activity widget factory that knows how to decode a list of
 * key/value pairs.
 */
public abstract class ValuePairWidgetFactory implements SingleActivityWidgetFactory {

	public static final String BUTTON_LABEL = "ButtonLabel";

	private static final String LINE_SEPARATOR = "\r?\n";
	private static final String VALUE_SEPARATOR = "=";

	private static final String SINGLE_QUOTES = "'";
	private static final String DOUBLE_QUOTES = "\"";
	private static final String CLIENT_PREFIX = "client:";

	public final CMActivityWidget createWidget(final String serialization) {
		final Map<String,String> valueMap = deserialize(serialization);
		final Widget widget = createWidget(valueMap);
		setWidgetId(widget, serialization);
		setWidgetLabel(widget, valueMap);
		return widget;
	}

	private void setWidgetId(Widget widget, String serialization) {
		final String id = String.format("widget-%x", serialization.hashCode());
		widget.setId(id);
	}

	private void setWidgetLabel(Widget widget, Map<String, String> valueMap) {
		final String label = valueMap.get(BUTTON_LABEL);
		if (label != null) {
			widget.setLabel(label);
		}
	}

	private Map<String,String> deserialize(final String serialization) {
		final Map<String,String> valueMap = new HashMap<String,String>();
		for (final String line : serialization.split(LINE_SEPARATOR)) {
			addPair(valueMap, line);
		}
		return valueMap;
	}

	private void addPair(final Map<String, String> valueMap, final String line) {
		final String pair[] = line.split(VALUE_SEPARATOR, 2);
		if (pair.length > 0 && !pair[0].isEmpty()) {
			if (pair.length == 1 || pair[1].isEmpty()) {
				valueMap.put(pair[0], null);
			} else {
				try {
					valueMap.put(pair[0], interpretValue(pair[1]));
				} catch (Exception e) {
					// Ignore malformed lines
				}
			}
		}
	}

	/*
	 * TODO switch to a strategy when
	 */
	private String interpretValue(final String value) {
		if (Character.isDigit(value.charAt(0))) {
			return value;
		} else if ((value.startsWith(DOUBLE_QUOTES) && value.endsWith(DOUBLE_QUOTES))
				|| (value.startsWith(SINGLE_QUOTES) && value.endsWith(SINGLE_QUOTES))) {
			return value.substring(1, value.length()-1);
		} else if (value.startsWith(CLIENT_PREFIX)) {
			return String.format("{%s}", value);
		} else {
			throw new UnsupportedOperationException("Grab a variable!");
		}
	}

	private CMActivityWidget createWidgetAndAddStandardAttributes(final Map<String, String> valueMap) {
		final Widget widget = createWidget(valueMap);
		final String label = valueMap.get(BUTTON_LABEL);
		if (label != null) {
			widget.setLabel(label);
		}
		return widget;
	}

	protected abstract Widget createWidget(Map<String, String> valueMap);

	protected final boolean readBooleanTrueIfPresent(String value) {
		return (value != null);
	}

	protected final boolean readBooleanTrueIfTrue(String value) {
		return Boolean.parseBoolean(value);
	}

	protected final Integer readInteger(String value) {
		Integer out = null;
		try {
			out = Integer.parseInt(value);
		} catch (NumberFormatException e) {
			// ignore it
		}

		return out;
	}

	protected final String readClassNameFromCQLFilter(String filter) {
		String className = null;
		try {
			QueryImpl q = CQLFacadeCompiler.compileWithTemplateParams(filter);
			className = q.getFrom().mainClass().getClassName();
		} catch (Exception e) {
			// ignore it
		}

		return className;
	}

	protected final List<String> extractParametersWithNullAsValue(Map<String,String> valueMap) {
		final ArrayList<String> out = new ArrayList<String>();
		for(String key:valueMap.keySet()) {
			if (valueMap.get(key) == null) {
				out.add(key);
			}
		}

		return out;
	}

	protected final Map<String, String> extractUnmanagedParameters(Map<String, String> valueMap, Set<String> managedParameters) {
		Map<String, String> out = new HashMap<String, String>();

		for (String key: valueMap.keySet()) {
			if (!managedParameters.contains(key)) {
				out.put(key, valueMap.get(key));
			}
		}

		return out;
	}

	protected final Map<String, String> extractUnmanagedParameters(Map<String, String> valueMap, String... managedParameters) {
		Set<String> parameters = new HashSet<String>();
		for (String s: managedParameters) {
			parameters.add(s);
		}
		return extractUnmanagedParameters(valueMap, parameters);
	}
}
