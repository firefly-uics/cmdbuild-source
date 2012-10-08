package org.cmdbuild.elements;

import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;

import org.cmdbuild.dao.backend.CMBackend;
import org.cmdbuild.dao.backend.postgresql.QueryComponents.QueryAttributeDescriptor;
import org.cmdbuild.elements.filters.AttributeFilter;
import org.cmdbuild.elements.interfaces.BaseSchema;
import org.cmdbuild.elements.interfaces.IAbstractElement;
import org.cmdbuild.elements.interfaces.ICard.CardAttributes;
import org.cmdbuild.exception.NotFoundException.NotFoundExceptionType;
import org.cmdbuild.exception.ORMException;
import org.cmdbuild.exception.ORMException.ORMExceptionType;
import org.cmdbuild.logger.Log;
import org.springframework.beans.factory.annotation.Autowired;

public abstract class AbstractElementImpl implements IAbstractElement {

	@Autowired
	protected CMBackend backend = CMBackend.INSTANCE;

	private static final long serialVersionUID = 1L;

	protected Map<String,AttributeValue> values;

	protected BaseSchema schema;
	protected String status;

	protected AbstractElementImpl() {
		values = new HashMap<String, AttributeValue>();
	}

	public abstract String toString();

	public AttributeValue getAttributeValue(String attrName) {
		if (values.containsKey(attrName)) {
			return values.get(attrName);
		} else if (schema.getAttributes().containsKey(attrName)) {
			AttributeValue value = new AttributeValue(schema.getAttributes().get(attrName));
			values.put(value.getSchema().getName(), value);
			return value;
		} else {
			throw NotFoundExceptionType.ATTRIBUTE_NOTFOUND.createException(schema.toString(), attrName);
		}
	}

	public void setAttributeValueMap(Map<String,AttributeValue> values) {
//		values.clear();
//		for (Entry<String,AttributeValue> item : values.entrySet()) {
//			
//		}
		this.values = values;
	}

	public Map<String,AttributeValue> getAttributeValueMap() {
		return values;
	}

	public int getId() {
		Integer id = null;
		if (values.containsKey("Id")) {
			id = values.get("Id").getInt();
		}
		if (id != null && id > 0) {
			return id;
		} else {
			return -1;
		}
	}

	public void save() throws ORMException {
		setDefaultValueIfPresent(CardAttributes.Status.toString(), ElementStatus.ACTIVE.value());
		if (isNew()) {
			int id = create();
			if (id > 0) { // HACK to allow triggers to ignore inserts
				setValue(CardAttributes.Id.toString(), (Integer)id);
				resetAttributes();
			}
		} else if (hasChanged()) {
			modify();
			resetAttributes();
		}
	}

	public void forceSave() {
		if (!hasChanged()) {
			// Force updating any (the first) value if nothing has changed
			if (values.isEmpty()) {
				Log.PERSISTENCE.warn("No values to force update!");
			} else { 
				values.values().iterator().next().setChanged(true);
			}
		}
		save();
	}

	protected final void setDefaultValueIfPresent(String name, Object value) {
		if (getSchema().getAttributes().containsKey(name)) {
			if (!values.containsKey(name)) {
				setValue(name, value);
			}
		} else {
			values.remove(name);
		}
	}

	protected final boolean hasChanged() {
		for (AttributeValue av : values.values()) {
			if (av.isChanged()) {
				return true;
			}
		}
		return false;
	}

	public final void delete() throws ORMException {
		if (isNew()) {
			throw NotFoundExceptionType.NOTFOUND.createException(this.toString());
		}
		if (getSchema().getAttributes().containsKey(CardAttributes.Status.toString())) {
			logicalDelete();
		} else {
			physicalDelete();
		}
	}

	private final void logicalDelete() {
		this.setStatus(ElementStatus.INACTIVE);
		this.save();
	}

	private final void physicalDelete() {
		backend.deleteElement(this);
	}

	public String getPrimaryKeyCondition() {
		return AttributeFilter.getEquals(this, "Id", getValue("Id").toString()).toString();
	}

	public boolean isNew() {
		return (getId() <= 0);
	}

	public void resetAttributes(){
		for(AttributeValue attr : values.values()){
			attr.setChanged(false);
		}
	}

	public BaseSchema getSchema() {
		return this.schema;
	}

	public ElementStatus getStatus(){
		return ElementStatus.fromString((String)getValue(ElementAttributes.Status.toString()));
	}

	public void setStatus(ElementStatus status){
		setValue(ElementAttributes.Status.toString(), status.value());
	}

	public boolean equals(Object o) {
		if (o instanceof IAbstractElement) {
			IAbstractElement e = ((IAbstractElement) o);
			return (!isNew() && getSchema().equals(e.getSchema()) && this.getId() == e.getId());
		}
		return false;
	}

	public int hashCode() {
		Integer id = getId();
		return id.hashCode();
	}

	protected abstract int create() throws ORMException;

	protected abstract void modify() throws ORMException;

	public Map<String, Object> getExtendedProperties() {
		return new HashMap<String, Object>();
	}

	public Object getValue(String name) {
		return getAttributeValue(name).getObject();
	}

	public Map<String, String> getDBQuotedValues(String name) {
		Map<String, String> dbQuotedValues = new HashMap<String, String>();
		for (Map.Entry<String, AttributeValue> av : values.entrySet()) {
			String key = av.getKey();
			Object value = getAttributeValue(name).getObject();
			String quotedValue = getSchema().getAttribute(name).valueToDBFormat(value);
			dbQuotedValues.put(key, quotedValue);
		}
		return dbQuotedValues;
	}

	public void setValue(String name, Object value) {
		if (value instanceof AttributeValue) {
			throw ORMExceptionType.ORM_TYPE_ERROR.createException();
		}
		getAttributeValue(name).setValue(value);
	}

	// TODO: Remove this as soon as possible
	public void setValue(String name, ResultSet rs, QueryAttributeDescriptor qad) {
		getAttributeValue(name).setValue(rs, qad);
	}
}
