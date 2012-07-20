package org.cmdbuild.api.fluent;

public class CardDescriptor {

	private final String className;
	private final Integer id;

	public CardDescriptor(final String className, final Integer id) {
		this.className = className;
		this.id = id;
	}

	public String getClassName() {
		return className;
	}

	public Integer getId() {
		return id;
	}

}
