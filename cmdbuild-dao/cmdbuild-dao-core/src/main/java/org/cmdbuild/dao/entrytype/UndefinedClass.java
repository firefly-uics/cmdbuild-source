package org.cmdbuild.dao.entrytype;

/**
 * This represents a class that has to be checked at runtime
 */
public class UndefinedClass extends PlaceholderClass {

	public static final UndefinedClass UNDEFINED_CLASS = new UndefinedClass();

	private UndefinedClass() {
	}

	/*
	 * Object overrides
	 */

	@Override
	public String toString() {
		return "?";
	}
}
