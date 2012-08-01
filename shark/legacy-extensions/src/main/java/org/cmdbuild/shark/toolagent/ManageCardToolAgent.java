package org.cmdbuild.shark.toolagent;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class ManageCardToolAgent extends AbstractConditionalToolAgent {

	protected static final String CLASS_NAME = "ClassName";
	protected static final String OBJ_ID = "ObjId";

	protected Map<String, Object> getAttributeMap() {
		final Map<String, Object> attributes = new HashMap<String, Object>();
		if (isMeta()) {
			for (final Map.Entry<String, Object> entry : getInputParameterValues().entrySet()) {
				final String name = entry.getKey();
				if (isNotMetaAttribute(name)) {
					continue;
				}
				attributes.put(name, entry.getValue());
			}
		} else {
			attributes.putAll(getNonMetaAttributes());
		}
		return attributes;
	}

	protected String getClassName() {
		String className = getExtendedAttribute(CLASS_NAME);
		if (className == null) {
			className = getParameterValue(CLASS_NAME);
		}
		return className;
	}

	private boolean isMeta() {
		return !notMetaToolNames().contains(getId());
	}

	protected abstract List<String> notMetaToolNames();

	private boolean isNotMetaAttribute(final String name) {
		return notMetaAttributeNames().contains(name);
	}

	protected abstract List<String> notMetaAttributeNames();

	protected abstract Map<String, Object> getNonMetaAttributes();

}