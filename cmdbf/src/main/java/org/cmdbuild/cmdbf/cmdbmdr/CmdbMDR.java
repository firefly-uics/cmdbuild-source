package org.cmdbuild.cmdbf.cmdbmdr;

import static org.cmdbuild.dao.query.clause.AnyAttribute.anyAttribute;
import static org.cmdbuild.dao.query.clause.QueryAliasAttribute.attribute;
import static org.cmdbuild.dao.query.clause.join.Over.over;
import static org.cmdbuild.dao.query.clause.where.AndWhereClause.and;
import static org.cmdbuild.dao.query.clause.where.BeginsWithOperatorAndValue.beginsWith;
import static org.cmdbuild.dao.query.clause.where.ContainsOperatorAndValue.contains;
import static org.cmdbuild.dao.query.clause.where.EndsWithOperatorAndValue.endsWith;
import static org.cmdbuild.dao.query.clause.where.EqualsOperatorAndValue.eq;
import static org.cmdbuild.dao.query.clause.where.GreaterThanOperatorAndValue.gt;
import static org.cmdbuild.dao.query.clause.where.InOperatorAndValue.in;
import static org.cmdbuild.dao.query.clause.where.LessThanOperatorAndValue.lt;
import static org.cmdbuild.dao.query.clause.where.NotWhereClause.not;
import static org.cmdbuild.dao.query.clause.where.NullOperatorAndValue.isNull;
import static org.cmdbuild.dao.query.clause.where.OrWhereClause.or;
import static org.cmdbuild.dao.query.clause.where.SimpleWhereClause.condition;
import static org.cmdbuild.dao.query.clause.where.TrueWhereClause.trueWhereClause;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.activation.DataHandler;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.cmdbuild.auth.user.OperationUser;
import org.cmdbuild.cmdbf.CMDBfId;
import org.cmdbuild.cmdbf.CMDBfItem;
import org.cmdbuild.cmdbf.CMDBfQueryResult;
import org.cmdbuild.cmdbf.CMDBfRelationship;
import org.cmdbuild.cmdbf.CMDBfUtils;
import org.cmdbuild.cmdbf.ContentSelectorFunction;
import org.cmdbuild.cmdbf.ItemSet;
import org.cmdbuild.cmdbf.ManagementDataRepository;
import org.cmdbuild.cmdbf.PathSet;
import org.cmdbuild.cmdbf.xml.DmsDocument;
import org.cmdbuild.cmdbf.xml.XmlRegistry;
import org.cmdbuild.common.Constants;
import org.cmdbuild.config.CmdbfConfiguration;
import org.cmdbuild.dao.entry.CMCard;
import org.cmdbuild.dao.entry.CMEntry;
import org.cmdbuild.dao.entry.CMRelation;
import org.cmdbuild.dao.entrytype.CMAttribute;
import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.dao.entrytype.CMDomain;
import org.cmdbuild.dao.entrytype.CMEntryType;
import org.cmdbuild.dao.query.CMQueryRow;
import org.cmdbuild.dao.query.QuerySpecsBuilder;
import org.cmdbuild.dao.query.clause.QueryAliasAttribute;
import org.cmdbuild.dao.query.clause.QueryAttribute;
import org.cmdbuild.dao.query.clause.QueryDomain.Source;
import org.cmdbuild.dao.query.clause.alias.Alias;
import org.cmdbuild.dao.query.clause.alias.NameAlias;
import org.cmdbuild.dao.query.clause.where.WhereClause;
import org.cmdbuild.dms.DmsConfiguration;
import org.cmdbuild.dms.DocumentTypeDefinition;
import org.cmdbuild.dms.StoredDocument;
import org.cmdbuild.exception.CMDBException;
import org.cmdbuild.logger.Log;
import org.cmdbuild.logic.DmsLogic;
import org.cmdbuild.logic.data.access.DataAccessLogic;
import org.cmdbuild.logic.data.access.RelationDTO;
import org.cmdbuild.model.data.Card;
import org.dmtf.schemas.cmdbf._1.tns.query.ExpensiveQueryErrorFault;
import org.dmtf.schemas.cmdbf._1.tns.query.InvalidPropertyTypeFault;
import org.dmtf.schemas.cmdbf._1.tns.query.QueryErrorFault;
import org.dmtf.schemas.cmdbf._1.tns.query.UnknownTemplateIDFault;
import org.dmtf.schemas.cmdbf._1.tns.query.UnsupportedConstraintFault;
import org.dmtf.schemas.cmdbf._1.tns.query.UnsupportedSelectorFault;
import org.dmtf.schemas.cmdbf._1.tns.query.XPathErrorFault;
import org.dmtf.schemas.cmdbf._1.tns.registration.DeregistrationErrorFault;
import org.dmtf.schemas.cmdbf._1.tns.registration.InvalidMDRFault;
import org.dmtf.schemas.cmdbf._1.tns.registration.InvalidRecordFault;
import org.dmtf.schemas.cmdbf._1.tns.registration.RegistrationErrorFault;
import org.dmtf.schemas.cmdbf._1.tns.registration.UnsupportedRecordTypeFault;
import org.dmtf.schemas.cmdbf._1.tns.servicedata.AcceptedType;
import org.dmtf.schemas.cmdbf._1.tns.servicedata.ComparisonOperatorType;
import org.dmtf.schemas.cmdbf._1.tns.servicedata.ContentSelectorType;
import org.dmtf.schemas.cmdbf._1.tns.servicedata.DeclinedType;
import org.dmtf.schemas.cmdbf._1.tns.servicedata.DeregisterInstanceResponseType;
import org.dmtf.schemas.cmdbf._1.tns.servicedata.DeregisterRequestType;
import org.dmtf.schemas.cmdbf._1.tns.servicedata.DeregisterResponseType;
import org.dmtf.schemas.cmdbf._1.tns.servicedata.EqualOperatorType;
import org.dmtf.schemas.cmdbf._1.tns.servicedata.ItemType;
import org.dmtf.schemas.cmdbf._1.tns.servicedata.MdrScopedIdType;
import org.dmtf.schemas.cmdbf._1.tns.servicedata.NullOperatorType;
import org.dmtf.schemas.cmdbf._1.tns.servicedata.PropertyValueType;
import org.dmtf.schemas.cmdbf._1.tns.servicedata.QNameType;
import org.dmtf.schemas.cmdbf._1.tns.servicedata.QueryResultType;
import org.dmtf.schemas.cmdbf._1.tns.servicedata.QueryType;
import org.dmtf.schemas.cmdbf._1.tns.servicedata.RecordConstraintType;
import org.dmtf.schemas.cmdbf._1.tns.servicedata.RecordType;
import org.dmtf.schemas.cmdbf._1.tns.servicedata.RegisterInstanceResponseType;
import org.dmtf.schemas.cmdbf._1.tns.servicedata.RegisterRequestType;
import org.dmtf.schemas.cmdbf._1.tns.servicedata.RegisterResponseType;
import org.dmtf.schemas.cmdbf._1.tns.servicedata.RelationshipType;
import org.dmtf.schemas.cmdbf._1.tns.servicedata.StringOperatorType;
import org.dmtf.schemas.cmdbf._1.tns.servicedata.RecordType.RecordMetadata;
import org.dmtf.schemas.cmdbf._1.tns.servicemetadata.RecordTypeList;
import org.dmtf.schemas.cmdbf._1.tns.servicemetadata.RecordTypes;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Element;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

