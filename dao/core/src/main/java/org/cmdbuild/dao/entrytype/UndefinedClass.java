package org.cmdbuild.dao.entrytype;

import org.cmdbuild.common.utils.UnsupportedProxyFactory;

/**
 * This represents a class that has to be checked at runtime
 */
public class UndefinedClass extends ForwardingClass {

	public static final UndefinedClass UNDEFINED_CLASS = new UndefinedClass();
	private static final String TO_STRING = "?";

	private UndefinedClass() {
		super(UnsupportedProxyFactory.of(CMClass.class).create());
	}

	@Override
	public String toString() {
		return TO_STRING;
	}

}
