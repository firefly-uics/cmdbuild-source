package org.cmdbuild.cmdbf.xml;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.ws.commons.schema.XmlSchema;
import org.apache.ws.commons.schema.XmlSchemaCollection;
import org.apache.ws.commons.schema.XmlSchemaComplexType;
import org.apache.ws.commons.schema.XmlSchemaContent;
import org.apache.ws.commons.schema.XmlSchemaContentModel;
import org.apache.ws.commons.schema.XmlSchemaElement;
import org.apache.ws.commons.schema.XmlSchemaEnumerationFacet;
import org.apache.ws.commons.schema.XmlSchemaFacet;
import org.apache.ws.commons.schema.XmlSchemaForm;
import org.apache.ws.commons.schema.XmlSchemaImport;
import org.apache.ws.commons.schema.XmlSchemaObject;
import org.apache.ws.commons.schema.XmlSchemaSimpleContent;
import org.apache.ws.commons.schema.XmlSchemaSimpleContentRestriction;
import org.apache.ws.commons.schema.XmlSchemaType;
import org.cmdbuild.config.CmdbfConfiguration;
import org.cmdbuild.dao.entry.LookupValue;
import org.cmdbuild.data.store.lookup.Lookup;
import org.cmdbuild.data.store.lookup.Lookup.LookupBuilder;
import org.cmdbuild.data.store.lookup.LookupType;
import org.cmdbuild.data.store.lookup.LookupType.LookupTypeBuilder;
import org.cmdbuild.exception.NotFoundException;
import org.cmdbuild.logic.data.lookup.LookupLogic;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

public class LookupNamespace extends AbstractNamespace {

	private static final String LOOKUP_NAME = "name";	
	private static final String LOOKUP_PARENT = "parent";
	private static final String LOOKUP_PARENTNAME = "parentName";
	private static final String LOOKUP_PARENTID = "parentId";
	private static final String LOOKUP_CODE = "code";
	private static final String LOOKUP_NOTES = "notes";
	private static final String LOOKUP_DEFAULT = "default";
	
	private final LookupLogic lookupLogic;
	
	public LookupNamespace(String name, LookupLogic lookupLogic, CmdbfConfiguration cmdbfConfiguration) {
		super(name, cmdbfConfiguration);
		this.lookupLogic = lookupLogic;
	}

	@Override
	public QName getTypeQName(Object type) {
		if (type instanceof LookupType)
			return new QName(getNamespaceURI(), ((LookupType) type).name.replace(" ", "-"), getNamespacePrefix());
		else
			return null;
	}

	@Override
	public LookupType getType(final QName qname) {
		if(getNamespaceURI().equals(qname.getNamespaceURI())) {
			
			return Iterables.tryFind(getTypes(LookupType.class), new Predicate<LookupType>(){
				public boolean apply(LookupType input) {
					return getTypeQName(input).equals(qname);
				}
			}).orNull();
			
		}
		else
			return null;
	}

	@Override
	public XmlSchema getSchema() {
		try {
			DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
			documentBuilderFactory.setNamespaceAware(true);	
			Document document = documentBuilderFactory.newDocumentBuilder().newDocument();		
			XmlSchemaCollection schemaCollection = new XmlSchemaCollection();
			XmlSchema schema = null;
			schema = new XmlSchema(getNamespaceURI(), schemaCollection);
			schema.setId(getSystemId());
			//schema.setElementFormDefault(XmlSchemaForm.QUALIFIED);
			schema.setElementFormDefault(new XmlSchemaForm(XmlSchemaForm.QUALIFIED));
			Set<String> imports = new HashSet<String>();
			
			for(LookupType lookupType : getTypes(LookupType.class))
				getXsd(lookupType, document, schema, imports);
			
			for(String namespace : imports) {
				XmlSchemaImport schemaImport = new XmlSchemaImport(/*schema*/);
				schemaImport.setNamespace(namespace);
				schemaImport.setSchemaLocation(getRegistry().getByNamespaceURI(namespace).getSchemaLocation());
				schema.getItems().add(schemaImport);
			}			
			return schema;
		} catch (ParserConfigurationException e) {
			throw new Error(e);
		}
	}

