package org.cmdbuild.dao.view.user;

import org.cmdbuild.dao.entrytype.CMAttribute;
import org.cmdbuild.dao.entrytype.CMEntryType;
import org.cmdbuild.dao.entrytype.CMEntryTypeVisitor;
import org.cmdbuild.dao.entrytype.CMIdentifier;

public abstract class UserEntryType implements CMEntryType {

	protected final UserDataView view;

	/*
	 * Should be used by the subclasses only
	 */
	protected UserEntryType(final UserDataView view) {
		this.view = view;
	}

	protected abstract CMEntryType inner();

	@Override
	public boolean isActive() {
		return inner().isActive();
	}

	@Override
	public CMIdentifier getIdentifier() {
		return inner().getIdentifier();
	}

	@Override
	public Long getId() {
		return inner().getId();
	}

	@Override
	public String getName() {
		return getIdentifier().getLocalName();
	}

	@Override
	public String getDescription() {
		return inner().getDescription();
	}

	@Override
	public boolean isSystem() {
		return inner().isSystem();
	}

	@Override
	public boolean isSystemButUsable() {
		return inner().isSystemButUsable();
	}

	@Override
	public boolean isBaseClass() {
		return inner().isBaseClass();
	}

	@Override
	public Iterable<UserAttribute> getActiveAttributes() {
		return view.proxyAttributes(inner().getActiveAttributes());
	}

	@Override
	public Iterable<UserAttribute> getAttributes() {
		return view.proxyAttributes(inner().getAttributes());
	}

	@Override
	public Iterable<? extends CMAttribute> getAllAttributes() {
		return view.proxyAttributes(inner().getAllAttributes());
	}

	@Override
	public UserAttribute getAttribute(final String name) {
		return UserAttribute.newInstance(view, inner().getAttribute(name));
	}

	@Override
	public String getKeyAttributeName() {
		return inner().getKeyAttributeName();
	}

	@Override
	public final void accept(final CMEntryTypeVisitor visitor) {
		inner().accept(visitor);
	}

	/*
	 * Object overrides
	 */

	@Override
	public int hashCode() {
		return inner().hashCode();
	}

	@Override
	public boolean equals(final Object obj) {
		return inner().equals(obj);
	}

	@Override
	public String toString() {
		// TODO Add username
		return inner().toString();
	}

	@Override
	public final String getPrivilegeId() {
		return inner().getPrivilegeId();
	}
}
