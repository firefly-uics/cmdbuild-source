package org.cmdbuild.dao.query.clause.alias;

import static org.cmdbuild.dao.entrytype.UndefinedClass.UNDEFINED_CLASS;

import org.apache.commons.lang.Validate;
import org.cmdbuild.dao.entrytype.CMClass;

public class ClassAlias {

	private final CMClass type;
	private final Alias alias;

	public ClassAlias(final CMClass type, final Alias alias) {
		Validate.notNull(type);
		Validate.notNull(alias);
		this.type = type;
		this.alias = alias;
	}

	/*
	 * FIXME pretty ugly (used by UndefinedClassAlias)
	 */
	protected ClassAlias() {
		type = UNDEFINED_CLASS;
		alias = null;
	}

	public CMClass getType() {
		return type;
	}

	public Alias getAlias() {
		return alias;
	}
}