	@Override
	public boolean updateSchema(XmlSchema schema) {
		boolean updated = false;
		if(getNamespaceURI().equals(schema.getTargetNamespace())) {
			Map<String, Long> idMap = new HashMap<String, Long>();
			//for(XmlSchemaElement element : schema.getSchemaTypes().values())
			Iterator<?> iterator = schema.getSchemaTypes().getValues();
			while(iterator.hasNext()) {
				XmlSchemaType type = (XmlSchemaType)iterator.next();
				lookupTypeFromXsd(type, idMap, schema);
			}
			updated = true;
		}
		return updated;
	}
	
	@Override
	public Iterable<LookupType> getTypes(Class<?> cls) {
		if(LookupType.class.isAssignableFrom(cls))
			return lookupLogic.getAllTypes();
		else
			return Collections.emptyList();
	}
	
	@Override
	public boolean serializeValue(Node xml, Object entry) {
		boolean serialized = false;
		if(entry instanceof LookupValue) {
			LookupValue value = (LookupValue)entry;
			if(xml instanceof Element) {
				if(value.getId() != null)
					((Element)xml).setAttribute(SystemNamespace.LOOKUP_ID, value.getId().toString());
				if(value.getLooupType() != null)
					((Element)xml).setAttribute(SystemNamespace.LOOKUP_TYPE_NAME, value.getLooupType());
			}
			if(value.getDescription() != null)
				xml.setTextContent(value.getDescription());
			serialized = true;
		}	
		return serialized;
	}
	
	@Override	
	public LookupValue deserializeValue(Node xml, Object type) {
		LookupValue value = null;
		if(LookupValue.class.equals(type)) {
			Long id = null;
			String lookupType = null;
			if(xml instanceof Element) {
				Element element = (Element)xml;
				String idValue = element.getAttribute(SystemNamespace.LOOKUP_ID);
				if(idValue != null)
					id = Long.parseLong(idValue);
				lookupType = element.getAttribute(SystemNamespace.LOOKUP_TYPE_NAME);
			}
			value = new LookupValue(id, xml.getTextContent(), lookupType);
		}
		return value;
	}
	
	private XmlSchemaType getXsd(LookupType lookupType, Document document, XmlSchema schema, final Set<String> imports) {
		XmlSchemaComplexType type = new XmlSchemaComplexType(schema/*, true*/);
		schema.getItems().add(type);
		type.setName(getTypeQName(lookupType).getLocalPart());
		Map<String, String> properties = new HashMap<String, String>();
		properties.put(LOOKUP_NAME, lookupType.name);
		properties.put(LOOKUP_PARENT, lookupType.parent);
		setAnnotations(type, properties, document);
		XmlSchemaSimpleContent contentModel = new XmlSchemaSimpleContent();
		XmlSchemaSimpleContentRestriction restriction = new XmlSchemaSimpleContentRestriction();
		QName baseLookupQName = getRegistry().getTypeQName(LookupValue.class);
		imports.add(baseLookupQName.getNamespaceURI());
		restriction.setBaseTypeName(baseLookupQName);
		for(Lookup lookup : lookupLogic.getAllLookup(lookupType, true)) {
			if(lookup.description != null && lookup.description.length() > 0) {
				XmlSchemaFacet facet = new XmlSchemaEnumerationFacet();
				facet.setValue(lookup.description);			
				Map<String, String> lookupProperties = new HashMap<String, String>();
				if(lookup.parent != null) {
					lookupProperties.put(LOOKUP_PARENTNAME, lookup.parent.description);
					lookupProperties.put(LOOKUP_PARENTID, Long.toString(lookup.parent.getId()));
				}
				lookupProperties.put(SystemNamespace.LOOKUP_ID, Long.toString(lookup.getId()));
				lookupProperties.put(LOOKUP_CODE, lookup.code);
				lookupProperties.put(LOOKUP_NOTES, lookup.notes);
				lookupProperties.put(LOOKUP_DEFAULT, Boolean.toString(lookup.isDefault));
				setAnnotations(facet, lookupProperties, document);		
				restriction.getFacets().add(facet);
			}
		}
		contentModel.setContent(restriction);
		type.setContentModel(contentModel);		
		return type;
	}
	
