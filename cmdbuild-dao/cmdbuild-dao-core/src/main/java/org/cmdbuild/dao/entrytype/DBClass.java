package org.cmdbuild.dao.entrytype;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang.builder.ToStringBuilder;

public class DBClass extends DBEntryType implements CMClass {

	private DBClass parent;
	private Set<DBClass> children;

	public DBClass(final String name, final Object id, final Collection<DBAttribute> attributes) {
		super(name, id, attributes);
		children = new HashSet<DBClass>();
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
		return children.isEmpty(); // FIXME IMPORTANT! It should be a flag marking is as a superclass, but this will suffice FOR NOW! 
	}
}