public class CmdbMDR implements ManagementDataRepository {
	
	private static final Alias SOURCE_ALIAS = NameAlias.as("SOURCE");
	private static final Alias TARGET_ALIAS = NameAlias.as("TARGET");
	private static final String ENTRY_RECORDID_PREFIX = "entry:";
	private static final String DOCUMENT_RECORDID_PREFIX = "doc:";
	
	private XmlRegistry xmlRegistry;
	private MdrScopedIdRegistry aliasRegistry;
	private DataAccessLogic dataAccessLogic;
	private DmsLogic dmsLogic;
	private OperationUser operationUser;
	private CmdbfConfiguration cmdbfConfiguration;
	private DmsConfiguration dmsConfiguration;
	
	private class CmdbQueryResult extends CMDBfQueryResult {
		
		private Map<String, Map<Long, Long>> typeMap;

		public CmdbQueryResult(QueryType body) throws QueryErrorFault {
			super(body);
			this.typeMap = new HashMap<String, Map<Long, Long>>();
			execute();
		}

		@Override
		protected Collection<CMDBfItem> getItems(String templateId,	Set<CMDBfId> instanceId, RecordConstraintType recordConstraint) {
			Map<Long, Long> templateTypeMap = new HashMap<Long, Long>();
			typeMap.put(templateId, templateTypeMap);
			return CmdbMDR.this.getItems(instanceId, recordConstraint, templateTypeMap);
		}

		@Override
		protected Collection<CMDBfRelationship> getRelationships(String templateId,	Set<CMDBfId> instanceId, Set<CMDBfId> source, Set<CMDBfId> target, RecordConstraintType recordConstraint) {
			Map<Long, Long> templateTypeMap = new HashMap<Long, Long>();
			typeMap.put(templateId, templateTypeMap);
			return CmdbMDR.this.getRelationships(instanceId, source, target, recordConstraint, templateTypeMap);
		}
		
		@Override
		protected void fetchItemRecords(String templateId, ItemSet<CMDBfItem> items, ContentSelectorType contentSelector) {
			Map<Long, Long> templateTypeMap = typeMap.get(templateId);
			CmdbMDR.this.fetchItemRecords(items, contentSelector, templateTypeMap);
		}

		@Override
		protected void fetchRelationshipRecords(String templateId, PathSet relationships, ContentSelectorType contentSelector) {
			Map<Long, Long> templateTypeMap = typeMap.get(templateId);
			CmdbMDR.this.fetchRelationshipRecords(relationships, contentSelector, templateTypeMap);		
		}
		
		@Override
		protected CMDBfId resolveAlias(MdrScopedIdType alias) {
			return aliasRegistry.resolveAlias(alias);
		}
		
		@Override
		protected void fetchAlias(CMDBfItem item) {
			Set<CMDBfId> idSet = new HashSet<CMDBfId>();
			for(CMDBfId id : item.instanceIds())
				idSet.addAll(aliasRegistry.getAlias(id));
			item.instanceIds().addAll(idSet);
		}
	}
	
	public CmdbMDR(XmlRegistry xmlRegistry, DataAccessLogic dataAccessLogic, DmsLogic dmsLogic, OperationUser operationUser, MdrScopedIdRegistry aliasRegistry, CmdbfConfiguration cmdbfConfiguration, DmsConfiguration dmsConfiguration) {
		this.xmlRegistry = xmlRegistry;				
		this.dataAccessLogic = dataAccessLogic;
		this.dmsLogic = dmsLogic;
		this.operationUser = operationUser;
		this.aliasRegistry = aliasRegistry;
		this.cmdbfConfiguration = cmdbfConfiguration;
		this.dmsConfiguration = dmsConfiguration;
	}
	
	@Override	
	public String getMdrId() {
		return cmdbfConfiguration.getMdrId();
	}	
	
	@Override
	public RecordTypeList getRecordTypesList() {
		Map<String, RecordTypes> recordTypesMap = new HashMap<String, RecordTypes>();
		for(Object type : Iterables.concat(xmlRegistry.getTypes(CMClass.class), xmlRegistry.getTypes(CMDomain.class), xmlRegistry.getTypes(DocumentTypeDefinition.class))) {
			QName typeQName = xmlRegistry.getTypeQName(type);
			org.dmtf.schemas.cmdbf._1.tns.servicemetadata.RecordType recordType = new org.dmtf.schemas.cmdbf._1.tns.servicemetadata.RecordType();
			recordType.setLocalName(typeQName.getLocalPart());
			if(type instanceof CMClass){
				CMClass cmClass = (CMClass)type;
				recordType.setAppliesTo("item");
				if(cmClass.getParent() != null) {
					QName parentQName = xmlRegistry.getTypeQName(cmClass.getParent());
					org.dmtf.schemas.cmdbf._1.tns.servicemetadata.QNameType qName = new org.dmtf.schemas.cmdbf._1.tns.servicemetadata.QNameType();
					qName.setNamespace(parentQName.getNamespaceURI());
					qName.setLocalName(parentQName.getLocalPart());
					recordType.getSuperType().add(qName);
				}
			}
			else if(type instanceof CMDomain)
				recordType.setAppliesTo("relationship");
			else if(type instanceof DocumentTypeDefinition)
				recordType.setAppliesTo("item");
			
			RecordTypes recordTypes = recordTypesMap.get(typeQName.getNamespaceURI());
			if(recordTypes == null) {
				recordTypes = new RecordTypes();
				recordTypes.setNamespace(typeQName.getNamespaceURI());
				recordTypes.setSchemaLocation(xmlRegistry.getByNamespaceURI(typeQName.getNamespaceURI()).getSchemaLocation());
				recordTypesMap.put(typeQName.getNamespaceURI(), recordTypes);
			}
			recordTypes.getRecordType().add(recordType);
		}
		RecordTypeList recordTypeList = new RecordTypeList();
		recordTypeList.getRecordTypes().addAll(recordTypesMap.values());
		return recordTypeList;
	}
	
