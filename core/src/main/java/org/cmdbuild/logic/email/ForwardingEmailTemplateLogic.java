package org.cmdbuild.logic.email;

public abstract class ForwardingEmailTemplateLogic implements EmailTemplateLogic {

	private final EmailTemplateLogic delegate;

	protected ForwardingEmailTemplateLogic(final EmailTemplateLogic delegate) {
		this.delegate = delegate;
	}

	@Override
	public Iterable<Template> readAll() {
		return delegate.readAll();
	}

	@Override
	public Template read(final String name) {
		return delegate.read(name);
	}

	@Override
	public Long create(final Template template) {
		return delegate.create(template);
	}

	@Override
	public void update(final Template template) {
		delegate.update(template);
	}

	@Override
	public void delete(final String name) {
		delegate.delete(name);
	}

}
