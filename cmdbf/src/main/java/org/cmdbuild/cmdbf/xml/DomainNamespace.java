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
import org.apache.ws.commons.schema.XmlSchemaElement;
import org.apache.ws.commons.schema.XmlSchemaForm;
import org.apache.ws.commons.schema.XmlSchemaImport;
import org.apache.ws.commons.schema.XmlSchemaObject;
import org.apache.ws.commons.schema.XmlSchemaParticle;
import org.apache.ws.commons.schema.XmlSchemaSequence;
import org.apache.ws.commons.schema.XmlSchemaType;
import org.cmdbuild.config.CmdbfConfiguration;
import org.cmdbuild.dao.entry.CMRelation;
import org.cmdbuild.dao.entrytype.CMAttribute;
import org.cmdbuild.dao.entrytype.CMDomain;
import org.cmdbuild.dao.entrytype.CMEntryType;
import org.cmdbuild.logic.data.DataDefinitionLogic;
import org.cmdbuild.logic.data.access.DataAccessLogic;
import org.cmdbuild.logic.data.access.RelationDTO;
import org.cmdbuild.logic.data.lookup.LookupLogic;
import org.cmdbuild.model.data.Domain;
import org.cmdbuild.model.data.Domain.DomainBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

public class DomainNamespace extends EntryNamespace {

	private static final String DOMAIN_DESCRIPTION = "description";
	private static final String DOMAIN_ACTIVE = "active";
	private static final String DOMAIN_DESCRIPTION1 = "description1";
	private static final String DOMAIN_DESCRIPTION2 = "description2";
	private static final String DOMAIN_CLASS1 = "class1";
	private static final String DOMAIN_CLASS2 = "class2";
	private static final String DOMAIN_CARDINALITY = "cardinality";
	private static final String DOMAIN_MASTER_DETAIL = "masterDetail";
	private static final String DOMAIN_MASTER_DETAIL_DESCRIPTION = "masterDetailDescription";
	
	public DomainNamespace(String name, DataAccessLogic systemdataAccessLogic, DataAccessLogic userDataAccessLogic, DataDefinitionLogic dataDefinitionLogic, LookupLogic lookupLogic, CmdbfConfiguration cmdbfConfiguration) {	
		super(name, systemdataAccessLogic, userDataAccessLogic, dataDefinitionLogic, lookupLogic, cmdbfConfiguration);
	}