	@Override
	public QueryResultType graphQuery(QueryType body) throws InvalidPropertyTypeFault, UnknownTemplateIDFault, ExpensiveQueryErrorFault, QueryErrorFault, XPathErrorFault, UnsupportedSelectorFault, UnsupportedConstraintFault {
		return new CmdbQueryResult(body);
	}
		
	@Override
	public RegisterResponseType register(RegisterRequestType body) throws UnsupportedRecordTypeFault, InvalidRecordFault, InvalidMDRFault, RegistrationErrorFault {
		if(getMdrId().equals(body.getMdrId())) {
			try {
				RegisterResponseType registerResponse = new RegisterResponseType();
				if(body.getItemList() != null) {
					for(ItemType item : body.getItemList().getItem()) {
						CMDBfItem cmdbfItem = new CMDBfItem(item);
						
						RegisterInstanceResponseType registerInstanceResponse = new RegisterInstanceResponseType();
						MdrScopedIdType instanceId = item.getInstanceId().get(0);
						registerInstanceResponse.setInstanceId(instanceId);
						try{
							if(registerItem(cmdbfItem)) {
								AcceptedType accepted = new AcceptedType();
								for(CMDBfId id : cmdbfItem.instanceIds())
									accepted.getAlternateInstanceId().add(id);
								registerInstanceResponse.setAccepted(accepted);
							}
							else {
								DeclinedType declined = new DeclinedType();
								registerInstanceResponse.setDeclined(declined);
							}							
						} catch (Exception e) {
							DeclinedType declined = new DeclinedType();
							declined.getReason().add(e.getMessage());
							registerInstanceResponse.setDeclined(declined);
						}
						registerResponse.getRegisterInstanceResponse().add(registerInstanceResponse);
					}
				}
				if(body.getRelationshipList() != null) {
					for(RelationshipType relationship : body.getRelationshipList().getRelationship()) {
						CMDBfRelationship cmdbfRelationship = new CMDBfRelationship(relationship);
						
						RegisterInstanceResponseType registerInstanceResponse = new RegisterInstanceResponseType();
						MdrScopedIdType instanceId = relationship.getInstanceId().get(0);
						registerInstanceResponse.setInstanceId(instanceId);				
						try {
							if(registerRelationship(cmdbfRelationship)) {
								AcceptedType accepted = new AcceptedType();
								for(CMDBfId id : cmdbfRelationship.instanceIds())
									accepted.getAlternateInstanceId().add(id);
								registerInstanceResponse.setAccepted(accepted);
							}
							else {
								DeclinedType declined = new DeclinedType();
								registerInstanceResponse.setDeclined(declined);
							}					
						} catch (Exception e) {
							Log.CMDBUILD.error("CMDBf register", e);
							DeclinedType declined = new DeclinedType();
							declined.getReason().add(e.getMessage());
							registerInstanceResponse.setDeclined(declined);
						}
						registerResponse.getRegisterInstanceResponse().add(registerInstanceResponse);							
					}
				}
				return registerResponse;
			}
			catch(Throwable e) {
				Log.CMDBUILD.error("CMDBf register rollback", e);
				throw new RegistrationErrorFault(e.getMessage());
			}
		}
		else
			throw new InvalidMDRFault(body.getMdrId());
	}
	
	@Override
	public DeregisterResponseType deregister(DeregisterRequestType body) throws DeregistrationErrorFault, InvalidMDRFault {
		if(getMdrId().equals(body.getMdrId())) {
			try {
				DeregisterResponseType deregisterResponse = new DeregisterResponseType();
				if(body.getRelationshipIdList() != null) {
					for(MdrScopedIdType instanceId : body.getRelationshipIdList().getInstanceId()) {
						DeregisterInstanceResponseType deregisterInstanceResponse = new DeregisterInstanceResponseType();
						deregisterInstanceResponse.setInstanceId(instanceId);								
						try {
							if(!deregisterRelationship(instanceId)) {
								DeclinedType declined = new DeclinedType();
								deregisterInstanceResponse.setDeclined(declined);
							}					
						} catch (Exception e) {
							DeclinedType declined = new DeclinedType();
							declined.getReason().add(e.getMessage());
							deregisterInstanceResponse.setDeclined(declined);
						}
						deregisterResponse.getDeregisterInstanceResponse().add(deregisterInstanceResponse);
					}
				}
				if(body.getItemIdList() != null) {
					for(MdrScopedIdType instanceId : body.getItemIdList().getInstanceId()) {
						DeregisterInstanceResponseType deregisterInstanceResponse = new DeregisterInstanceResponseType();
						deregisterInstanceResponse.setInstanceId(instanceId);				
						try {
							if(!deregisterItem(instanceId)) {
								DeclinedType declined = new DeclinedType();
								deregisterInstanceResponse.setDeclined(declined);
							}
						} catch (Exception e) {
							Log.CMDBUILD.error("CMDBf deregister", e);
							DeclinedType declined = new DeclinedType();
							declined.getReason().add(e.getMessage());
							deregisterInstanceResponse.setDeclined(declined);
						}
						deregisterResponse.getDeregisterInstanceResponse().add(deregisterInstanceResponse);
					}
				}				return deregisterResponse;
			}
			catch(Throwable e) {
				Log.CMDBUILD.error("CMDBf register rollback", e);
				throw new DeregistrationErrorFault(e.getMessage());
			}
		}
		else
			throw new InvalidMDRFault(body.getMdrId());
	}
	
