package org.cmdbuild.dao.entrytype.attributetype;


public interface CMAttributeType<T> {

	public T convertNotNullValue(Object value);
}
