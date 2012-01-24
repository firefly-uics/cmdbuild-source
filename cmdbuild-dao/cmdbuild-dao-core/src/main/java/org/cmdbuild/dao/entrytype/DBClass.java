package org.cmdbuild.dao.entrytype;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class DBClass extends DBEntryType implements CMClass {

	public static class ClassMetadata extends EntryTypeMetadata {
		public static final String SUPERCLASS = BASE_NS + "superclass";

		final boolean isSuperclass() {
			return Boolean.parseBoolean(get(SUPERCLASS));
		}

		final void setSuperclass(final boolean superclass) {
			put(SUPERCLASS, Boolean.toString(superclass));
		}
	}

	private final ClassMetadata meta;
	private DBClass parent;
	private Set<DBClass> children;

	public DBClass(final String name, final Object id, final ClassMetadata meta, final List<DBAttribute> attributes) {
		super(name, id, attributes);
		this.meta = meta;
		children = new HashSet<DBClass>();
	}

	@Deprecated
	public DBClass(final String name, final Object id, final List<DBAttribute> attributes) {
		this(name, id, new ClassMetadata(), attributes);
	}

    public void accept(CMEntryTypeVisitor visitor) {
        visitor.visit(this);
    }

    public void accept(DBEntryTypeVisitor visitor) {
        visitor.visit(this);
    }

	protected final ClassMetadata meta() {
		return meta;
	}

	public String toString() {
		return String.format("[Class %s]", getName());
	}

	public final String getPrivilegeId() {
		return String.format("Class:%d", getId());
	}

	public void setParent(final DBClass newParent) {
		if (parent != newParent) {
			if (parent != null) {
				parent.children.remove(this);
			}
			if (newParent != null) {
				newParent.children.add(this);
			}
			parent = newParent;
		}
	}

	@Override
	public DBClass getParent() {
		return parent;
	}

	@Override
	public Iterable<DBClass> getChildren() {
		return children;
	}

	@Override
	public Iterable<DBClass> getLeaves() {
		Set<DBClass> leaves = new HashSet<DBClass>();
		addLeaves(leaves, this);
		return leaves;
	}

	private void addLeaves(final Set<DBClass> leaves, final DBClass currentClass) {
		if (currentClass.isSuperclass()) {
			for (DBClass subclass : currentClass.getChildren()) {
				addLeaves(leaves, subclass);
			}
		} else {
			leaves.add(currentClass);
		}
	}

	public boolean isAncestorOf(final CMClass cmClass) {
		for (CMClass parent = cmClass; parent != null; parent = parent.getParent()) {
			if (parent.equals(this)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean isSuperclass() {
		return meta().isSuperclass(); 
	}

	@Override
	public String getCodeAttributeName() {
		// TODO Mark it in the metadata!
		return "Code";
	}

	@Override
	public String getDescriptionAttributeName() {
		// TODO Mark it in the metadata!
		return "Description";
	}
}
