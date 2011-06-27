package org.cmdbuild.dao.entrytype;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang.builder.ToStringBuilder;

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

	private DBClass parent;
	private Set<DBClass> children;

	public DBClass(final String name, final Object id, final ClassMetadata meta, final Collection<DBAttribute> attributes) {
		super(name, id, meta, attributes);
		children = new HashSet<DBClass>();
	}

	@Deprecated
	public DBClass(final String name, final Object id, final Collection<DBAttribute> attributes) {
		this(name, id, new ClassMetadata(), attributes);
	}

	protected ClassMetadata getMeta() {
		return (ClassMetadata) super.getMeta();
	}

	public String toString() {
		return ToStringBuilder.reflectionToString(this);
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
		return getMeta().isSuperclass(); 
	}
}
