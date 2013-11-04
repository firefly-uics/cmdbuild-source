package org.cmdbuild.cmdbf.xml;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.apache.ws.commons.schema.XmlSchema;
import org.apache.ws.commons.schema.XmlSchemaCollection;
import org.apache.ws.commons.schema.XmlSchemaComplexType;
import org.apache.ws.commons.schema.XmlSchemaElement;
import org.apache.ws.commons.schema.XmlSchemaEnumerationFacet;
import org.apache.ws.commons.schema.XmlSchemaForm;
import org.apache.ws.commons.schema.XmlSchemaSequence;
import org.apache.ws.commons.schema.XmlSchemaSimpleType;
import org.apache.ws.commons.schema.XmlSchemaSimpleTypeRestriction;
import org.apache.ws.commons.schema.XmlSchemaType;
import org.cmdbuild.config.CmdbfConfiguration;
import org.cmdbuild.data.store.lookup.Lookup;
import org.cmdbuild.data.store.lookup.LookupType;
import org.cmdbuild.dms.DmsConfiguration;
import org.cmdbuild.dms.DocumentTypeDefinition;
import org.cmdbuild.dms.Metadata;
import org.cmdbuild.dms.MetadataDefinition;
import org.cmdbuild.dms.MetadataGroup;
import org.cmdbuild.dms.MetadataGroupDefinition;
import org.cmdbuild.dms.MetadataType;
import org.cmdbuild.logger.Log;
import org.cmdbuild.logic.DmsLogic;
import org.cmdbuild.logic.data.lookup.LookupLogic;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

public class DocumentNamespace extends AbstractNamespace {

	public static final String DOCUMENT_NAME = "name";
	public static final String DOCUMENT_DESCRIPTION = "description";
	public static final String DOCUMENT_CONTENT = "content";
	
	private DmsLogic dmsLogic;
	private LookupLogic lookupLogic;
	private DmsConfiguration dmsConfiguration;
							
	
	public DocumentNamespace(String name, DmsLogic dmsLogic, LookupLogic lookupLogic, CmdbfConfiguration cmdbfConfiguration, DmsConfiguration dmsConfiguration) {
		super(name, cmdbfConfiguration);
		this.dmsLogic = dmsLogic;
		this.lookupLogic = lookupLogic;
		this.dmsConfiguration = dmsConfiguration;
	}
	
	@Override	
	public boolean isEnabled() {
		return dmsConfiguration.isEnabled();
	}
	
	@Override
	public XmlSchema getSchema() {
		DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
		documentBuilderFactory.setNamespaceAware(true);	
		XmlSchemaCollection schemaCollection = new XmlSchemaCollection();
		XmlSchema schema = null;
		
		schema = new XmlSchema(getNamespaceURI(), schemaCollection);
		schema.setId(getSystemId());
		schema.setElementFormDefault(new XmlSchemaForm(XmlSchemaForm.QUALIFIED));		
		
		if(dmsConfiguration.isEnabled()) {
			for(DocumentTypeDefinition documentTypeDefinition : getTypes(DocumentTypeDefinition.class)){
				XmlSchemaType type = getXsd(documentTypeDefinition, schema);
				schema.addType(type);
				XmlSchemaElement element = new XmlSchemaElement();
				element.setType(type);
				element.setName(type.getName());
				schema.getItems().add(element);
			}
		}		
		return schema;
	}

	@Override
	public boolean updateSchema(XmlSchema schema) {
		return false;
	}
	
	@Override
	public Iterable<DocumentTypeDefinition> getTypes(Class<?> cls) {
		if(DocumentTypeDefinition.class.isAssignableFrom(cls))
			return Iterables.transform(lookupLogic.getAllLookup(getLookupType(dmsLogic.getCategoryLookupType()), true), new Function<Lookup, DocumentTypeDefinition>(){
				public DocumentTypeDefinition apply(Lookup input) {
					return dmsLogic.getCategoryDefinition(input.description);
				}
			});
		else
			return Collections.emptyList();
	}
	
	@Override
	public QName getTypeQName(Object type) {
		if (type instanceof DocumentTypeDefinition)
			return new QName(getNamespaceURI(), ((DocumentTypeDefinition) type).getName(), getNamespacePrefix());
		else
			return null;
	}
	
