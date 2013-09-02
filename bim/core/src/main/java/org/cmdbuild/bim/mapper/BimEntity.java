package org.cmdbuild.bim.mapper;

import java.util.Iterator;
import java.util.List;

import org.cmdbuild.bim.model.Attribute;
import org.cmdbuild.bim.model.Entity;
import org.cmdbuild.bim.service.BimError;

import com.google.common.collect.Lists;

public class BimEntity implements Entity {

	private List<Attribute> attributes;
	private String typeName;
	
	
	public BimEntity(String typeName) {
		this.typeName = typeName;
		this.attributes = Lists.newArrayList();
	}

	@Override
	public boolean isValid() {
		return true;
	}

	@Override
	public List<Attribute> getAttributes() {
		return attributes;
	}
	
	public void setAttributes(List<Attribute> attributes){
		this.attributes = attributes;
	}

	@Override
	public Attribute getAttributeByName(String attributeName) {
		Attribute attribute = Attribute.NULL_ATTRIBUTE;
		for(Iterator<Attribute> it = attributes.iterator(); it.hasNext();){
			BimAttribute readedAttribute = (BimAttribute) it.next();
			if(readedAttribute.getName().equals(attributeName)){
				attribute = readedAttribute;
				break;
			}
		}
		return attribute;
	}

	@Override
	public String getKey() {
		String identifier = "";
		Attribute guid = getAttributeByName(KEYATTRIBUTE);
		if(guid.isValid()){
			identifier = guid.getValue();
		}
		return identifier;
	}

	@Override
	public String getTypeName() {
		return typeName;
	}
	
	public void setTypeName(String typeName) {
		this.typeName = typeName;
	}

	public void addAttribute(Attribute readedAttribute) {
		attributes.add(readedAttribute);
	}

	@Override
	public String getContainerKey() {
		throw new BimError("Can not call getContainerKey on BimEntity class");
	}

}
