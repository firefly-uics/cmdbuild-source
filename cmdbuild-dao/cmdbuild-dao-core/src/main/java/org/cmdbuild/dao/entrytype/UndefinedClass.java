package org.cmdbuild.dao.entrytype;



/**
 * This represents a class that has to be checked at runtime
 */
public class UndefinedClass extends PlaceholderClass {

	public static final UndefinedClass UNDEFINED_CLASS = new UndefinedClass();

	private UndefinedClass() {
	}

	@Override
	public String getName() {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isAncestorOf(CMClass cmClass) {
		throw new UnsupportedOperationException();
	}
}