	private boolean registerItem(CMDBfItem item) throws RegistrationErrorFault {
		try {
			Collection<Long> idList = new ArrayList<Long>();
			for(CMDBfId alias : item.instanceIds()) {
				CMDBfId id = aliasRegistry.resolveAlias(alias);
				if(id != null)
					idList.add(aliasRegistry.getInstanceId(id));
			}
			boolean registered = false;			
			for(RecordType record : item.records()) {
				QName recordQName = CMDBfUtils.getRecordType(record);
				Object recordType = xmlRegistry.getType(recordQName);
				if(recordType instanceof CMClass) {
					CMCard card = Iterables.getOnlyElement(findCards(idList, (CMClass)recordType, null, new ArrayList<QName>()), null);
					Date recordDate = card!=null ? card.getBeginDate().toDate() : null;
					Date lastModified = null;
					if(record.getRecordMetadata()!=null && record.getRecordMetadata().getLastModified()!=null)
						lastModified = record.getRecordMetadata().getLastModified().toGregorianCalendar().getTime();
					if(recordDate==null || lastModified == null || !lastModified.before(recordDate)) {
						Long id = null;
						Element xml = CMDBfUtils.getRecordContent(record);
						Card newCard = (Card)xmlRegistry.deserialize(xml);
						if(card == null){
							id = dataAccessLogic.createCard(newCard);
							item.instanceIds().add(aliasRegistry.getCMDBfId(id));
							idList.add(id);
						}
						else {
							id = card.getId();
							Card.CardBuilder cardBuilder = Card.newInstance().clone(newCard);
							cardBuilder.withId(id);
							dataAccessLogic.updateCard(cardBuilder.build());
						}
						aliasRegistry.addAlias(id, item.instanceIds());
						item.instanceIds().addAll(aliasRegistry.getAlias(aliasRegistry.getCMDBfId(id)));
						registered = true;
					}					
				}
			}
			
			for(RecordType record : item.records()) {
				QName recordQName = CMDBfUtils.getRecordType(record);
				Object recordType = xmlRegistry.getType(recordQName);
				if(recordType instanceof DocumentTypeDefinition) {
					CMCard card = Iterables.getOnlyElement(findCards(idList, dataAccessLogic.findClass(Constants.BASE_CLASS_NAME), null, new ArrayList<QName>()), null);
					if(card != null) {
						Element xml = CMDBfUtils.getRecordContent(record);
						DmsDocument newDocument = (DmsDocument)xmlRegistry.deserialize(xml);
						
						Date recordDate = null; 
						Date lastModified = null;
						if(record.getRecordMetadata()!=null && record.getRecordMetadata().getLastModified()!=null)
							lastModified = record.getRecordMetadata().getLastModified().toGregorianCalendar().getTime();
						if(lastModified != null) {
							List<StoredDocument> documents = dmsLogic.search(card.getType().getIdentifier().getLocalName(), card.getId());
							Iterator<StoredDocument> documentIterator = documents.iterator();
							while(recordDate==null && documentIterator.hasNext()) {
								StoredDocument document = documentIterator.next();
								if(document.getName().equals(newDocument.getName()))
									recordDate = document.getModified();
							}
						}
						if(recordDate==null || lastModified == null || !lastModified.before(recordDate)) {									
							if(newDocument.getInputStream() != null)
								dmsLogic.upload(operationUser.getAuthenticatedUser().getUsername(), card.getType().getIdentifier().getLocalName(), card.getId(), newDocument.getInputStream(), newDocument.getName(), newDocument.getCategory(), newDocument.getDescription(), newDocument.getMetadataGroups());
							else
								dmsLogic.updateDescriptionAndMetadata(card.getType().getIdentifier().getLocalName(), card.getId(), newDocument.getName(), newDocument.getDescription(), newDocument.getMetadataGroups());
							aliasRegistry.addAlias(card.getId(), item.instanceIds());
							item.instanceIds().addAll(aliasRegistry.getAlias(aliasRegistry.getCMDBfId(card)));
							registered = true;						
						}											
					}
				}
			}
			return registered;
		} catch (Throwable e) {
			throw new RegistrationErrorFault(e.getMessage(), e);
		}
	}

	private boolean registerRelationship(CMDBfRelationship relationship) throws RegistrationErrorFault {
		try {
			Collection<Long> idList = new ArrayList<Long>();
			for(CMDBfId alias : relationship.instanceIds()) {
				CMDBfId id = aliasRegistry.resolveAlias(alias);
				if(id != null)
					idList.add(aliasRegistry.getInstanceId(id));
			}
			CMDBfId sourceId = aliasRegistry.resolveAlias(relationship.getSource());
			CMDBfId targetId = aliasRegistry.resolveAlias(relationship.getTarget());
			boolean registered = false;			
			for(RecordType record : relationship.records()) {
				QName recordQName = CMDBfUtils.getRecordType(record);
				Object recordType = xmlRegistry.getType(recordQName);
				if(recordType instanceof CMDomain) {
					CMRelation relation = Iterables.getOnlyElement(findRelations(idList, null, null, (CMDomain)recordType, null, new ArrayList<QName>()), null);
					if(relation == null && sourceId!=null && targetId!=null)
						relation = Iterables.getOnlyElement(findRelations(null, Arrays.asList(aliasRegistry.getInstanceId(sourceId)), Arrays.asList(aliasRegistry.getInstanceId(targetId)), (CMDomain)recordType, null, new ArrayList<QName>()), null);
					Date recordDate = relation!=null ? relation.getBeginDate().toDate() : null;
					Date lastModified = null;
					if(record.getRecordMetadata()!=null && record.getRecordMetadata().getLastModified()!=null)
						lastModified = record.getRecordMetadata().getLastModified().toGregorianCalendar().getTime();
					if(recordDate==null || lastModified == null || !lastModified.before(recordDate)) {
						Long id = null;
						Element xml = CMDBfUtils.getRecordContent(record);
						RelationDTO newRelation = (RelationDTO)xmlRegistry.deserialize(xml);
						newRelation.master = Source._1.name();
						if(relation == null){
							if(sourceId!=null && targetId!=null) {
								CMCard source = Iterables.getOnlyElement(findCards(Arrays.asList(aliasRegistry.getInstanceId(sourceId)), ((CMDomain) recordType).getClass1(), null, new ArrayList<QName>()), null);
								CMCard target = Iterables.getOnlyElement(findCards(Arrays.asList(aliasRegistry.getInstanceId(targetId)), ((CMDomain) recordType).getClass2(), null, new ArrayList<QName>()), null);
								if(source != null && target != null) {
									newRelation.addSourceCard(source.getId(), source.getType().getIdentifier().getLocalName());
									newRelation.addDestinationCard(target.getId(), target.getType().getIdentifier().getLocalName());
									dataAccessLogic.createRelations(newRelation);
									relation = Iterables.getOnlyElement(findRelations(null, Arrays.asList(source.getId()), Arrays.asList(target.getId()), (CMDomain)recordType, null, new ArrayList<QName>()));
									id = relation.getId();
									relationship.instanceIds().add(aliasRegistry.getCMDBfId(id));
									aliasRegistry.addAlias(id, relationship.instanceIds());
									relationship.instanceIds().addAll(aliasRegistry.getAlias(aliasRegistry.getCMDBfId(id)));
									registered = true;
								}
							}
						}
						else {
							id = relation.getId();
							newRelation.relationId = relation.getId();
							newRelation.addSourceCard(relation.getCard1Id(), relation.getType().getClass1().getIdentifier().getLocalName());
							newRelation.addDestinationCard(relation.getCard2Id(), relation.getType().getClass2().getIdentifier().getLocalName());
							dataAccessLogic.updateRelation(newRelation);
							aliasRegistry.addAlias(id, relationship.instanceIds());
							relationship.instanceIds().addAll(aliasRegistry.getAlias(aliasRegistry.getCMDBfId(id)));
							registered = true;
						}															
					}
				}
			}
			
			if(!registered) {
			}
			return registered;
		} catch (ParserConfigurationException e) {
			throw new RegistrationErrorFault(e.getMessage(), e);
		} catch (CMDBException e) {
			throw new RegistrationErrorFault(e.getMessage(), e);
		}		
	}

