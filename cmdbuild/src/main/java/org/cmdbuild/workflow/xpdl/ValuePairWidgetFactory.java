package org.cmdbuild.workflow.xpdl;

import java.util.HashMap;
import java.util.Map;

import org.cmdbuild.workflow.CMActivity.CMActivityWidget;

/**
 * Single activity widget factory that knows how to decode a list of
 * key/value pairs.
 */
public abstract class ValuePairWidgetFactory implements SingleActivityWidgetFactory {

	public final CMActivityWidget createWidget(final String serialization) {
		return createWidgetWithStandardAttributes(deserialize(serialization));
	}

	private Map<String,String> deserialize(final String serialization) {
		final Map<String,String> valuePairs = new HashMap<String,String>();
		// TODO
		return valuePairs;
	}

	private CMActivityWidget createWidgetWithStandardAttributes(final Map<String, String> valuePairs) {
		final CMActivityWidget w = createWidget(valuePairs);
		// TODO add standard attributes (label, ...)
		return w;
	}

	protected abstract CMActivityWidget createWidget(final Map<String, String> valuePairs);

	
}
