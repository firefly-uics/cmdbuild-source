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
import org.apache.ws.commons.schema.XmlSchemaComplexContent;
import org.apache.ws.commons.schema.XmlSchemaComplexContentExtension;
import org.apache.ws.commons.schema.XmlSchemaComplexType;
import org.apache.ws.commons.schema.XmlSchemaContent;
import org.apache.ws.commons.schema.XmlSchemaContentModel;
import org.apache.ws.commons.schema.XmlSchemaElement;
import org.apache.ws.commons.schema.XmlSchemaForm;
import org.apache.ws.commons.schema.XmlSchemaImport;
import org.apache.ws.commons.schema.XmlSchemaObject;
import org.apache.ws.commons.schema.XmlSchemaParticle;
import org.apache.ws.commons.schema.XmlSchemaSequence;
import org.apache.ws.commons.schema.XmlSchemaType;
import org.cmdbuild.config.CmdbfConfiguration;
import org.cmdbuild.dao.entry.CMCard;
import org.cmdbuild.dao.entrytype.CMAttribute;
import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.dao.entrytype.CMEntryType;
import org.cmdbuild.logic.data.DataDefinitionLogic;
import org.cmdbuild.logic.data.access.DataAccessLogic;
import org.cmdbuild.logic.data.lookup.LookupLogic;
import org.cmdbuild.model.data.Card;
import org.cmdbuild.model.data.EntryType;
import org.cmdbuild.model.data.EntryType.ClassBuilder;
import org.cmdbuild.workflow.CMProcessClass;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

public class ClassNamespace extends EntryNamespace {
	
	
	private static final String CLASS_DESCRIPTION = "description";
	private static final String CLASS_ACTIVE = "active";
	private static final String CLASS_SUPERCLASS = "superclass";
	private static final String CLASS_TYPE = "type";
	private static final String CLASS_PROCESS = "process";
	private static final String CLASS_STOPPABLE = "stoppable";
	
	public ClassNamespace(String name, DataAccessLogic systemdataAccessLogic, DataAccessLogic userDataAccessLogic, DataDefinitionLogic dataDefinitionLogic, LookupLogic lookupLogic, CmdbfConfiguration cmdbfConfiguration) {	
		super(name, systemdataAccessLogic, userDataAccessLogic, dataDefinitionLogic, lookupLogic, cmdbfConfiguration);
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
			schema.setElementFormDefault(new XmlSchemaForm(XmlSchemaForm.QUALIFIED));
			Set<String> imports = new HashSet<String>();
		
			for(CMClass cmClass : getTypes(CMClass.class)) {
				while(cmClass != null){
					XmlSchemaType type = schema.getTypeByName(cmClass.getIdentifier().getLocalName());
					if(type == null) {
						type = getXsd(cmClass, document, schema, imports);
						schema.addType(type);
						XmlSchemaElement element = new XmlSchemaElement();
						element.setType(type);
						element.setName(type.getName());
						schema.getItems().add(element);
					}
					cmClass = cmClass.getParent();
				}
			}
			for(String namespace : imports) {
				XmlSchemaImport schemaImport = new XmlSchemaImport();
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
			@SuppressWarnings("unchecked")
			Iterator<XmlSchemaElement> elementIterator = schema.getElements().getValues();
			while(elementIterator.hasNext()) {
				XmlSchemaElement element = elementIterator.next();
				classFromXsd(element, schema);							    				
			}
			updated = true;
		}
		return updated;
	}
	
	@Override
	public Iterable<? extends CMClass> getTypes(Class<?> cls) {
		if(CMClass.class.isAssignableFrom(cls))
			return Iterables.filter(systemDataAccessLogic.findActiveClasses(), new Predicate<CMClass>(){
				public boolean apply(CMClass input) {
					return !input.isSystem();
				}
			});
		else
			return Collections.emptyList();
	}
	
	@Override
	public QName getTypeQName(Object type) {
		QName qname = null;
		if (type instanceof CMClass) {
			CMEntryType entryType = (CMEntryType)type;
			qname = new QName(getNamespaceURI(), entryType.getIdentifier().getLocalName(), getNamespacePrefix());
		}
		return qname;
	}
	
	@Override
	public CMClass getType(final QName qname) {
		if(getNamespaceURI().equals(qname.getNamespaceURI()))
			return Iterables.tryFind(userDataAccessLogic.findActiveClasses(), new Predicate<CMClass>(){
				public boolean apply(CMClass input) {
					return input.getIdentifier().getLocalName().equals(qname.getLocalPart());
				}
			}).orNull();
		else
			return null;
	}
	
	@Override
	public boolean serialize(Node xml, Object entry) {
		boolean serialized = false;
		if(entry instanceof CMCard) {
			CMCard card = (CMCard)entry;
			serialized = serialize(xml, card.getType(), card.getValues());
		}	
		return serialized;
	}
	
	@Override	
	public Card deserialize(Node xml) {
		Card value = null;
		CMEntryType type = getType(new QName(xml.getNamespaceURI(), xml.getLocalName()));
		if(type != null) {
			Card.CardBuilder builder = Card.newInstance().withClassName(type.getIdentifier().getLocalName());
			builder.withAllAttributes(deserialize(xml, type));
			value = builder.build();
		}
		return value;
	}	
	
