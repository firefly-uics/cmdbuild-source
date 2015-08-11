package org.cmdbuild.model.widget.customform;

class IdentityFormBuilder implements FormBuilder {

	private final String value;

	public IdentityFormBuilder(final String value) {
		this.value = value;
	}

	@Override
	public String build() {
		return value;
	}

}