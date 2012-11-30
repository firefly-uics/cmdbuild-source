package org.cmdbuild.dao.entrytype;

import java.util.List;

import org.cmdbuild.common.Builder;

import com.google.common.collect.Lists;

public class DBDomain extends DBEntryType implements CMDomain {

	public static class DomainMetadata extends EntryTypeMetadata {

		public static final String CLASS_1 = BASE_NS + "class1";
		public static final String CLASS_2 = BASE_NS + "class2";
		public static final String DESCRIPTION_1 = BASE_NS + "description1";
		public static final String DESCRIPTION_2 = BASE_NS + "description2";

		String getDescription1() {
			return get(DESCRIPTION_1);
		}

		void setDescription1(final String description) {
			put(DESCRIPTION_1, description);
		}

		String getDescription2() {
			return get(DESCRIPTION_2);
		}

		void setDescription2(final String description) {
			put(DESCRIPTION_2, description);
		}

	}

	public static class DBDomainBuilder implements Builder<DBDomain> {

		private final List<DBAttribute> attributes;

		private String name;
		private Long id;
		private DomainMetadata metadata;
		private DBClass class1;
		private DBClass class2;

		private DBDomainBuilder() {
			metadata = new DomainMetadata();
			attributes = Lists.newArrayList();
		}

		public DBDomainBuilder withName(final String name) {
			this.name = name;
			return this;
		}

		public DBDomainBuilder withId(final Long id) {
			this.id = id;
			return this;
		}

		public DBDomainBuilder withAllMetadata(final DomainMetadata metadata) {
			this.metadata = metadata;
			return this;
		}

		public DBDomainBuilder withAllAttributes(final List<DBAttribute> attributes) {
			this.attributes.addAll(attributes);
			return this;
		}

		public DBDomainBuilder withAttribute(final DBAttribute attribute) {
			this.attributes.add(attribute);
			return this;
		}

		@Deprecated
		public DBDomainBuilder withClass1(final DBClass dbClass) {
			this.class1 = dbClass;
			return this;
		}

		@Deprecated
		public DBDomainBuilder withClass2(final DBClass dbClass) {
			this.class2 = dbClass;
			return this;
		}

		@Override
		public DBDomain build() {
			return new DBDomain(this);
		}

	}

	private final DomainMetadata metadata;

	@Deprecated
	private final DBClass class1;
	@Deprecated
	private final DBClass class2;

	private DBDomain(final DBDomainBuilder builder) {
		super(builder.name, builder.id, builder.attributes);
		this.metadata = builder.metadata;
		this.class1 = builder.class1;
		this.class2 = builder.class2;
	}

	// @Deprecated
	// public DBDomain(final String name, final Long id, final List<DBAttribute>
	// attributes) {
	// this(name, id, new DomainMetadata(), attributes);
	// }

	@Override
	public void accept(final CMEntryTypeVisitor visitor) {
		visitor.visit(this);
	}

	@Override
	public void accept(final DBEntryTypeVisitor visitor) {
		visitor.visit(this);
	}

	@Override
	protected final DomainMetadata meta() {
		return metadata;
	}

	@Override
	public String toString() {
		return String.format("[Domain %s]", getName());
	}

	@Override
	public final String getPrivilegeId() {
		return String.format("Domain:%d", getId());
	}

	@Override
	public DBClass getClass1() {
		if (class1 == null) {
			throw new IllegalStateException();
		}
		return class1;
	}

	@Override
	public DBClass getClass2() {
		if (class2 == null) {
			throw new IllegalStateException();
		}
		return class2;
	}

	@Override
	public String getDescription1() {
		return meta().getDescription1();
	}

	public void setDescription1(final String description1) {
		meta().setDescription1(description1);
	}

	@Override
	public String getDescription2() {
		return meta().getDescription2();
	}

	public void setDescription2(final String description2) {
		meta().setDescription2(description2);
	}

	@Override
	public boolean holdsHistory() {
		return true;
	}

	public static DBDomainBuilder newDomain() {
		return new DBDomainBuilder();
	}

}
