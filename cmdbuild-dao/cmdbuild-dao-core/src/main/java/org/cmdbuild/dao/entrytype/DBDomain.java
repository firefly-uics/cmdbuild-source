package org.cmdbuild.dao.entrytype;

import java.util.List;

import org.apache.commons.lang.builder.ToStringBuilder;

public class DBDomain extends DBEntryType implements CMDomain {

	public static class DomainMetadata extends EntryTypeMetadata {
		public static final String CLASS_1 = BASE_NS + "class1";
		public static final String CLASS_2 = BASE_NS + "class2";
		public static final String DESCRIPTION_1 = BASE_NS + "description1";
		public static final String DESCRIPTION_2 = BASE_NS + "description2";

		final String getDescription1() {
			return get(DESCRIPTION_1);
		}

		final void setDescription1(final String description1) {
			put(DESCRIPTION_1, description1);
		}

		final String getDescription2() {
			return get(DESCRIPTION_2);
		}

		final void setDescription2(final String description2) {
			put(DESCRIPTION_2, description2);
		}
	}

	private final DomainMetadata meta;
	@Deprecated private DBClass class1;
	@Deprecated private DBClass class2;

	public DBDomain(final String name, final Object id, final DomainMetadata meta, final List<DBAttribute> attributes) {
		super(name, id, attributes);
		this.meta = meta;
	}

	@Deprecated
	public DBDomain(final String name, final Object id, final List<DBAttribute> attributes) {
		this(name, id, new DomainMetadata(), attributes);
	}

    public void accept(CMEntryTypeVisitor visitor) {
        visitor.visit(this);
    }

    public void accept(DBEntryTypeVisitor visitor) {
        visitor.visit(this);
    }

	protected final DomainMetadata meta() {
		return meta;
	}

	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}

	@Override
	public DBClass getClass1() {
		if (class1 == null) {
			throw new IllegalStateException();
		}
		return class1;
	}

	public void setClass1(final DBClass class1) {
		this.class1 = class1;
	}

	@Override
	public DBClass getClass2() {
		if (class2 == null) {
			throw new IllegalStateException();
		}
		return class2;
	}

	public void setClass2(final DBClass class2) {
		this.class2 = class2;
	}

	@Override
	public String getDescription1() {
		return meta().getDescription1();
	}

	public void setDescription1(String description1) {
		meta().setDescription1(description1);
	}

	@Override
	public String getDescription2() {
		return meta().getDescription2();
	}

	public void setDescription2(String description2) {
		meta().setDescription2(description2);
	}
}
