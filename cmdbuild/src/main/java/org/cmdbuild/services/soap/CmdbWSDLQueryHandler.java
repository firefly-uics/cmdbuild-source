package org.cmdbuild.services.soap;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.wsdl.Definition;
import javax.wsdl.extensions.schema.SchemaReference;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.cxf.Bus;
import org.apache.cxf.helpers.DOMUtils;
import org.apache.cxf.service.model.EndpointInfo;
import org.apache.cxf.transport.http.WSDLQueryHandler;
import org.apache.cxf.transports.http.QueryHandler;
import org.apache.cxf.transports.http.QueryHandlerRegistry;
import org.cmdbuild.cmdbf.ManagementDataRepository;
import org.dmtf.schemas.cmdbf._1.tns.servicemetadata.ObjectFactory;
import org.dmtf.schemas.cmdbf._1.tns.servicemetadata.PropertyValueOperatorsType;
import org.dmtf.schemas.cmdbf._1.tns.servicemetadata.QueryCapabilities;
import org.dmtf.schemas.cmdbf._1.tns.servicemetadata.QueryServiceMetadata;
import org.dmtf.schemas.cmdbf._1.tns.servicemetadata.RegistrationServiceMetadata;
import org.dmtf.schemas.cmdbf._1.tns.servicemetadata.ServiceDescription;
import org.dmtf.schemas.cmdbf._1.tns.servicemetadata.XPathType;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class CmdbWSDLQueryHandler extends WSDLQueryHandler implements ApplicationContextAware {
	
	private ApplicationContext applicationContext;
	
	public CmdbWSDLQueryHandler() {
	}

	public CmdbWSDLQueryHandler(Bus b) {
		super(b);
	}
	
	@Override
	public void setApplicationContext(final ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
	}
	
	@Override
    public void setBus(Bus bus) {
        super.setBus(bus);
        
        QueryHandlerRegistry reg = bus.getExtension(QueryHandlerRegistry.class);
        for (Iterator<QueryHandler> it = reg.getHandlers().iterator(); it.hasNext(); it.next())
        {
          if (it.next() instanceof WSDLQueryHandler) {
            reg.registerHandler(this,0);
            break;
          }
        } 
    }
	
	public ManagementDataRepository getMdr() {
		return applicationContext.getBean(ManagementDataRepository.class);
	}

	@Override
	protected void updateDoc(Document doc, String base,	Map<String, Definition> mp, Map<String, SchemaReference> smp, EndpointInfo ei) {

		super.updateDoc(doc, base, mp, smp, ei);

		if(ei.getName().getNamespaceURI().equals("http://schemas.dmtf.org/cmdbf/1/tns/query")) {
			try {
				updateQueryWsdl(doc);
			} catch (JAXBException e) {
				e.printStackTrace();
			} catch (ParserConfigurationException e) {
				e.printStackTrace();
			}
		}
		if(ei.getName().getNamespaceURI().equals("http://schemas.dmtf.org/cmdbf/1/tns/registration")) {
			try {
					updateRegistrationWsdl(doc);
				} catch (JAXBException e) {
					e.printStackTrace();
				} catch (ParserConfigurationException e) {
					e.printStackTrace();
				}
			}
	}
	
	public void updateQueryWsdl(Document doc) throws JAXBException, ParserConfigurationException{
		ObjectFactory objectFactory = new ObjectFactory();		
		updateWsdl(doc, getQueryServiceMetadata(objectFactory));
	}
	
	public void updateRegistrationWsdl(Document doc) throws JAXBException, ParserConfigurationException{
		ObjectFactory objectFactory = new ObjectFactory();		
		updateWsdl(doc, getRegistrationServiceMetadata(objectFactory));
	}
	
	private void updateWsdl(Document doc, Object metadata) throws JAXBException, ParserConfigurationException{
		final String policyId = "SupportedMetadata";
		Element definitions = null;
		List<Element> defList = DOMUtils.findAllElementsByTagNameNS(doc.getDocumentElement(), "http://schemas.xmlsoap.org/wsdl/", "definitions");
		if(!defList.isEmpty())
			definitions = defList.get(0);
		if(definitions != null) {
			Element policy = doc.createElementNS("http://www.w3.org/ns/ws-policy", "wsp:Policy");
			policy.setAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns:wsu", "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd");
			policy.setAttributeNS("http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd", "wsu:Id", policyId);
			definitions.insertBefore(policy, definitions.getFirstChild());
			
			JAXBContext context = JAXBContext.newInstance(QueryServiceMetadata.class, RegistrationServiceMetadata.class);
			Marshaller marshaller = context.createMarshaller();;
			DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
			documentBuilderFactory.setNamespaceAware(true);
		
			marshaller.marshal(metadata, policy);					
							
			List<Element> bindingList = DOMUtils.findAllElementsByTagNameNS(definitions, "http://schemas.xmlsoap.org/wsdl/", "binding");
			for(Element binding : bindingList) {
				Element policyReference = doc.createElementNS("http://www.w3.org/ns/ws-policy", "wsp:PolicyReference");
				policyReference.setAttribute("URI", "#" + policyId);
				binding.appendChild(policyReference);
			}
		}
	}
	
	private QueryServiceMetadata getQueryServiceMetadata(ObjectFactory factory){
		QueryServiceMetadata queryServiceMetadata = factory.createQueryServiceMetadata();
		queryServiceMetadata.setServiceDescription(getServiceDescription(factory));
		queryServiceMetadata.setRecordTypeList(getMdr().getRecordTypesList());
		queryServiceMetadata.setQueryCapabilities(getQueryCapabilities(factory));
		return queryServiceMetadata;
	}
	
	private RegistrationServiceMetadata getRegistrationServiceMetadata(ObjectFactory factory){
		RegistrationServiceMetadata registrationServiceMetadata = factory.createRegistrationServiceMetadata();
		registrationServiceMetadata.setServiceDescription(getServiceDescription(factory));
		registrationServiceMetadata.setRecordTypeList(getMdr().getRecordTypesList());
		return registrationServiceMetadata;
	}
	
	private ServiceDescription getServiceDescription(ObjectFactory factory) {
		ServiceDescription serviceDescription = factory.createServiceDescription();
		serviceDescription.setMdrId(getMdr().getMdrId());
		return serviceDescription;
	}
	
	private QueryCapabilities getQueryCapabilities(ObjectFactory factory){
		QueryCapabilities queryCapabilities = factory.createQueryCapabilities();
		
		org.dmtf.schemas.cmdbf._1.tns.servicemetadata.ContentSelectorType contentSelectorType = factory.createContentSelectorType();
		contentSelectorType.setPropertySelector(true);
		contentSelectorType.setRecordTypeSelector(true);
		queryCapabilities.setContentSelectorSupport(contentSelectorType);
		
		org.dmtf.schemas.cmdbf._1.tns.servicemetadata.RecordConstraintType recordConstraintType = factory.createRecordConstraintType();
		recordConstraintType.setRecordTypeConstraint(true);
		recordConstraintType.setPropertyValueConstraint(true);
		PropertyValueOperatorsType propertyValueOperatorsType = factory.createPropertyValueOperatorsType();
		propertyValueOperatorsType.setContains(true);
		propertyValueOperatorsType.setEqual(true);
		propertyValueOperatorsType.setGreater(true);
		propertyValueOperatorsType.setGreaterOrEqual(true);
		propertyValueOperatorsType.setIsNull(true);
		propertyValueOperatorsType.setLess(true);
		propertyValueOperatorsType.setLessOrEqual(true);
		propertyValueOperatorsType.setLike(true);
		recordConstraintType.setPropertyValueOperators(propertyValueOperatorsType);
		queryCapabilities.setRecordConstraintSupport(recordConstraintType);
		
		org.dmtf.schemas.cmdbf._1.tns.servicemetadata.RelationshipTemplateType relationshipTemplateType = factory.createRelationshipTemplateType();
		relationshipTemplateType.setDepthLimit(true);
		relationshipTemplateType.setMinimumMaximum(true);
		queryCapabilities.setRelationshipTemplateSupport(relationshipTemplateType);
		
		XPathType xPathType = factory.createXPathType();
		queryCapabilities.setXpathSupport(xPathType);
		return queryCapabilities;
	}
}
