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
}
