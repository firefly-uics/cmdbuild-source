package org.cmdbuild.cmdbf.xml;

import java.util.HashMap;
import java.util.Map;

import org.apache.ws.commons.schema.XmlSchemaAnnotated;
import org.apache.ws.commons.schema.XmlSchemaAnnotation;
import org.apache.ws.commons.schema.XmlSchemaAppInfo;
import org.apache.ws.commons.schema.XmlSchemaObject;
import org.cmdbuild.cmdbf.CMDBfUtils;
import org.cmdbuild.config.CmdbfConfiguration;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

abstract public class AbstractNamespace implements XmlNamespace {
	
	private String name;
	private XmlRegistry registry;
	private CmdbfConfiguration configuration;
	
	public void setRegistry(XmlRegistry registry) {
		this.registry = registry;
	}
	
	public XmlRegistry getRegistry() {
		return registry;
	}
		
	public AbstractNamespace(String name, CmdbfConfiguration configuration) {
		this.name = name;
		this.configuration = configuration;
	}
	
	@Override
	public boolean isEnabled() {
		return true;
	} 
	
	@Override
	public String getSystemId() {
		return name + ".xsd";
	}
	
	@Override
	public String getNamespaceURI() {
		return configuration.getMdrId() + "/" + name ;
	}
	
	@Override
	public String getNamespacePrefix() {
		return name;
	}
	
	@Override
	public String getSchemaLocation() {
		return configuration.getSchemaLocation() + "/" + getSystemId();
	}
	@Override
	public boolean serialize(Node xml, Object entry) {
		return false;
	}
	
	@Override	
	public Object deserialize(Node xml) {
		return null;
	}
	
	@Override
	public boolean serializeValue(Node xml, Object entry) {
		return false;
	}
	
	@Override	
	public Object deserializeValue(Node xml, Object type) {
		return null;
	}
	
	
	protected Map<String, String> getAnnotations(XmlSchemaAnnotated annotated) {
		Map<String, String> properties = new HashMap<String, String>();		
		XmlSchemaAnnotation annotation = annotated.getAnnotation();
		if(annotation != null) {
			for(int i=0; i<annotation.getItems().getCount(); i++) {
				XmlSchemaObject item = annotation.getItems().getItem(i);
				if(item instanceof XmlSchemaAppInfo) {
					XmlSchemaAppInfo appInfo = (XmlSchemaAppInfo)item;
					NodeList nodeList = appInfo.getMarkup();
					if(nodeList != null) {
						for(int j=0; j<nodeList.getLength(); j++) {
							Node node = nodeList.item(j);
							if(node instanceof Element){
								Element element = (Element)node;
								if(CMDBfUtils.CMDBUILD_NS.equals(element.getNamespaceURI()))
									properties.put(element.getLocalName(), element.getTextContent());
							}
						}
					}
				}
			}
		}
		return properties;
	}
    
    protected void setAnnotations(XmlSchemaAnnotated annotated, Map<String, String> properties, Document document) {
    	if(!properties.isEmpty()) {
	    	XmlSchemaAnnotation annotation = new XmlSchemaAnnotation();
			XmlSchemaAppInfo appInfo = new XmlSchemaAppInfo();
			DocumentFragment appInfoMarkup = document.createDocumentFragment();
			for(String name : properties.keySet()) {
				Element element = document.createElementNS(CMDBfUtils.CMDBUILD_NS, name);
				element.setTextContent(properties.get(name));				
				appInfoMarkup.appendChild(element);
			}
			appInfo.setMarkup(appInfoMarkup.getChildNodes());
			annotation.getItems().add(appInfo);
			annotated.setAnnotation(annotation);
    	}
    }
}
