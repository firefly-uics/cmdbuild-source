package org.cmdbuild.api.fluent;

public class CardDescriptor {

	private final String className;
	private final int id;

	public CardDescriptor(final String className, final int id) {
		this.className = className;
		this.id = id;
	}

	public String getClassName() {
		return className;
	}

	public int getId() {
		return id;
	}

}
