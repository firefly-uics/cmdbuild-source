package org.cmdbuild.dao.entrytype.attributetype;

public abstract class AbstractAttributeType<T> implements CMAttributeType<T> {

	@Override
	public final T convertValue(Object value) {
		if (value == null) {
			return null;
		} else {
			return convertNotNullValue(value);
		}
	}

	/**
	 * Casts a value that is assumed not to be null to the native type.
	 * 
	 * @param not null value of any type
	 * @return value of the native type
	 */
	protected abstract T convertNotNullValue(Object value);
}
