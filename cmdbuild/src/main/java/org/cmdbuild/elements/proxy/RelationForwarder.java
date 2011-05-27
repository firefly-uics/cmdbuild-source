package org.cmdbuild.elements.proxy;

import java.sql.ResultSet;
import java.util.Map;

import org.cmdbuild.dao.backend.postgresql.QueryComponents.QueryAttributeDescriptor;
import org.cmdbuild.elements.AttributeValue;
import org.cmdbuild.elements.DirectedDomain;
import org.cmdbuild.elements.interfaces.ICard;
import org.cmdbuild.elements.interfaces.IDomain;
import org.cmdbuild.elements.interfaces.IRelation;
import org.cmdbuild.exception.ORMException;

public class RelationForwarder implements IRelation {

	protected IRelation r;

	protected RelationForwarder(IRelation r) {
		this.r = r;
	}

	public void save() { r.save(); }
	public void delete() throws ORMException { r.delete(); }
	public boolean equals(Object o) { return r.equals(o); }

	public ICard getCard1() { return r.getCard1(); }
	public ICard getCard2() { return r.getCard2(); }
	public IDomain getSchema() { return r.getSchema(); }

	public void setCard1(ICard card1) { r.setCard1(card1); }
	public void setCard2(ICard card2) { r.setCard2(card2); }
	public void setSchema(IDomain schema) { r.setSchema(schema); }
	public ElementStatus getStatus() { return r.getStatus(); }
	public void setStatus(ElementStatus status) { r.setStatus(status); }

	public String getPrimaryKeyCondition() { return r.getPrimaryKeyCondition(); }
	public AttributeValue getAttributeValue(String attrName) { return r.getAttributeValue(attrName); }
	public Map<String, AttributeValue> getAttributeValueMap() { return r.getAttributeValueMap(); }

	public int getId() { return r.getId(); }
	public boolean isNew() { return r.isNew(); }
	public void resetAttributes() { r.resetAttributes(); }
	public void setAttributeValueMap(Map<String, AttributeValue> values) { r.setAttributeValueMap(values); }

	public boolean isReversed() { return r.isReversed(); }

	public DirectedDomain getDirectedDomain() { return r.getDirectedDomain(); }

	public Map<String, Object> getExtendedProperties() { return r.getExtendedProperties(); }

	public Object getValue(String name) { return r.getValue(name); }
	public void setValue(String name, Object value) { r.setValue(name, value); }
	public void setValue(String name, ResultSet rs, QueryAttributeDescriptor qad) { r.setValue(name, rs, qad); }

}