	private boolean deregisterItem(MdrScopedIdType instanceId) throws DeregistrationErrorFault {
		try {
			CMDBfId id = aliasRegistry.resolveAlias(instanceId);
			CMCard card = null;
			if(id != null) {
				for(CMClass cmClass : dataAccessLogic.findActiveClasses()) {
					if(card==null && !cmClass.isSuperclass())
						card = Iterables.getOnlyElement(findCards(Arrays.asList(aliasRegistry.getInstanceId(id)), cmClass, null, new ArrayList<QName>()), null);
				}
				if(card!=null) {
					String recordId = aliasRegistry.getRecordId(instanceId);
					if(recordId==null || recordId.startsWith(ENTRY_RECORDID_PREFIX)) {
						aliasRegistry.removeAlias(card);
						dataAccessLogic.deleteCard(card.getType().getIdentifier().getLocalName(), card.getId());
					}
					else if(recordId.startsWith(DOCUMENT_RECORDID_PREFIX)) {
						String name = recordId.substring(DOCUMENT_RECORDID_PREFIX.length());
						dmsLogic.delete(card.getType().getIdentifier().getLocalName(), card.getId(), name);
					}
				}
			}
			return card!=null;
		} catch(CMDBException e) {
			throw new DeregistrationErrorFault(e.getMessage(), e);
		}
	}

	private boolean deregisterRelationship(MdrScopedIdType instanceId) throws DeregistrationErrorFault {
		try {
			CMDBfId id = aliasRegistry.resolveAlias(instanceId);
			CMRelation relation = null;
			if(id != null) {
				for(CMDomain domain : dataAccessLogic.findActiveDomains()) {
					if(relation == null)
						relation = Iterables.getOnlyElement(findRelations(Arrays.asList(aliasRegistry.getInstanceId(id)), null, null, domain, null, new ArrayList<QName>()), null);
				}			
				if(relation!=null) {
					aliasRegistry.removeAlias(relation);
					dataAccessLogic.deleteRelation(relation.getType().getIdentifier().getLocalName(), relation.getId());
				}			
			}
			return relation != null;
		} catch(CMDBException e) {
			throw new DeregistrationErrorFault(e.getMessage(), e);
		}
	}

	private Collection<CMDBfItem> getItems(Set<CMDBfId> instanceId, RecordConstraintType recordConstraint, Map<Long, Long> typeMap) {
		try {
			List<CMClass> typeList = new ArrayList<CMClass>();
			Set<String> documentTypes = new HashSet<String>();
			if(recordConstraint!=null) {
				for(QNameType recordType : recordConstraint.getRecordType()){
					Object type = xmlRegistry.getType(new QName(recordType.getNamespace(), recordType.getLocalName()));
					if(type instanceof CMClass)
						typeList.add((CMClass)type);
					else if(type instanceof DocumentTypeDefinition) {
						documentTypes.add(((DocumentTypeDefinition)type).getName());
					}
				}
			}
			if(recordConstraint==null || recordConstraint.getRecordType().isEmpty() || (typeList.isEmpty() && !documentTypes.isEmpty())) {			
				for(CMClass cmClass : dataAccessLogic.findActiveClasses()) {
					if(!cmClass.isSystem() && !cmClass.isSuperclass())
						typeList.add(cmClass);
				}
			}
	
			Document xml = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();			
			Map<Long, List<Long>> idMap = buildIdMap(instanceId, typeMap);
			List<CMDBfItem> instanceList = new ArrayList<CMDBfItem>();
			for(CMClass type : typeList) {
				List<Long> idList = null;
				if(idMap!=null) {
					idList = idMap.get(type.getId());
					List<Long> unresolvedIdList = idMap.get(null);
					if(idList == null)
						idList = unresolvedIdList;
					else if(unresolvedIdList != null)
						idList.addAll(unresolvedIdList);
				}
				if(idMap == null || idList!=null) {
					for (CMCard card : findCards(idList, type, recordConstraint != null ? recordConstraint.getPropertyValue() :  null, new ArrayList<QName>())) {
						boolean match = false;
						if(!documentTypes.isEmpty()) {
							for(StoredDocument doc : dmsLogic.search(card.getType().getIdentifier().getLocalName(), card.getId())) {
								match |= documentTypes.contains(doc.getCategory());
								if(match && !recordConstraint.getPropertyValue().isEmpty()) {
									RecordType record = getRecord(aliasRegistry.getCMDBfId(card), doc, null, xml);
									final Map<QName, String> properties = CMDBfUtils.parseRecord(record);
									match &= Iterables.all(recordConstraint.getPropertyValue(), new Predicate<PropertyValueType>(){
										public boolean apply(PropertyValueType input) {
											return CMDBfUtils.filter(properties, input);
										}
									});
								}
							}
						}
						else
							match = true;
						if(match) {
							instanceList.add(getCMDBfItem(card));
							typeMap.put(card.getId(), card.getType().getId());
						}
					}
				}
			}
			
			return instanceList;
		} catch (ParserConfigurationException e) {
			throw new Error(e);
		}
	}

