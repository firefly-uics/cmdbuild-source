package org.cmdbuild.workflow.xpdl;

import org.cmdbuild.workflow.utils.SimpleXMLNode;

public class XPDLExtendedAttribute {

	String name;
	String value;
	
	public XPDLExtendedAttribute(String name, String value) {
		this.name = name;
		this.value = value;
	}
	
	public void putSimpleXML( SimpleXMLNode parent ) {
		parent.createChild("ExtendedAttribute")
		.put("Name", name).put("Value", value);
	}
}
