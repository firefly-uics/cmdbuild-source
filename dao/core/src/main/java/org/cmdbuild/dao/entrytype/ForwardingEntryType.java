package org.cmdbuild.dao.entrytype;

public class ForwardingEntryType implements CMEntryType {

	private final CMEntryType inner;

	public ForwardingEntryType(final CMEntryType inner) {
		this.inner = inner;
	}

	@Override
	public boolean isActive() {
		return inner.isActive();
	}

	@Override
	public String getPrivilegeId() {
		return inner.getPrivilegeId();
	}

	@Override
	public void accept(final CMEntryTypeVisitor visitor) {
		inner.accept(visitor);
	}

	@Override
	public Long getId() {
		return inner.getId();
	}

	@Override
	public String getName() {
		return inner.getName();
	}

	@Override
	public CMIdentifier getIdentifier() {
		return inner.getIdentifier();
	}

	@Override
	public String getDescription() {
		return inner.getDescription();
	}

	@Override
	public boolean isSystem() {
		return inner.isSystem();
	}

	@Override
	public boolean isSystemButUsable() {
		return inner.isSystemButUsable();
	}

	@Override
	public boolean isBaseClass() {
		return inner.isBaseClass();
	}

	@Override
	public boolean holdsHistory() {
		return inner.holdsHistory();
	}

	@Override
	public Iterable<? extends CMAttribute> getActiveAttributes() {
		return inner.getActiveAttributes();
	}

	@Override
	public Iterable<? extends CMAttribute> getAttributes() {
		return inner.getAttributes();
	}

	@Override
	public Iterable<? extends CMAttribute> getAllAttributes() {
		return inner.getAllAttributes();
	}

	@Override
	public CMAttribute getAttribute(final String name) {
		return inner.getAttribute(name);
	}

	@Override
	public String getKeyAttributeName() {
		return inner.getKeyAttributeName();
	}

	@Override
	public int hashCode() {
		return inner.hashCode();
	}

	@Override
	public boolean equals(final Object obj) {
		return inner.equals(obj);
	}

	@Override
	public String toString() {
		return inner.toString();
	}

}
