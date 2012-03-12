package org.cmdbuild.elements;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

import org.cmdbuild.dao.backend.postgresql.QueryComponents.QueryAttributeDescriptor;
import org.cmdbuild.dao.type.ByteArray;
import org.cmdbuild.dao.type.IntArray;
import org.cmdbuild.dao.type.StringArray;
import org.cmdbuild.elements.interfaces.IAttribute;
import org.cmdbuild.elements.interfaces.ObjectWithId;
import org.cmdbuild.elements.interfaces.IAttribute.AttributeType;
import org.cmdbuild.exception.ORMException;
import org.cmdbuild.exception.ORMException.ORMExceptionType;
import org.cmdbuild.logger.Log;
import org.postgresql.util.PGobject;

public class AttributeValue implements Serializable {

	private static final long serialVersionUID = 1L;

	private IAttribute schema = null;

	private Object value;

	private boolean isChanged = false;
	private boolean loading = false; // used to disable change check on load

	public AttributeValue(IAttribute attribute) {
		schema = attribute;
	}

	public IAttribute getSchema() {
		return schema;
	}

	public void setValue(Object maybeValue) {
		Object value = schema.readValue(maybeValue);
		isChanged = isDifferent(this.value, value);
		this.value = value;
	}


	private boolean isDifferent(Object obj1, Object obj2) {
		if (this.loading) // if loading from db
			return false;
		if (this.isChanged) // if already changed
			return true;
		if (obj1 == null)
			return (obj2 != null);
		else
			return (!obj1.equals(obj2));
	}

	public void setValue(ResultSet rs, QueryAttributeDescriptor qad) throws ORMException {
		try {
			this.loading = true;
			final int valueIndex = qad.getValueIndex();
			Object value = rs.getObject(valueIndex);
			if (value instanceof java.sql.Array) {
				value = ((java.sql.Array)value).getArray();
			} else if (value instanceof PGobject) {
				value = ((PGobject)value).getValue();
			}
			if (schema.getType() == AttributeType.REFERENCE) {
				int refId = rs.getInt(valueIndex);
				if (refId > 0) {
					final String refDescription = rs.getString(qad.getDescriptionIndex());
					value = new Reference(schema.getReferenceDirectedDomain(), refId, refDescription, true);
				} else {
					value = null;
				}
			}
			setValue(value);
		} catch (SQLException ex) {
			Log.PERSISTENCE.error("Unable to get value from result set", ex);
			throw ORMExceptionType.ORM_ERROR_GENERIC_SQL.createException(ex.getMessage());
		} finally {
			this.loading = false;
		}
	}

	public void setValueForceChange(String value) {
		setValue(value);
		isChanged = true;
	}
		
	public boolean isNull(){
		return value == null;
	}

	/**
	 * Returns the id for lookup and reference types
	 * 
	 * @return type id value
	 */
	public Integer getId() {
		if (value instanceof ObjectWithId) {
			return ((ObjectWithId) value).getId();
		} else {
			return null;
		}
	}

	public String toString() {
		return getSchema().valueToString(value);
	}

	public void setChanged(boolean isChanged) {
		this.isChanged = isChanged;
	}

	public boolean isChanged() {
		return isChanged;
	}

	public Integer getInt() {
		return (Integer) value;
	}

	public String getString() {
		return (String) value;
	}

	public Boolean getBoolean() {
		return (Boolean) value;
	}

	public Date getDate() {
		return (Date)value;
	}

	public Double getDouble() {
		return (Double) value;
	}

	public Reference getReference() {
		return (Reference) value;
	}

	public Lookup getLookup() {
		return (Lookup) value;
	}

	public byte[] getBinary() {
		if (value != null) {
			return ((ByteArray)value).getValue();
		} else {
			return null;
		}
	}

	public int[] getIntArrayValue() {
		if (value != null) {
			return ((IntArray)value).getValue();
		} else {
			return null;
		}
	}
	
	public String[] getStringArrayValue() {
		if (value != null) {
			return ((StringArray)value).getValue();
		} else {
			return null;
		}
	}

	public boolean isValidId() {
		Integer id = this.getId();
		return (id != null) && (id > 0);
	}

	public String quote() {
		return getSchema().valueToDBFormat(value);
	}

	public Object getObject() {
		return value;
	}

}
