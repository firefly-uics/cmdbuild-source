package org.cmdbuild.workflow.xpdl;

import org.cmdbuild.elements.interfaces.IAttribute;
import org.cmdbuild.workflow.utils.SimpleXMLNode;

public class XPDLAttribute {

	String id;
	XPDLAttributeType type;
	
	public XPDLAttribute(IAttribute cmdbAttr) {
		this.id = cmdbAttr.getName();
		this.type = XPDLAttributeType.fromCmdbuildType(cmdbAttr.getType());
	}
	public XPDLAttribute(String id, XPDLAttributeType type) {
		this.id = id;
		this.type = type;
	}
	
	public void putSimpleXML(SimpleXMLNode parent) {
		SimpleXMLNode node = parent.createChild("DataField")
		.put("Id", id).put("Name", id).put("IsArray", "FALSE");
		type.putSimpleXML(node);
	}
}
