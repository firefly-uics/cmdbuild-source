package org.cmdbuild.elements.proxy;

import java.sql.ResultSet;
import java.util.Date;
import java.util.Map;

import org.cmdbuild.dao.backend.postgresql.QueryComponents.QueryAttributeDescriptor;
import org.cmdbuild.elements.AttributeValue;
import org.cmdbuild.elements.interfaces.ICard;
import org.cmdbuild.elements.interfaces.ITable;
import org.cmdbuild.exception.ORMException;

public class CardForwarder implements ICard {

	protected ICard c;

	protected CardForwarder() {
		this.c = null;
	}

	protected CardForwarder(ICard c) {
		this.c = c;
	}

	protected ICard get() {
		return c;
	}

	public boolean equals(Object o) {
		if (o instanceof ICard)
			return get().equals(o);
		return false;
	}

	public int hashCode() {
		return get().hashCode();
	}

	public String toString() { return get().toString(); }
	public Date getBeginDate() { return get().getBeginDate(); }
	public String getCode() { return get().getCode(); }
	public String getDescription() { return get().getDescription(); }
	public int getId() { return get().getId(); }
	public int getIdClass() { return get().getIdClass(); }
	public String getNotes() { return get().getNotes(); }
	public ITable getSchema() { return get().getSchema(); }
	public ElementStatus getStatus() { return get().getStatus(); }
	public String getUser() { return get().getUser(); }
	public void setBeginDate(Date date) { get().setBeginDate(date); }
	public void setCode(String code) { get().setCode(code); }
	public void setDescription(String description) { get().setDescription(description); }
	public void setIdClass(Integer idClass) { get().setIdClass(idClass); }
	public void setNotes(String notes) { get().setNotes(notes); }
	public void setStatus(ElementStatus status) { get().setStatus(status); }
	public void setUser(String user) { get().setUser(user); }
	public String getPrimaryKeyCondition() { return get().getPrimaryKeyCondition(); }
	public AttributeValue getAttributeValue(String attrName) { return get().getAttributeValue(attrName); }
	public Map<String, AttributeValue> getAttributeValueMap() { return get().getAttributeValueMap(); }
	public boolean isNew() { return get().isNew(); }
	public void save() throws ORMException { get().save(); }
	public void forceSave() { get().forceSave(); }
	public void setAttributeValueMap(Map<String, AttributeValue> values) { get().setAttributeValueMap(values); }

	/******************************* TO BE CHANGED */
	public void resetAttributes() { get().resetAttributes(); }

	public void delete() throws ORMException { get().delete(); }

	public Map<String, Object> getExtendedProperties() { return get().getExtendedProperties(); };

	public Object getValue(String name) { return get().getValue(name); }
	public void setValue(String name, Object value) { get().setValue(name, value); }
	public void setValue(String name, ResultSet rs, QueryAttributeDescriptor qad) { get().setValue(name, rs, qad); }
}