	private Collection<CMDBfRelationship> getRelationships(Set<CMDBfId> instanceId, Set<CMDBfId> source, Set<CMDBfId> target, RecordConstraintType recordConstraint, Map<Long, Long> typeMap) {		
		List<CMDomain> domainList = new ArrayList<CMDomain>();
		if(recordConstraint==null || recordConstraint.getRecordType().isEmpty()) {
			for(CMDomain domain : dataAccessLogic.findActiveDomains()) {
				if(!domain.isSystem())
					domainList.add(domain);
			}			
		}
		else {
			for(QNameType recordType : recordConstraint.getRecordType()){
				Object type = xmlRegistry.getType(new QName(recordType.getNamespace(), recordType.getLocalName()));
				if(type instanceof CMDomain)
					domainList.add((CMDomain)type);
			}	
		}

		Map<Long, List<Long>> idMap = buildIdMap(instanceId, typeMap);
		List<CMDBfRelationship> relationshipList = new ArrayList<CMDBfRelationship>();		
		for(CMDomain type : domainList) {			
			List<Long> idList = null;
			if(idMap!=null) {
				idList = idMap.get(type.getId());
				List<Long> unresolvedIdList = idMap.get(null);
				if(idList == null)
					idList = unresolvedIdList;
				else if(unresolvedIdList != null)
					idList.addAll(unresolvedIdList);
			}
			if(idMap == null || idList!=null) {
				for(CMRelation relation : findRelations(idList, getIdList(source), getIdList(target), type, recordConstraint!=null ? recordConstraint.getPropertyValue() : null, new ArrayList<QName>())) {
					relationshipList.add(getCMDBfRelationship(relation));
					typeMap.put(relation.getId(), relation.getType().getId());
				}
			}
		}		
		return relationshipList;		
	}

	private void fetchItemRecords(ItemSet<CMDBfItem> items, ContentSelectorType contentSelector, Map<Long, Long> typeMap) {
		try{
			Map<Long, List<Long>> idMap = buildIdMap(items.idSet(), typeMap);
			
			Map<QName, Set<QName>> propertyMap = null;
			if(contentSelector != null)
				propertyMap = CMDBfUtils.parseContentSelector(contentSelector);
			
			Set<String> documentTypes = new HashSet<String>();
			if(propertyMap != null) {
				for(QName qname : propertyMap.keySet()) {
					if(qname.getNamespaceURI() != null) {
						Object type = xmlRegistry.getType(qname);
						if(type instanceof DocumentTypeDefinition) {
							documentTypes.add(((DocumentTypeDefinition)type).getName());
						}
					}
				}
			}
			
			ContentSelectorFunction contentSelectorFunction = new ContentSelectorFunction(contentSelector);
			Document xml = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();			
			for(Long typeId : idMap.keySet()) {
				if(typeId != null) {
					CMClass type = dataAccessLogic.findClass(typeId);
					Collection<QName> properties = getTypeProperties(type, propertyMap);
					if(propertyMap==null || properties!=null) {
						for (CMCard card : findCards(idMap.get(typeId), type, null, properties)) {
							CMDBfItem item = items.get(aliasRegistry.getCMDBfId(card));
							item.records().add(getRecord(card, xml));
						}
					}
					if(dmsConfiguration.isEnabled()) {
						if(propertyMap==null || propertyMap.containsKey(new QName("")) || !documentTypes.isEmpty()) {
							for(Long cardId : idMap.get(typeId)) {
								for(StoredDocument document : dmsLogic.search(type.getIdentifier().getLocalName(), cardId)) {
									if(propertyMap==null || propertyMap.containsKey(new QName("")) || !documentTypes.contains(document.getCategory())){
										DataHandler dataHandler = dmsLogic.download(type.getIdentifier().getLocalName(), cardId, document.getName());
										CMDBfId id = aliasRegistry.getCMDBfId(cardId);						
										CMDBfItem item = items.get(id);
										RecordType record = getRecord(id, document, dataHandler.getInputStream(), xml);
										item.records().add(contentSelectorFunction.apply(record));
									}
								}
							}
						}
					}
				}
			}
		}
		catch(ParserConfigurationException e) {
			throw new Error(e);
		} catch (IOException e) {
			throw new Error(e);
		}
	}

	private void fetchRelationshipRecords(PathSet relationships, ContentSelectorType contentSelector, Map<Long, Long> typeMap) {
		try{
			Map<Long, List<Long>> idMap = buildIdMap(relationships.idSet(), typeMap);
			
			Map<QName, Set<QName>> propertyMap = null;
			if(contentSelector != null)
				propertyMap = CMDBfUtils.parseContentSelector(contentSelector);
			Document xml = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();			
			for(Long typeId : idMap.keySet()) {
				if(typeId != null) {
					CMDomain type = dataAccessLogic.findDomain(typeId);
					Collection<QName> properties = getTypeProperties(type, propertyMap);
					if(propertyMap==null || properties!=null) {
						for (CMRelation relation : findRelations(idMap.get(typeId), null, null, type, null, properties)) {
							CMDBfItem item = relationships.get(aliasRegistry.getCMDBfId(relation));
							item.records().add(getRecord(relation, xml));
						}
					}
				}
			}
		}
		catch(ParserConfigurationException e) {
			throw new Error(e);
		}			
	}
	