	private LookupType lookupTypeFromXsd(XmlSchemaObject schemaObject, Map<String, Long> idMap, XmlSchema schema) {
		XmlSchemaType type = null;
		if(schemaObject instanceof XmlSchemaType)
			type = (XmlSchemaType)schemaObject;
		else if(schemaObject instanceof XmlSchemaElement) {
			XmlSchemaElement element = (XmlSchemaElement)schemaObject;
			type = element.getSchemaType();
			if(type == null) {
				QName typeName = element.getSchemaTypeName();
				type = schema.getTypeByName(typeName);
			}
		}
		LookupType lookupType = null;
		if(type != null) {			
			if(type instanceof XmlSchemaComplexType) {
				XmlSchemaContentModel contentModel = ((XmlSchemaComplexType)type).getContentModel();
				if(contentModel!=null) {
					XmlSchemaContent content = contentModel.getContent();
					if(content != null && content instanceof XmlSchemaSimpleContentRestriction) {
						XmlSchemaSimpleContentRestriction restriction = (XmlSchemaSimpleContentRestriction)content;
						if(restriction.getBaseTypeName().equals(org.apache.ws.commons.schema.constants.Constants.XSD_STRING)) { 
							Map<String, String> properties = getAnnotations(type);
							String parent = properties.get(LOOKUP_PARENT);
							String name = properties.get(LOOKUP_NAME);
							if(name == null)
								name = type.getName();
							LookupTypeBuilder lookupTypeBuilder = LookupType.newInstance().withName(name);
							LookupType parentLookupType = null;
							if(parent != null && !parent.isEmpty()) {
								lookupTypeBuilder.withParent(parent);
								parentLookupType = getLookupType(parent);							
							}
							lookupType = lookupTypeBuilder.build();
							LookupType oldLookupType = getLookupType(lookupType.name);						
							lookupLogic.saveLookupType(lookupType, oldLookupType);
							//for(XmlSchemaFacet facet : restriction.getFacets()) {
							for(int i=0; i<restriction.getFacets().getCount(); i++) {
								XmlSchemaObject facet = restriction.getFacets().getItem(i);
								if(facet instanceof XmlSchemaEnumerationFacet) {
									XmlSchemaEnumerationFacet enumeration = (XmlSchemaEnumerationFacet)facet;
									String value = (String)enumeration.getValue();
									Map<String, String> lookupProperties = getAnnotations(enumeration);
									String parentId = lookupProperties.get(LOOKUP_PARENTID);
									String parentName = lookupProperties.get(LOOKUP_PARENTNAME);
									Lookup lookupParent = getLookup(parentLookupType, parentId, parentName, null, idMap);									
									String lookupId = lookupProperties.get(SystemNamespace.LOOKUP_ID);
									Lookup oldLookup = getLookup(lookupType, lookupId, value, lookupParent, idMap);
									LookupBuilder lookupBuilder = Lookup.newInstance().withType(lookupType);
									if(oldLookup != null)
										lookupBuilder.withId(oldLookup.getId());
									lookupBuilder.withActiveStatus(true);
									lookupBuilder.withDescription(value).build();
									if(lookupParent != null)
										lookupBuilder.withParent(lookupParent);
									String isDefault = lookupProperties.get(LOOKUP_DEFAULT);
									if(isDefault != null)
										lookupBuilder.withDefaultStatus(Boolean.parseBoolean(isDefault));
									lookupBuilder.withCode(lookupProperties.get(LOOKUP_CODE));
									lookupBuilder.withNotes(lookupProperties.get(LOOKUP_NOTES));
									Long newId = lookupLogic.createOrUpdateLookup(lookupBuilder.build());
									idMap.put(lookupId, newId);
								}
							}
						}
					}
				}
			}
		}
		return lookupType;
	}
	
	private LookupType getLookupType(final String name){
		return Iterables.find(lookupLogic.getAllTypes(), new Predicate<LookupType>(){
			public boolean apply(LookupType input) {
				return input.name.equals(name);
			}
		});
	}
	
	private Lookup getLookup(LookupType type, String id, final String name, final Lookup parent, Map<String, Long> idMap){
		Lookup lookup = null;
		if(id != null && !id.isEmpty()) {
			Long lookupId = idMap!=null ? idMap.get(id) : null;
			if(lookupId == null)
				lookupId = new Long(id);
			try {
				lookup = lookupLogic.getLookup(lookupId);
			}
			catch(NotFoundException e){}
		}
		if(lookup==null && type!=null && name!=null)
			lookup = Iterables.find(lookupLogic.getAllLookup(type, false), new Predicate<Lookup>(){
			public boolean apply(Lookup input) {
				return input.description.equals(name) && (parent==null || input.parent.equals(parent));
			}
		});
		return lookup;
	}
}
