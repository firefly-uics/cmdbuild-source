package org.cmdbuild.elements.interfaces;

import java.sql.ResultSet;
import java.util.Map;

import org.cmdbuild.dao.backend.postgresql.QueryComponents.QueryAttributeDescriptor;
import org.cmdbuild.elements.AttributeValue;
import org.cmdbuild.exception.ORMException;

public interface IAbstractElement extends ObjectWithId {

	public enum ElementAttributes {
		Status("Status");

		private final String descr;

		ElementAttributes(String descr) {
			this.descr = descr;
		}

		public String toString() {
			return descr;
		}
	}

	public enum ElementStatus {
		ACTIVE("A"), UPDATED("U"), INACTIVE("N"), INACTIVE_USER("D");

		private final String value;

		ElementStatus(String value) {
			this.value = value;
		}

		public String value() {
			return value;
		}

		public boolean isActive() {
			return ACTIVE.equals(this);
		}

		public static ElementStatus fromString(String value) {
			for (ElementStatus status : ElementStatus.values()) {
				if (status.value.equals(value))
					return status;
			}
			return ElementStatus.INACTIVE;
		}
	}

	public AttributeValue getAttributeValue(String attrName);

	public void setValue(String name, Object value);
	public void setValue(String name, ResultSet rs, QueryAttributeDescriptor qad);
	public Object getValue(String name);

	public ElementStatus getStatus();
	public void setStatus(ElementStatus status);

	public void setAttributeValueMap(Map<String,AttributeValue> values);
	public Map<String,AttributeValue> getAttributeValueMap();

	public int getId();
	public boolean isNew();
	public String getPrimaryKeyCondition();

	public void save() throws ORMException;
	public void delete() throws ORMException;

	public BaseSchema getSchema();

	/**********************************************************
	 * CHANGE THIS! MAKE DEPRECATED AND USE SUPPRESSWARNINGS? *
	 **********************************************************/
	public void resetAttributes();

	public Map<String, Object> getExtendedProperties();

	/*
	 * Should be implemented to use sets
	 */
	public int hashCode();
	public boolean equals(Object obj);
}
