package org.cmdbuild.workflow.xpdl;

import org.cmdbuild.workflow.utils.SimpleXMLNode;

public class XPDLFormalParameterDescriptor {
	public enum FormalParameterMode {
		IN,
		OUT,
		INOUT;
	}
	
	String id;
	XPDLAttributeType xpdlType;
	FormalParameterMode mode;
	
	public XPDLFormalParameterDescriptor(XPDLAttributeType type, FormalParameterMode mode, String id){
		this.xpdlType = type;
		this.mode = mode;
		this.id = id;
	}
	
	public void putSimpleXML( SimpleXMLNode parent ) {
		SimpleXMLNode param = parent.createChild("FormalParameter")
		.put("Id", id).copy("Id", "Index").put("Mode", mode.name());
		xpdlType.putSimpleXML(param);
	}
	
}
