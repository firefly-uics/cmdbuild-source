package org.cmdbuild.dao.entrytype.attributetype;


public interface CMAttributeType<T> {

	T convertNotNullValue(Object value);
	void accept(CMAttributeTypeVisitor visitor);
}
