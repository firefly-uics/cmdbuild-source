package org.cmdbuild.dao.entrytype;

public class ForwardingClass extends ForwardingEntryType implements CMClass {

	private final CMClass inner;

	public ForwardingClass(final CMClass inner) {
		super(inner);
		this.inner = inner;
	}

	@Override
	public CMClass getParent() {
		return inner.getParent();
	}

	@Override
	public Iterable<? extends CMClass> getChildren() {
		return inner.getChildren();
	}

	@Override
	public Iterable<? extends CMClass> getLeaves() {
		return inner.getLeaves();
	}

	@Override
	public Iterable<? extends CMClass> getDescendants() {
		return inner.getDescendants();
	}

	@Override
	public boolean isAncestorOf(final CMClass cmClass) {
		return inner.isAncestorOf(cmClass);
	}

	@Override
	public boolean isSuperclass() {
		return inner.isSuperclass();
	}

	@Override
	public String getCodeAttributeName() {
		return inner.getCodeAttributeName();
	}

	@Override
	public String getDescriptionAttributeName() {
		return inner.getDescriptionAttributeName();
	}

	@Override
	public boolean isUserStoppable() {
		return inner.isUserStoppable();
	}

	@Override
	public boolean isSimple() {
		return inner.isSimple();
	}
}
