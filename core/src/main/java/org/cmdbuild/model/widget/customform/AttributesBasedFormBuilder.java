package org.cmdbuild.model.widget.customform;

import static com.google.common.collect.Lists.newArrayList;

abstract class AttributesBasedFormBuilder implements FormBuilder {

	protected static final String //
			TYPE_BOOLEAN = "boolean", //
			TYPE_CHAR = "char", //
			TYPE_DATE = "date", //
			TYPE_DATE_TIME = "dateTime", //
			TYPE_DECIMAL = "decimal", //
			TYPE_DOUBLE = "double", //
			TYPE_ENTRY_TYPE = "entryType", //
			TYPE_INTEGER = "integer", //
			TYPE_IP_ADDRESS = "ipAddress", //
			TYPE_LOOKUP = "lookup", //
			TYPE_REFERENCE = "reference", //
			TYPE_STRING_ARRAY = "stringArray", //
			TYPE_STRING = "string", //
			TYPE_TEXT = "text", //
			TYPE_TIME = "time";

	/**
	 * Usable by subclasses only.
	 */
	protected AttributesBasedFormBuilder() {
	}

	@Override
	public final String build() {
		return CustomFormWidgetFactory.writeJsonString(newArrayList(attributes()));
	}

	protected abstract Iterable<Attribute> attributes();

}