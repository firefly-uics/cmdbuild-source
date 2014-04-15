package org.cmdbuild.common.template;

public abstract class ForwardingTemplateResolverEngine implements TemplateResolverEngine {

	private final TemplateResolverEngine delegate;

	protected ForwardingTemplateResolverEngine(final TemplateResolverEngine delegate) {
		this.delegate = delegate;
	}

	@Override
	public Object eval(final String expression) {
		return delegate.eval(expression);
	}

}