	private Collection<CMCard> findCards(Collection<Long> instanceId, CMClass type, Collection<PropertyValueType> filters, Collection<QName> properties) {
		List<CMCard> cardList = new ArrayList<CMCard>();			
			
		List<QueryAttribute> attributes = new ArrayList<QueryAttribute>();
		if(properties!=null && !properties.contains(new QName(""))) {
			for(QName property : properties) {
				if(type.getAttribute(property.getLocalPart())!=null)
					attributes.add(attribute(type, property.getLocalPart()));
			}
		}
		else
			attributes.add(anyAttribute(type));
	
		boolean isSatisfiable = true;
		List<WhereClause> conditions = new ArrayList<WhereClause>();
		if(instanceId!=null)
			 isSatisfiable = applyIdFilter(attribute(type, Constants.ID_ATTRIBUTE), instanceId, conditions);
		if(filters!=null)
			isSatisfiable &= applyPropertyFilter(type, filters, conditions);
		if(isSatisfiable) {
			QuerySpecsBuilder queryBuilder = dataAccessLogic.getView().select(attributes.toArray()).from(type);
			if(!conditions.isEmpty()) {
				if(conditions.size() == 1)
					queryBuilder.where(conditions.get(0));
				else if(conditions.size() == 2)
					queryBuilder.where(and(conditions.get(0), conditions.get(1)));
				else
					queryBuilder.where(and(conditions.get(0), conditions.get(1), conditions.subList(2, conditions.size()).toArray(new WhereClause[0])));
			}
			else
				queryBuilder.where(trueWhereClause());
			for (final CMQueryRow row : queryBuilder.run()) {
				CMCard card = row.getCard(type);
				cardList.add(card);
			}
		}
		return cardList;
	}

	private Collection<CMRelation> findRelations(Collection<Long> instanceId, Collection<Long> source, Collection<Long> target,	CMDomain type, Collection<PropertyValueType> filters, Collection<QName> properties) {
		List<CMRelation> relationList = new ArrayList<CMRelation>();

		List<QueryAttribute> attributes = new ArrayList<QueryAttribute>();
		if(properties!=null && !properties.contains(new QName(""))) {
			for(QName property : properties) {
				if(type.getAttribute(property.getLocalPart())!=null)
					attributes.add(attribute(type, property.getLocalPart()));
			}
		}
		else
			attributes.add(anyAttribute(type));
	
		boolean isSatisfiable = true;
		List<WhereClause> conditions = new ArrayList<WhereClause>();
		conditions.add(condition(attribute(type, "_Src"), eq(Source._1.name())));
		
		if(source != null)
			isSatisfiable = applyIdFilter(attribute(SOURCE_ALIAS, Constants.ID_ATTRIBUTE), source, conditions);
		if(target != null)
			isSatisfiable &= applyIdFilter(attribute(TARGET_ALIAS, Constants.ID_ATTRIBUTE), target, conditions);
		if(instanceId!=null)
			isSatisfiable &= applyIdFilter(attribute(type, Constants.ID_ATTRIBUTE), instanceId, conditions);
		if(filters!=null)
			isSatisfiable &= applyPropertyFilter(type, filters, conditions);
		if(isSatisfiable) {
			QuerySpecsBuilder queryBuilder = dataAccessLogic.getView().select(attributes.toArray());
			queryBuilder.from(type.getClass1(), SOURCE_ALIAS);
			queryBuilder.join(type.getClass2(), TARGET_ALIAS, over(type));
			if(!conditions.isEmpty()) {
				if(conditions.size() == 1)
					queryBuilder.where(conditions.get(0));
				else if(conditions.size() == 2)
					queryBuilder.where(and(conditions.get(0), conditions.get(1)));
				else
					queryBuilder.where(and(conditions.get(0), conditions.get(1), conditions.subList(2, conditions.size()).toArray(new WhereClause[0])));
			}
			else
				queryBuilder.where(trueWhereClause());
			for (final CMQueryRow row : queryBuilder.run()) {
				CMRelation relation = row.getRelation(type).getRelation();
				relationList.add(relation);
			}
		}
		return relationList;		
	}

	private CMDBfItem getCMDBfItem(CMCard element) {
		return new CMDBfItem(aliasRegistry.getCMDBfId(element));
    }
	
	private CMDBfRelationship getCMDBfRelationship(CMRelation relation) {
		return new CMDBfRelationship(aliasRegistry.getCMDBfId(relation), aliasRegistry.getCMDBfId(relation.getCard1Id()), aliasRegistry.getCMDBfId(relation.getCard2Id()));		
    }
	
	private RecordType getRecord(CMEntry element, Document xml) {
		try {
			DocumentFragment root = xml.createDocumentFragment();
			xmlRegistry.serialize(root, element);
			Element xmlElement = (Element)root.getFirstChild();
			RecordMetadata recordMetadata = new RecordMetadata();
			DatatypeFactory datatypeFactory = DatatypeFactory.newInstance();
			recordMetadata.setRecordId(aliasRegistry.getCMDBfId(element, ENTRY_RECORDID_PREFIX + element.getType().getIdentifier().getLocalName()).getLocalId());					
			GregorianCalendar calendar = element.getBeginDate().toGregorianCalendar();
			recordMetadata.setLastModified(datatypeFactory.newXMLGregorianCalendar(calendar));
			RecordType recordType = new RecordType();
			recordType.setRecordMetadata(recordMetadata);
			recordType.setAny(xmlElement);
			return recordType;
		} catch (DatatypeConfigurationException e) {
			throw new Error(e);
		}
	}

	private RecordType getRecord(CMDBfId id, StoredDocument document, InputStream inputStream, Document xml) {
		try {
			DocumentFragment root = xml.createDocumentFragment();
			xmlRegistry.serialize(root, new DmsDocument(document, inputStream));
			Element xmlElement = (Element)root.getFirstChild();
			RecordMetadata recordMetadata = new RecordMetadata();
			DatatypeFactory datatypeFactory = DatatypeFactory.newInstance();
			recordMetadata.setRecordId(aliasRegistry.getCMDBfId(id, DOCUMENT_RECORDID_PREFIX + document.getName()).getLocalId());					
			GregorianCalendar calendar = new GregorianCalendar();
			calendar.setTime(document.getCreated());
			recordMetadata.setLastModified(datatypeFactory.newXMLGregorianCalendar(calendar));
			RecordType recordType = new RecordType();
			recordType.setRecordMetadata(recordMetadata);
			recordType.setAny(xmlElement);
			return recordType;
		} catch (DatatypeConfigurationException e) {
			throw new Error(e);
		}
	}
	
	private boolean applyIdFilter(QueryAliasAttribute attribute, Collection<Long> idList, List<WhereClause> conditions) {
		boolean isSatisfiable = true; 
		if(idList.isEmpty())
			 isSatisfiable = false;
		 else
			 conditions.add(condition(attribute, in(idList.toArray())));
		return isSatisfiable;
	}
	
