package org.cmdbuild.dao.entrytype;

import java.util.List;
import java.util.Set;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.cmdbuild.common.Builder;

import com.google.common.collect.Lists;
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

	public static class DBClassBuilder implements Builder<DBClass> {

		private final List<DBAttribute> attributes;

		private String name;
		private Long id;
		private ClassMetadata metadata;
		private final Set<DBClass> children;

		private DBClassBuilder() {
			metadata = new ClassMetadata();
			attributes = Lists.newArrayList();
			children = Sets.newHashSet();
		}

		public DBClassBuilder withName(final String name) {
			this.name = name;
			return this;
		}

		public DBClassBuilder withId(final Long id) {
			this.id = id;
			return this;
		}

		public DBClassBuilder withAllMetadata(final ClassMetadata metadata) {
			this.metadata = metadata;
			return this;
		}

		public DBClassBuilder withAllAttributes(final List<DBAttribute> attributes) {
			this.attributes.addAll(attributes);
			return this;
		}

		@Override
		public DBClass build() {
			return new DBClass(this);
		}

	}

	public static DBClassBuilder newClass() {
		return new DBClassBuilder();
	}

	private final ClassMetadata meta;
	private DBClass parent;
	private final Set<DBClass> children;

	private DBClass(final DBClassBuilder builder) {
		super(builder.name, builder.id, builder.attributes);
		this.meta = builder.metadata;
		this.children = builder.children;
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
		return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
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
