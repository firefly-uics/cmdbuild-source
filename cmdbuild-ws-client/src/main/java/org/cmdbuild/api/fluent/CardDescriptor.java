package org.cmdbuild.api.fluent;

public class CardDescriptor {

	private String className;
	private int id;

	public String getClassName() {
		return className;
	}

	void setClassName(final String className) {
		this.className = className;
	}

	public int getId() {
		return id;
	}

	void setId(final int id) {
		this.id = id;
	}

	public static CardDescriptor newInstance(final String className, final int id) {
		return new ExistingCard(null) //
				.forClassName(className) //
				.withId(id);
	}

}
