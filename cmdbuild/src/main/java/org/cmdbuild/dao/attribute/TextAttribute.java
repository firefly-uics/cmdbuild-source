package org.cmdbuild.dao.attribute;

import java.util.Map;

import org.cmdbuild.elements.interfaces.BaseSchema;

public class TextAttribute extends AbstractTextAttribute {

	private enum ALLOWED_TYPES {
		PLAIN,
		HTML;
	}

	public TextAttribute(BaseSchema schema, String name, Map<String, String> meta) {
		super(schema, name, meta);
	}

	@Override
	public final AttributeType getType() {
		return AttributeType.TEXT;
	}

	@Override
	public String getEditorType() {
		final String editorType = super.getEditorType();
		if (editorType == null) {
			return ALLOWED_TYPES.PLAIN.toString();
		} else {
			return editorType;
		}
	}

	@Override
	public void setEditorType(final String editorType) {
		if (isTypeAllowed(editorType)) {
			super.setEditorType(editorType);
		} else {
			throw new IllegalArgumentException();
		}
	}

	private boolean isTypeAllowed(String editorType) {
		ALLOWED_TYPES type = ALLOWED_TYPES.valueOf(editorType);
		return type != null;
	}
}
