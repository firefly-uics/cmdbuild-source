package org.cmdbuild.services.store.filter;

import org.cmdbuild.services.localization.LocalizableStorableVisitor;
import org.cmdbuild.services.store.filter.FilterStore.Filter;

import com.google.common.collect.ForwardingObject;

public abstract class ForwardingFilter extends ForwardingObject implements Filter {

	/**
	 * Usable by subclasses only.
	 */
	protected ForwardingFilter() {
	}

	@Override
	protected abstract Filter delegate();

	@Override
	public String getPrivilegeId() {
		return delegate().getPrivilegeId();
	}

	@Override
	public void accept(final LocalizableStorableVisitor visitor) {
		delegate().accept(visitor);
	}

	@Override
	public String getIdentifier() {
		return delegate().getIdentifier();
	}

	@Override
	public Long getId() {
		return delegate().getId();
	}

	@Override
	public String getName() {
		return delegate().getName();
	}

	@Override
	public String getDescription() {
		return delegate().getDescription();
	}

	@Override
	public String getClassName() {
		return delegate().getClassName();
	}

	@Override
	public String getValue() {
		return delegate().getValue();
	}

	@Override
	public boolean isTemplate() {
		return delegate().isTemplate();
	}

	@Override
	public Long getOwner() {
		return delegate().getOwner();
	}

}
