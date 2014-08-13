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
	public Template read(String name) {
		return delegate.read(name);
	}

	@Override
	public Long create(Template template) {
		return delegate.create(template);
	}

	@Override
	public void update(Template template) {
		delegate.update(template);
	}

	@Override
	public void delete(String name) {
		delegate.delete(name);
	}

}
