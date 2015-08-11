package org.cmdbuild.model.widget.customform;

class InvalidFormBuilder implements FormBuilder {

	private final String message;

	public InvalidFormBuilder(final String message) {
		this.message = message;
	}

	@Override
	public String build() {
		throw new RuntimeException(message);
	}

}