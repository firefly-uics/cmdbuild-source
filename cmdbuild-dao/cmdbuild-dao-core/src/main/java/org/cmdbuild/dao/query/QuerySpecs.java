package org.cmdbuild.dao.query;

import static org.cmdbuild.dao.entrytype.UndefinedClass.UNDEFINED_CLASS;

import java.util.ArrayList;
import java.util.List;

import org.cmdbuild.dao.entrytype.CMAttribute;
import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.dao.entrytype.DBClass;

/*
 * Used by QuerySpecsBuilder and by driver tests only
 */
public class QuerySpecs {

	CMClass from;
	List<CMAttribute> attributes;
	Integer offset;
	Integer limit;

	protected QuerySpecs() {
		from = UNDEFINED_CLASS;
		attributes = new ArrayList<CMAttribute>();
		offset = null;
		limit = null;
	}

	/*
	 * This has to be a CMClass because 
	 */
	public void setFrom(final CMClass from) {
		this.from = from;
	}

	public DBClass getDBFrom() {
		// FIXME use the driver to transform CMClass in DBClass?
		return (DBClass) from;
	}

	public Iterable<CMAttribute> getAttributes() {
		return this.attributes;
	}

	public void addSelectAttribute(final CMAttribute attribute) {
		attributes.add(attribute);
	}

	public void setOffset(final Integer offset) {
		this.offset = offset;
	}

	public Integer getOffset() {
		return offset;
	}

	public void setLimit(final Integer limit) {
		this.limit = limit;
	}

	public Integer getLimit() {
		return limit;
	}
}