	private XmlSchemaType getXsd(CMClass cmClass, Document document, XmlSchema schema, Set<String> imports) {
		XmlSchemaComplexType type = new XmlSchemaComplexType(schema);
		type.setName(cmClass.getIdentifier().getLocalName());
		
		Map<String, String> properties = new HashMap<String, String>();
		properties.put(CLASS_DESCRIPTION, cmClass.getDescription());
		properties.put(CLASS_ACTIVE, Boolean.toString(cmClass.isActive()));
		properties.put(CLASS_SUPERCLASS, Boolean.toString(cmClass.isSuperclass()));
		if(cmClass instanceof CMProcessClass) {
			properties.put(CLASS_PROCESS, Boolean.toString(true));
			properties.put(CLASS_STOPPABLE, Boolean.toString(((CMProcessClass)cmClass).isUserStoppable()));
		}
		properties.put(CLASS_TYPE, cmClass.getParent()==null ? EntryType.TableType.simpletable.name() : EntryType.TableType.standard.name());
		setAnnotations(type, properties, document);
		
		XmlSchemaSequence sequence = new XmlSchemaSequence();
		for(CMAttribute attribute : cmClass.getAttributes()){
			if(attribute.isActive() && !attribute.isInherited())
				sequence.getItems().add(getXsd(attribute, document, schema, imports));
		}
		if(cmClass.getParent() != null) {
			XmlSchemaComplexContent content = new XmlSchemaComplexContent();
			XmlSchemaComplexContentExtension extension = new XmlSchemaComplexContentExtension();
			QName baseTypeName = getRegistry().getTypeQName(cmClass.getParent());
			extension.setBaseTypeName(baseTypeName);
			extension.setParticle(sequence);
			content.setContent(extension);
			type.setContentModel(content);
		}
		else
			type.setParticle(sequence);
		return type;
	}
	
	private CMClass classFromXsd(XmlSchemaObject schemaObject, XmlSchema schema){
		XmlSchemaType type = null;
		if(schemaObject instanceof XmlSchemaType)
			type = (XmlSchemaType)schemaObject;
		else if(schemaObject instanceof XmlSchemaElement) {
			XmlSchemaElement element = (XmlSchemaElement)schemaObject;
			type = element.getSchemaType();
		}
		CMClass cmClass = null;
		if(type != null) {
			Map<String, String> properties = getAnnotations(type);
			if(type instanceof XmlSchemaComplexType) {
				String parent = null;
				XmlSchemaParticle particle = ((XmlSchemaComplexType)type).getParticle();
				if(particle == null) {
					XmlSchemaContentModel contentModel =  ((XmlSchemaComplexType)type).getContentModel();
					if(contentModel != null){
						XmlSchemaContent content = contentModel.getContent();
						if(content!=null && content instanceof XmlSchemaComplexContentExtension) {
							XmlSchemaComplexContentExtension extension = (XmlSchemaComplexContentExtension)content;
							particle = extension.getParticle();							
							QName baseType = extension.getBaseTypeName();
							if(baseType != null && getNamespaceURI().equals(baseType.getNamespaceURI()))
								parent = baseType.getLocalPart();
						}
					}
				}
				CMClass parentClass = null;
				if(parent != null) {
					parentClass = userDataAccessLogic.findClass(parent);
					if(parentClass == null) {
						XmlSchemaType parentType = schema.getTypeByName(parent);
						@SuppressWarnings("unchecked")
						Iterator<XmlSchemaElement> iterator = schema.getElements().getValues();
						while(parentType==null && iterator.hasNext()) {
							XmlSchemaElement element = iterator.next();
							if(element.getSchemaType().getName().equals(parent))
								parentType = element.getSchemaType();
						}
						if(parentType != null)
							parentClass = classFromXsd(parentType, schema);
					}
				}
				ClassBuilder classBuilder = EntryType.newClass().withName(type.getName());
				if(parentClass != null)
					classBuilder.withParent(parentClass.getId());
				if(properties.containsKey(CLASS_DESCRIPTION))
					classBuilder.withDescription(properties.get(CLASS_DESCRIPTION));
				if(properties.containsKey(CLASS_SUPERCLASS))
					classBuilder.thatIsSuperClass(Boolean.parseBoolean(properties.get(CLASS_SUPERCLASS)));
				if(properties.containsKey(CLASS_PROCESS))
					classBuilder.thatIsProcess(Boolean.parseBoolean(properties.get(CLASS_PROCESS)));
				if(properties.containsKey(CLASS_STOPPABLE))
					classBuilder.thatIsUserStoppable(Boolean.parseBoolean(properties.get(CLASS_STOPPABLE)));
				if(properties.containsKey(CLASS_TYPE))
					classBuilder.withTableType(Enum.valueOf(EntryType.TableType.class, properties.get(CLASS_TYPE)));				
				if(properties.containsKey(CLASS_ACTIVE))
					classBuilder.thatIsActive(Boolean.parseBoolean(properties.get(CLASS_ACTIVE)));
				else
					classBuilder.thatIsActive(true);
				cmClass = dataDefinitionLogic.createOrUpdate(classBuilder.build());
				if(particle!=null && particle instanceof XmlSchemaSequence) {
					XmlSchemaSequence sequence = (XmlSchemaSequence)particle;
					for(int i=0; i<sequence.getItems().getCount(); i++) {
						XmlSchemaObject schemaItem = sequence.getItems().getItem(i);
						if(schemaItem instanceof XmlSchemaElement) {
							XmlSchemaElement element = (XmlSchemaElement)schemaItem;
							addAttributeFromXsd(element, schema, cmClass);
						}
					}
				}
			}
		}
		return cmClass;		
	}
}
