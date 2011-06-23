package org.cmdbuild.dao.entrytype;

import java.util.Collection;

import org.apache.commons.lang.builder.ToStringBuilder;

public class DBDomain extends DBEntryType implements CMDomain {

	public static class DomainMetadata extends EntryTypeMetadata {
		public static final String CLASS_1 = BASE_NS + "class1";
		public static final String CLASS_2 = BASE_NS + "class2";
		public static final String DESCRIPTION_1 = BASE_NS + "description1";
		public static final String DESCRIPTION_2 = BASE_NS + "description2";
	}

	// FIXME
	private DBClass class1;
	private DBClass class2;
	private String description1;
	private String description2;

	public DBDomain(final String name, final Object id, final Collection<DBAttribute> attributes) {
		super(name, id, attributes);
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
		return description1;
	}

	public void setDescription1(String description1) {
		this.description1 = description1;
	}

	@Override
	public String getDescription2() {
		return description2;
	}

	public void setDescription2(String description2) {
		this.description2 = description2;
	}	
}