	@Override
	public DocumentTypeDefinition getType(QName qname) {
		if(getNamespaceURI().equals(qname.getNamespaceURI()))
			return dmsLogic.getCategoryDefinition(qname.getLocalPart());
		else
			return null;
	}
	
	@Override
	public boolean serialize(Node xml, Object entry) {
		boolean serialized = false;
		if(entry instanceof DmsDocument) {
			DmsDocument document = (DmsDocument)entry;
			DocumentTypeDefinition documentTypeDefinition = dmsLogic.getCategoryDefinition(document.getCategory());
			QName qName = getTypeQName(documentTypeDefinition);
			Element xmlElement = xml.getOwnerDocument().createElementNS(qName.getNamespaceURI(), getNamespacePrefix() + ":" + qName.getLocalPart());
			
			Element nameProperty = xml.getOwnerDocument().createElementNS(getNamespaceURI(), getNamespacePrefix() + ":" + DOCUMENT_NAME);				
			nameProperty.setTextContent(document.getName());
			xmlElement.appendChild(nameProperty);
			
			Element descriptionProperty = xml.getOwnerDocument().createElementNS(getNamespaceURI(), getNamespacePrefix() + ":" + DOCUMENT_DESCRIPTION);				
			descriptionProperty.setTextContent(document.getDescription());
			xmlElement.appendChild(descriptionProperty);
	
			try {
				String content = new String(Base64.encodeBase64(IOUtils.toByteArray(document.getInputStream())), "ASCII");
				Element contentProperty = xml.getOwnerDocument().createElementNS(getNamespaceURI(), getNamespacePrefix() + ":" + DOCUMENT_CONTENT);				
				contentProperty.setTextContent(content);
				xmlElement.appendChild(contentProperty);
			} catch (UnsupportedEncodingException e) {
				Log.CMDBUILD.error("DocumentNamespace getXml", e);
			} catch (IOException e) {
				Log.CMDBUILD.error("DocumentNamespace getXml", e);
			}			
			
			for(MetadataGroup group : document.getMetadataGroups()) {
				for(Metadata metadata : group.getMetadata()) {
					String value = metadata.getValue();
					if(value != null) {
						Element property = xml.getOwnerDocument().createElementNS(getNamespaceURI(), getNamespacePrefix() + ":" + metadata.getName());	
						property.setTextContent(metadata.getValue());
						xmlElement.appendChild(property);
					}
				}
			}
			
			xml.appendChild(xmlElement);
			serialized = true;
		}	
		return serialized;
	}
	
	@Override	
	public DmsDocument deserialize(Node xml) {
		DmsDocument value = null;
		DocumentTypeDefinition type = getType(new QName(xml.getNamespaceURI(), xml.getLocalName()));
		if(type != null) {
			Map<String, String> properties = new HashMap<String, String>();
			for (int i = 0; i < xml.getChildNodes().getLength(); i++) {
				Node item = xml.getChildNodes().item(i);
				if (item instanceof Element) {
					Element child = (Element) item;
					String name = child.getLocalName();
					if (name == null)
						name = child.getTagName();
					properties.put(name, child.getTextContent());
				}
			}
			Map<String, Map<String, String>> autoCompletionRules = dmsLogic.getAutoCompletionRulesByClass(type.getName());
			for(String group : autoCompletionRules.keySet())
				properties.putAll(autoCompletionRules.get(group));
			
			Map<String, MetadataGroup> metadataGroups = new HashMap<String, MetadataGroup>();
			for(String key : properties.keySet()) {
				DmsMetadataGroup group = null;
				for(MetadataGroupDefinition groupDefinition : type.getMetadataGroupDefinitions()) {
					for(MetadataDefinition metadataDefinition : groupDefinition.getMetadataDefinitions()) {
						if(metadataDefinition.getName().equals(key)) {
							group = (DmsMetadataGroup)metadataGroups.get(groupDefinition.getName());
							if(group == null) {
								group = new DmsMetadataGroup(groupDefinition.getName());
								metadataGroups.put(group.getName(), group);
							}
						}					
					}
					if(group != null && !group.containsKey(key)) {
						DmsMetadata metadata = new DmsMetadata(key, properties.get(key));
						group.put(key, metadata);
					}
				}
			}
			
			value = new DmsDocument();
			value.setCategory(type.getName());
			value.setName(properties.get(DOCUMENT_NAME));
	    	value.setDescription(properties.get(DOCUMENT_DESCRIPTION));
	    	value.setMetadataGroups(metadataGroups.values());
			if(properties.containsKey(DOCUMENT_CONTENT)) {
				String content = properties.get(DOCUMENT_CONTENT);
				try {
					value.setInputStream(new ByteArrayInputStream(Base64.decodeBase64(content.getBytes("ASCII"))));
				} catch (UnsupportedEncodingException e) {
					throw new Error(e);
				}		    					
			}					
		}
		return value;
	}
	
