package org.cmdbuild.dao.query.clause.alias;



public class UndefinedClassAlias extends ClassAlias {

	public static final UndefinedClassAlias UNDEFINED_CLASS_ALIAS = new UndefinedClassAlias();

	private UndefinedClassAlias() {
		super();
	}

	public Alias getAlias() {
		throw new UnsupportedOperationException();
	}
}