	@Override
	public XmlSchema getSchema(){
		try {			
			DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
			documentBuilderFactory.setNamespaceAware(true);	
			Document document = documentBuilderFactory.newDocumentBuilder().newDocument();
			XmlSchemaCollection schemaCollection = new XmlSchemaCollection();
			XmlSchema schema = null;
					
			Set<String> imports = new HashSet<String>();
			schema = new XmlSchema(getNamespaceURI(), schemaCollection);
			schema.setId(getSystemId());
			schema.setElementFormDefault(new XmlSchemaForm(XmlSchemaForm.QUALIFIED));						
			for(CMDomain domain : getTypes(CMDomain.class)) {
				XmlSchemaType type = getXsd(domain, document, schema, imports);
				schema.addType(type);
				XmlSchemaElement element = new XmlSchemaElement();
				element.setType(type);
				element.setName(type.getName());
				schema.getItems().add(element);
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
	public boolean updateSchema(XmlSchema schema){
		boolean updated = false;
		if(getNamespaceURI().equals(schema.getTargetNamespace())) {
			@SuppressWarnings("unchecked")
			Iterator<XmlSchemaElement> elementIterator = schema.getElements().getValues();
			while(elementIterator.hasNext()) {
				XmlSchemaElement element = elementIterator.next();
				domainFromXsd(element, schema);							    				
			}
			updated = true;
		}
		return updated;
	}
	
	@Override
	public Iterable<? extends CMDomain> getTypes(Class<?> cls) {
		if(CMDomain.class.isAssignableFrom(cls))
			return Iterables.filter(systemDataAccessLogic.findActiveDomains(), new Predicate<CMDomain>(){
				public boolean apply(CMDomain input) {
					return !input.isSystem();
				}
			});
		else
			return Collections.emptyList();
	}
	
	@Override
	public QName getTypeQName(Object type) {
		QName qname = null;
		if (type instanceof CMDomain) {
			CMEntryType entryType = (CMEntryType)type;
			qname = new QName(getNamespaceURI(), entryType.getIdentifier().getLocalName(), getNamespacePrefix());
		}
		return qname;
	}
	
	@Override
	public CMDomain getType(final QName qname) {
		if(getNamespaceURI().equals(qname.getNamespaceURI()))
			return Iterables.tryFind(userDataAccessLogic.findActiveDomains(), new Predicate<CMDomain>(){
				public boolean apply(CMDomain input) {
					return input.getIdentifier().getLocalName().equals(qname.getLocalPart());
				}
			}).orNull();
		else
			return null;
	}
	
	@Override
	public boolean serialize(Node xml, Object entry) {
		boolean serialized = false;
		if(entry instanceof CMRelation) {
			CMRelation relation = (CMRelation)entry;
			serialized = serialize(xml, relation.getType(), relation.getValues());
		}	
		return serialized;
	}
	
	@Override	
	public RelationDTO deserialize(Node xml) {
		RelationDTO value = null;
		CMDomain type = getType(new QName(xml.getNamespaceURI(), xml.getLocalName()));
		if(type != null) {
			value = new RelationDTO();
			value.domainName = type.getIdentifier().getLocalName();
			value.relationAttributeToValue = deserialize(xml, type);
		}
		return value;
	}
	
	private XmlSchemaType getXsd(CMDomain domain, Document document, XmlSchema schema, Set<String> imports) {
		XmlSchemaComplexType type = new XmlSchemaComplexType(schema);
		type.setName(domain.getIdentifier().getLocalName());
		
		Map<String, String> properties = new HashMap<String, String>();
		properties.put(DOMAIN_DESCRIPTION, domain.getDescription());
		properties.put(DOMAIN_ACTIVE, Boolean.toString(domain.isActive()));
		properties.put(DOMAIN_CLASS1, domain.getClass1().getIdentifier().getLocalName());
		properties.put(DOMAIN_CLASS2, domain.getClass2().getIdentifier().getLocalName());
		properties.put(DOMAIN_DESCRIPTION1, domain.getDescription1());
		properties.put(DOMAIN_DESCRIPTION2, domain.getDescription2());
		properties.put(DOMAIN_CARDINALITY, domain.getCardinality());
		properties.put(DOMAIN_MASTER_DETAIL, Boolean.toString(domain.isMasterDetail()));
		properties.put(DOMAIN_MASTER_DETAIL_DESCRIPTION, domain.getMasterDetailDescription());
		setAnnotations(type, properties, document);
		
		XmlSchemaSequence sequence = new XmlSchemaSequence();
		for(CMAttribute attribute : domain.getAttributes()){
			if(attribute.isActive() && !attribute.isInherited())
				sequence.getItems().add(getXsd(attribute, document, schema, imports));
		}
		type.setParticle(sequence);
		return type;
	}
	
	private CMDomain domainFromXsd(XmlSchemaObject schemaObject, XmlSchema schema){
		XmlSchemaType type = null;
		if(schemaObject instanceof XmlSchemaType)
			type = (XmlSchemaType)schemaObject;
		else if(schemaObject instanceof XmlSchemaElement) {
			XmlSchemaElement element = (XmlSchemaElement)schemaObject;
			type = element.getSchemaType();
		}
		CMDomain domain = null;		
		if(type != null) {
			Map<String, String> properties = getAnnotations(type);
			if(type instanceof XmlSchemaComplexType) {
				XmlSchemaParticle particle = ((XmlSchemaComplexType)type).getParticle();
				
				DomainBuilder domainBuilder = Domain.newDomain().withName(type.getName());
				if(properties.containsKey(DOMAIN_CLASS1))
					domainBuilder.withIdClass1(userDataAccessLogic.findClass(properties.get(DOMAIN_CLASS1)).getId());
				if(properties.containsKey(DOMAIN_CLASS2))
					domainBuilder.withIdClass2(userDataAccessLogic.findClass(properties.get(DOMAIN_CLASS2)).getId());
				if(properties.containsKey(DOMAIN_DESCRIPTION))
					domainBuilder.withDescription(properties.get(DOMAIN_DESCRIPTION));
				if(properties.containsKey(DOMAIN_DESCRIPTION1))
					domainBuilder.withDirectDescription(properties.get(DOMAIN_DESCRIPTION1));
				if(properties.containsKey(DOMAIN_DESCRIPTION2))
					domainBuilder.withInverseDescription(properties.get(DOMAIN_DESCRIPTION2));
				if(properties.containsKey(DOMAIN_CARDINALITY))
					domainBuilder.withCardinality(properties.get(DOMAIN_CARDINALITY));
				if(properties.containsKey(DOMAIN_MASTER_DETAIL_DESCRIPTION))
					domainBuilder.withMasterDetailDescription(properties.get(DOMAIN_MASTER_DETAIL_DESCRIPTION));
				if(properties.containsKey(DOMAIN_ACTIVE))
					domainBuilder.thatIsActive(Boolean.parseBoolean(properties.get(DOMAIN_ACTIVE)));
				else
					domainBuilder.thatIsActive(true);
				if(properties.containsKey(DOMAIN_MASTER_DETAIL))
					domainBuilder.thatIsMasterDetail(Boolean.parseBoolean(properties.get(DOMAIN_MASTER_DETAIL)));
				domain = dataDefinitionLogic.createOrUpdate(domainBuilder.build());
				
				if(particle!=null && particle instanceof XmlSchemaSequence) {
					XmlSchemaSequence sequence = (XmlSchemaSequence)particle;
					for(int i=0; i<sequence.getItems().getCount(); i++) {
						XmlSchemaObject schemaItem = sequence.getItems().getItem(i);
						if(schemaItem instanceof XmlSchemaElement) {
							XmlSchemaElement element = (XmlSchemaElement)schemaItem;
							addAttributeFromXsd(element, schema, domain);
						}
					}
				}
			}
		}
		return domain;		
	}	
}
