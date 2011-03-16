package org.cmdbuild.workflow.xpdl;

import java.util.ArrayList;
import java.util.List;

import org.cmdbuild.workflow.utils.SimpleXMLDoc;
import org.cmdbuild.workflow.utils.SimpleXMLNode;

public class XPDLApplicationDescriptor {

	String id;
	String description;
	List<XPDLFormalParameterDescriptor> formalParameters;
	
	List<XPDLExtendedAttribute> extendedAttributes;
	
	@SuppressWarnings("unchecked")
	public XPDLApplicationDescriptor() {
		this.id = "";
		this.description = "";
		this.formalParameters = new ArrayList();
		this.extendedAttributes = new ArrayList();
	}
	public XPDLApplicationDescriptor(
		String id,
		String descr,
		List<XPDLFormalParameterDescriptor> formalParameters,
		List<XPDLExtendedAttribute> extAttrs
	) {
		this.id = id;
		this.description = descr;
		this.formalParameters = formalParameters;
		this.extendedAttributes = extAttrs;
	}
	
	public void putSimpleXML( SimpleXMLNode parent ) {
		SimpleXMLNode appNode = parent.createChild("Application").put("Id", id).copy("Id", "Name");
		
		appNode.createChild("Description").set(description);
		
		SimpleXMLNode fps = appNode.createChild("FormalParameters");
		for( XPDLFormalParameterDescriptor fp : formalParameters ) {
			fp.putSimpleXML(fps);
		}
		
		SimpleXMLNode eas = appNode.createChild("ExtendedAttributes");
		for(XPDLExtendedAttribute ea : extendedAttributes) {
			ea.putSimpleXML(eas);
		}
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public List<XPDLFormalParameterDescriptor> getFormalParameters() {
		return formalParameters;
	}
	public void setFormalParameters(
			List<XPDLFormalParameterDescriptor> formalParameters) {
		this.formalParameters = formalParameters;
	}
	
	
	public void addFormalParameter(XPDLFormalParameterDescriptor formalParam) {
		this.formalParameters.add(formalParam);
	}
	public void addExtendedAttribute(XPDLExtendedAttribute ea) {
		this.extendedAttributes.add(ea);
	}
}