	private XmlSchemaType getXsd(DocumentTypeDefinition documentType, XmlSchema schema) {
		XmlSchemaComplexType type = new XmlSchemaComplexType(schema);
		type.setName(documentType.getName());		
		XmlSchemaSequence sequence = new XmlSchemaSequence();
		
		XmlSchemaElement nameElement = new XmlSchemaElement();
		nameElement.setName(DOCUMENT_NAME);
		nameElement.setSchemaTypeName(org.apache.ws.commons.schema.constants.Constants.XSD_STRING);		
		sequence.getItems().add(nameElement);
		
		XmlSchemaElement descriptionElement = new XmlSchemaElement();
		descriptionElement.setName(DOCUMENT_DESCRIPTION);
		descriptionElement.setSchemaTypeName(org.apache.ws.commons.schema.constants.Constants.XSD_STRING);
		sequence.getItems().add(descriptionElement);
				
		XmlSchemaElement contentElement = new XmlSchemaElement();
		contentElement.setName(DOCUMENT_CONTENT);
		contentElement.setSchemaTypeName(org.apache.ws.commons.schema.constants.Constants.XSD_BASE64);
		sequence.getItems().add(contentElement);
		
		for(MetadataGroupDefinition metadataGroup : documentType.getMetadataGroupDefinitions()){
			for(MetadataDefinition metadata : metadataGroup.getMetadataDefinitions())
				sequence.getItems().add(getXsd(metadata, schema));
		}
		
		type.setParticle(sequence);
		return type;
	}
	
	private XmlSchemaElement getXsd(MetadataDefinition metadata, XmlSchema schema) {
		XmlSchemaElement element = new XmlSchemaElement();
		element.setName(metadata.getName());
		
		if(metadata.isMandatory())
			element.setMinOccurs(1);
		else
			element.setMinOccurs(0);
		element.setMaxOccurs(1);
		
		if(metadata.getType() == MetadataType.BOOLEAN)
			element.setSchemaTypeName(org.apache.ws.commons.schema.constants.Constants.XSD_BOOLEAN);
		else if(metadata.getType() == MetadataType.INTEGER)
			element.setSchemaTypeName(org.apache.ws.commons.schema.constants.Constants.XSD_INTEGER);
		else if(metadata.getType() == MetadataType.FLOAT)
			element.setSchemaTypeName(org.apache.ws.commons.schema.constants.Constants.XSD_FLOAT);
		else if(metadata.getType() == MetadataType.DATE)
			element.setSchemaTypeName(org.apache.ws.commons.schema.constants.Constants.XSD_DATE);
		else if(metadata.getType() == MetadataType.DATETIME)
			element.setSchemaTypeName(org.apache.ws.commons.schema.constants.Constants.XSD_DATETIME);
		else if(metadata.getType() == MetadataType.TEXT)
			element.setSchemaTypeName(org.apache.ws.commons.schema.constants.Constants.XSD_STRING);
		else if(metadata.getType() == MetadataType.LIST) {
			XmlSchemaSimpleType type = new XmlSchemaSimpleType(schema);
			XmlSchemaSimpleTypeRestriction restriction = new XmlSchemaSimpleTypeRestriction();
			restriction.setBaseTypeName(org.apache.ws.commons.schema.constants.Constants.XSD_STRING);
			for(String value : metadata.getListValues()) {
				XmlSchemaEnumerationFacet facet = new XmlSchemaEnumerationFacet();
				facet.setValue(value);
				restriction.getFacets().add(facet);
			}			
			type.setContent(restriction);
			element.setSchemaType(type);
		}
		else
			element.setSchemaTypeName(org.apache.ws.commons.schema.constants.Constants.XSD_STRING);
		return element;
	}	
	
	private LookupType getLookupType(final String type){
		return Iterables.find(lookupLogic.getAllTypes(), new Predicate<LookupType>(){
			public boolean apply(LookupType input) {
				return input.name.equals(type);
			}
		});
	}
}