	private boolean applyPropertyFilter(CMEntryType type, Collection<PropertyValueType> propertyValueList, List<WhereClause> conditions) {
		boolean isSatisfiable = true;
		Iterator<PropertyValueType> iterator = propertyValueList.iterator();
		while(isSatisfiable && iterator.hasNext()) {
			PropertyValueType propertyValue = iterator.next();
			
			CMAttribute attribute = type.getAttribute(propertyValue.getLocalName());					
			if(attribute != null) {
				List<WhereClause> expressions = new ArrayList<WhereClause>();						
				if(propertyValue.getEqual() != null) {							
					for(EqualOperatorType operator : propertyValue.getEqual()) {
						WhereClause filter = condition(attribute(type, attribute.getName()), eq(operator.getValue()));
						if(operator.isNegate())
							filter = not(filter);
						expressions.add(filter);																
					}
				}						
				if(propertyValue.getLess() != null) {
					ComparisonOperatorType operator = propertyValue.getLess();
					WhereClause filter = condition(attribute(type, attribute.getName()), lt(operator.getValue()));
					if(operator.isNegate())
						filter = not(filter);
					expressions.add(filter);																
				}					
				if(propertyValue.getLessOrEqual() != null) {
					ComparisonOperatorType operator = propertyValue.getLessOrEqual();
					WhereClause filter = condition(attribute(type, attribute.getName()), gt(operator.getValue()));
					if(!operator.isNegate())
						filter = not(filter);
					expressions.add(filter);																
				}															
				if(propertyValue.getGreater() != null) {
					ComparisonOperatorType operator = propertyValue.getGreater();
					WhereClause filter = condition(attribute(type, attribute.getName()), gt(operator.getValue()));
					if(operator.isNegate())
						filter = not(filter);
					expressions.add(filter);																
				}									
				if(propertyValue.getGreaterOrEqual() != null) {
					ComparisonOperatorType operator = propertyValue.getGreater();
					WhereClause filter = condition(attribute(type, attribute.getName()), lt(operator.getValue()));
					if(!operator.isNegate())
						filter = not(filter);
					expressions.add(filter);																
				}																					
				if(propertyValue.getContains() != null) {							
					for(StringOperatorType operator : propertyValue.getContains()) {
						WhereClause filter = condition(attribute(type, attribute.getName()), contains(operator.getValue()));
						if(operator.isNegate())
							filter = not(filter);
						expressions.add(filter);																
					}
				}
				if(propertyValue.getLike() != null) {							
					for(StringOperatorType operator : propertyValue.getLike()) {
						WhereClause filter = null;
						if(operator.getValue().startsWith("%") && operator.getValue().endsWith("%"))
							filter = condition(attribute(type, attribute.getName()), contains(operator.getValue().substring(1, operator.getValue().length()-1)));
						else if(operator.getValue().startsWith("%"))
							filter = condition(attribute(type, attribute.getName()), endsWith(operator.getValue().substring(1)));
						else if(operator.getValue().endsWith("%"))
							filter = condition(attribute(type, attribute.getName()), beginsWith(operator.getValue().substring(0, operator.getValue().length()-1)));
						else
							filter = condition(attribute(type, attribute.getName()), contains(operator.getValue()));
						if(operator.isNegate())
							filter = not(filter);
						expressions.add(filter);																
					}
				}	
				if(propertyValue.getIsNull() != null) {
					NullOperatorType operator = propertyValue.getIsNull();
					WhereClause filter = condition(attribute(type, attribute.getName()), isNull());
					if(operator.isNegate())
						filter = not(filter);
					expressions.add(filter);																
				}
				if(!expressions.isEmpty()) {
					WhereClause propertyFilter = null;
					if(propertyValue.isMatchAny()) {
						if(expressions.size() == 1)
							propertyFilter = expressions.get(0);
						else if(expressions.size() == 2)
							propertyFilter = or(expressions.get(0), expressions.get(1));
						else
							propertyFilter = or(expressions.get(0), expressions.get(1), expressions.subList(2, expressions.size()).toArray(new WhereClause[0]));
					}
					else {
						if(expressions.size() == 1)
							propertyFilter = expressions.get(0);
						else if(expressions.size() == 2)
							propertyFilter = and(expressions.get(0), expressions.get(1));
						else
							propertyFilter = and(expressions.get(0), expressions.get(1), expressions.subList(2, expressions.size()).toArray(new WhereClause[0]));
					}
		    		conditions.add(propertyFilter);
				}
			}
			else
				isSatisfiable = false;
		}
		return isSatisfiable;
	}
	
	private Collection<Long> getIdList(Iterable<? extends MdrScopedIdType>instanceId) {
		Collection<Long> idSet = null;
		if(instanceId != null) {
			idSet = new HashSet<Long>();
			for(MdrScopedIdType id : instanceId) {					
				if(aliasRegistry.isLocal(id))
					idSet.add(aliasRegistry.getInstanceId(id));
			}
		}
		return idSet;
	}
	
	private Map<Long, List<Long>> buildIdMap(Iterable<? extends MdrScopedIdType> instanceId, Map<Long, Long> typeMap){
		Map<Long, List<Long>> idMap = null;
		if(instanceId != null) {
			idMap = new HashMap<Long, List<Long>>();
			Collection<Long> idSet = getIdList(instanceId);
			for(Long id : idSet) {
				Long typeId = typeMap.get(id);
				List<Long> idList = idMap.get(typeId);
				if(idList == null) {
					idList = new ArrayList<Long>();
					idMap.put(typeId, idList);
				}
				idList.add(id);
			}
		}
		return idMap;
	}
	
	private Collection<QName> getTypeProperties(CMEntryType type, Map<QName, Set<QName>> propertyMap) {
		Set<QName> properties = null;
		if(propertyMap!=null){
			if(propertyMap.containsKey(new QName(""))){
				if(properties == null)
					 properties = new HashSet<QName>();
				for(QName property : propertyMap.get(new QName("")))
					properties.add(property);
			}
			while(type!=null){
				Set<QName> propertySet = propertyMap.get(xmlRegistry.getTypeQName(type));
				if(propertySet != null) {
					if(properties == null)
						 properties = new HashSet<QName>();
					for(QName property : propertySet)
						properties.add(property);
				}
				if(type instanceof CMClass)
					type = ((CMClass)type).getParent();
				else
					type = null;
			}							
		}
		return properties;
	}
}
