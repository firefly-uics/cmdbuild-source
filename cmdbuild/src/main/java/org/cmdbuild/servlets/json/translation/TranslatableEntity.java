package org.cmdbuild.servlets.json.translation;


public enum TranslatableEntity {
	
	CLASS("class"),
	DOMAIN("domain"),
	PROCESS("process"),
	LOOKUP("lookup"),
	UNDEFINED(null);
	

	private final String type;

	private TranslatableEntity(final String type) {
		this.type = type;
	};
	
	private static TranslatableEntity of(final String type) {
		for (final TranslatableEntity element : values()) {
			if (element.type.equalsIgnoreCase(type)) {
				return element;
			}
		}
		return UNDEFINED;
	}
}
