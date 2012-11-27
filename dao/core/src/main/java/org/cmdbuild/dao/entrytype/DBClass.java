package org.cmdbuild.dao.entrytype;

import java.util.List;
import java.util.Set;

import org.apache.commons.lang.builder.ToStringBuilder;

import com.google.common.collect.Sets;

public class DBClass extends DBEntryType implements CMClass {

	public static class ClassMetadata extends EntryTypeMetadata {

		public static final String SUPERCLASS = BASE_NS + "superclass";

		public final boolean isSuperclass() {
			return Boolean.parseBoolean(get(SUPERCLASS));
		}

		public final void setSuperclass(final boolean superclass) {
			put(SUPERCLASS, Boolean.toString(superclass));
		}

		public final boolean holdsHistory() {
			return Boolean.parseBoolean(get(HOLD_HISTORY));
		}

		public final void setHoldsHistory(final boolean holdsHistory) {
			put(HOLD_HISTORY, Boolean.toString(holdsHistory));
		}

	}

	private final ClassMetadata meta;
	private DBClass parent;
	private final Set<DBClass> children;

	public DBClass(final String name, final Long id, final ClassMetadata meta, final List<DBAttribute> attributes) {
		super(name, id, attributes);
		this.meta = meta;
		children = Sets.newHashSet();
	}

	@Deprecated
	public DBClass(final String name, final Long id, final List<DBAttribute> attributes) {
		this(name, id, new ClassMetadata(), attributes);
	}

	@Override
	public void accept(final CMEntryTypeVisitor visitor) {
		visitor.visit(this);
	}

	@Override
	public void accept(final DBEntryTypeVisitor visitor) {
		visitor.visit(this);
	}

	@Override
	protected final ClassMetadata meta() {
		return meta;
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}

	@Override
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
		final Set<DBClass> leaves = Sets.newHashSet();
		addLeaves(leaves, this);
		return leaves;
	}

	private void addLeaves(final Set<DBClass> leaves, final DBClass currentClass) {
		if (currentClass.isSuperclass()) {
			for (final DBClass subclass : currentClass.getChildren()) {
				addLeaves(leaves, subclass);
			}
		} else {
			leaves.add(currentClass);
		}
	}

	@Override
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

	@Override
	public boolean holdsHistory() {
		return meta().holdsHistory();
	}

}
