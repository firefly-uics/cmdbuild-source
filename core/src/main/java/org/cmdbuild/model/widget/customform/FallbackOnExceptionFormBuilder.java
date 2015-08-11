package org.cmdbuild.model.widget.customform;

class FallbackOnExceptionFormBuilder implements FormBuilder {

	private final FormBuilder delegate;
	private final FormBuilder fallback;

	public FallbackOnExceptionFormBuilder(final FormBuilder delegate, final FormBuilder fallback) {
		this.delegate = delegate;
		this.fallback = fallback;
	}

	@Override
	public String build() {
		try {
			return delegate.build();
		} catch (final Exception e) {
			return fallback.build();
		}
	}

}