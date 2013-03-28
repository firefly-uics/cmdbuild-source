package org.cmdbuild.dao.query.clause;

import org.cmdbuild.common.utils.UnsupportedProxyFactory;
import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.dao.entrytype.ForwardingClass;

public class AnyClass extends ForwardingClass {

	private static final AnyClass ANY_CLASS = new AnyClass();

	public static CMClass anyClass() {
		return ANY_CLASS;
	}

	private static final String TO_STRING = "*";

	private AnyClass() {
		super(UnsupportedProxyFactory.of(CMClass.class).create());
	}

	@Override
	public String toString() {
		return TO_STRING;
	}

}
