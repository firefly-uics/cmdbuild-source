package org.cmdbuild.cmdbf.xml;

import java.util.Arrays;
import java.util.Collections;

import javax.xml.namespace.QName;

import org.apache.ws.commons.schema.XmlSchema;
import org.apache.ws.commons.schema.XmlSchemaAttribute;
import org.apache.ws.commons.schema.XmlSchemaCollection;
import org.apache.ws.commons.schema.XmlSchemaComplexType;
import org.apache.ws.commons.schema.XmlSchemaForm;
import org.apache.ws.commons.schema.XmlSchemaSimpleContent;
import org.apache.ws.commons.schema.XmlSchemaSimpleContentExtension;
import org.cmdbuild.config.CmdbfConfiguration;
import org.cmdbuild.dao.entry.IdAndDescription;
import org.cmdbuild.dao.entry.LookupValue;
import org.cmdbuild.data.store.lookup.LookupType;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class SystemNamespace extends AbstractNamespace{
	
	static final String LOOKUP_TYPE = "Lookup";
	static final String LOOKUP_ID = "lookupId";
	static final String LOOKUP_TYPE_NAME = "lookupType";
	static final String REFERENCE_TYPE = "Reference";	
	static final String REFERENCE_ID = "referenceId";

	public SystemNamespace(String name, CmdbfConfiguration cmdbfConfiguration) {
		super(name, cmdbfConfiguration);
	}

	@Override
	public XmlSchema getSchema() {
		XmlSchemaCollection schemaCollection = new XmlSchemaCollection();
		XmlSchema schema = null;
		schema = new XmlSchema(getNamespaceURI(), schemaCollection);
		schema.setId(getSystemId());
		//schema.setElementFormDefault(XmlSchemaForm.QUALIFIED);
		schema.setElementFormDefault(new XmlSchemaForm(XmlSchemaForm.QUALIFIED));
		
		XmlSchemaComplexType lookupType = new XmlSchemaComplexType(schema/*, true*/);
		schema.getItems().add(lookupType);
		lookupType.setName(LOOKUP_TYPE);
		XmlSchemaSimpleContent lookupContent = new XmlSchemaSimpleContent();
		XmlSchemaSimpleContentExtension lookupExtension = new XmlSchemaSimpleContentExtension();
		lookupExtension.setBaseTypeName(org.apache.ws.commons.schema.constants.Constants.XSD_STRING);
		XmlSchemaAttribute lookupId = new XmlSchemaAttribute(/*schema, false*/);
		lookupId.setName(LOOKUP_ID);
		lookupId.setSchemaTypeName(org.apache.ws.commons.schema.constants.Constants.XSD_LONG);
		lookupExtension.getAttributes().add(lookupId);
		XmlSchemaAttribute lookupTypeName = new XmlSchemaAttribute(/*schema, false*/);
		lookupTypeName.setName(LOOKUP_TYPE_NAME);
		lookupTypeName.setSchemaTypeName(org.apache.ws.commons.schema.constants.Constants.XSD_STRING);
		lookupExtension.getAttributes().add(lookupTypeName);
		lookupContent.setContent(lookupExtension);
		lookupType.setContentModel(lookupContent);
		
		XmlSchemaComplexType referenceType = new XmlSchemaComplexType(schema/*, true*/);
		schema.getItems().add(referenceType);
		referenceType.setName(REFERENCE_TYPE);
		XmlSchemaSimpleContent referenceContent = new XmlSchemaSimpleContent();
		XmlSchemaSimpleContentExtension referenceExtension = new XmlSchemaSimpleContentExtension();
		referenceExtension.setBaseTypeName(org.apache.ws.commons.schema.constants.Constants.XSD_STRING);
		XmlSchemaAttribute referenceId = new XmlSchemaAttribute(/*schema, false*/);
		referenceId.setName(REFERENCE_ID);
		referenceId.setSchemaTypeName(org.apache.ws.commons.schema.constants.Constants.XSD_LONG);
		referenceExtension.getAttributes().add(referenceId);
		referenceContent.setContent(referenceExtension);
		referenceType.setContentModel(referenceContent);
		return schema;
	}

	@Override
	public boolean updateSchema(XmlSchema schema) {
		return false;
	}

	@Override
	public Iterable<LookupType> getTypes(Class<?> cls) {
		return Collections.emptyList();
	}

	@Override
	public QName getTypeQName(Object type) {
		if(IdAndDescription.class.equals(type))
			return new QName(getNamespaceURI(), REFERENCE_TYPE);
		else if(LookupValue.class.equals(type))
			return new QName(getNamespaceURI(), LOOKUP_TYPE);
		else
			return null;
	}

	@Override
	public Object getType(QName qname) {
		if(getNamespaceURI().equals(qname.getNamespaceURI()) && REFERENCE_TYPE.equals(qname.getLocalPart()))
			return IdAndDescription.class;
		else if(getNamespaceURI().equals(qname.getNamespaceURI()) && LOOKUP_TYPE.equals(qname.getLocalPart()))
			return LookupValue.class;
		else
			return null;
	}
	
	@Override
	public boolean serializeValue(Node xml, Object entry) {
		boolean serialized = false;
		if(entry instanceof IdAndDescription && !(entry instanceof LookupValue)) {
			IdAndDescription value = (IdAndDescription)entry;
			if(xml instanceof Element && value.getId()!=null)
				((Element)xml).setAttribute(REFERENCE_ID, value.getId().toString());
			xml.setTextContent(value.getDescription());
			serialized = true;
		}	 
		return serialized;
	}
	
	@Override	
	public IdAndDescription deserializeValue(Node xml, Object type) {
		IdAndDescription value = null;
		if(IdAndDescription.class.equals(type)) {
			Long id = null;
			if(xml instanceof Element) {
				Element element = (Element)xml;
				String idValue = element.getAttribute(REFERENCE_ID);
				if(idValue != null)
					id = Long.parseLong(idValue);
			}
			value = new IdAndDescription(id, xml.getTextContent());
		}
		return value;
	}
}
