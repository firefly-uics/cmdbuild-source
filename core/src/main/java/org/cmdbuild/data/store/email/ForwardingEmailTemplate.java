package org.cmdbuild.data.store.email;

import java.util.List;

public abstract class ForwardingEmailTemplate implements EmailTemplate {

	private final EmailTemplate delegate;

	protected ForwardingEmailTemplate(final EmailTemplate delegate) {
		this.delegate = delegate;
	}

	@Override
	public String getIdentifier() {
		return delegate.getIdentifier();
	}

	@Override
	public Long getId() {
		return delegate.getId();
	}

	@Override
	public String getName() {
		return delegate.getName();
	}

	@Override
	public String getDescription() {
		return delegate.getDescription();
	}

	@Override
	public String getFrom() {
		return delegate.getFrom();
	}

	@Override
	public String getTo() {
		return delegate.getTo();
	}

	@Override
	public List<String> getToAddresses() {
		return delegate.getToAddresses();
	}

	@Override
	public String getCc() {
		return delegate.getCc();
	}

	@Override
	public List<String> getCCAddresses() {
		return delegate.getCCAddresses();
	}

	@Override
	public String getBcc() {
		return delegate.getBcc();
	}

	@Override
	public List<String> getBCCAddresses() {
		return delegate.getBCCAddresses();
	}

	@Override
	public String getSubject() {
		return delegate.getSubject();
	}

	@Override
	public String getBody() {
		return delegate.getBody();
	}

	@Override
	public Long getAccount() {
		return delegate.getAccount();
	}

}
