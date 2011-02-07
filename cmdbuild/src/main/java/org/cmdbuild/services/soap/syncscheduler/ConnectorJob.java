package org.cmdbuild.services.soap.syncscheduler;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.cmdbuild.elements.AttributeValue;
import org.cmdbuild.elements.DirectedDomain;
import org.cmdbuild.elements.Lookup;
import org.cmdbuild.elements.Reference;
import org.cmdbuild.elements.filters.AttributeFilter.AttributeFilterType;
import org.cmdbuild.elements.interfaces.CardQuery;
import org.cmdbuild.elements.interfaces.IAttribute;
import org.cmdbuild.elements.interfaces.ICard;
import org.cmdbuild.elements.interfaces.IDomain;
import org.cmdbuild.elements.interfaces.IRelation;
import org.cmdbuild.elements.interfaces.ITable;
import org.cmdbuild.exception.CMDBException;
import org.cmdbuild.exception.ORMException;
import org.cmdbuild.logger.Log;
import org.cmdbuild.operation.management.LookupOperation;
import org.cmdbuild.services.auth.UserContext;
import org.dom4j.Element;

public class ConnectorJob implements Runnable {

	UserContext userCtx;

	private Action action;
	private boolean isMaster; // id of the master card
	private int masterCardId; // id of the master card
	private String masterClassName; // name of the class of the master class
	private int detailCardId; // id of the detail card
	private String detailClassName; // name of the class of the detail class
	private String domainName; // name of the domain between master and detail
	private DomainDirected domainDirection;
	private boolean isShared; // relation 1:N -> 1 detail : N master
	private List<String> sharedIds; //the "id" used to search for the details
	private Element elementCard;
	private int jobNumber;
	private static int jobNumberCounter = 0;

	public ConnectorJob() {
		jobNumber = ++jobNumberCounter;
	}

	private Map<String, String> referenceToMaster = new HashMap<String, String>();

	public enum Action {
		CREATE("create"), UPDATE("update"), DELETE("delete");

		private final String action;

		Action(String action) {
			this.action = action;
		}

		public String getAction() {
			return action;
		}

		public static Action getAction(String action) throws Exception {
			if (Action.CREATE.getAction().equals(action)) {
				return Action.CREATE;
			} else {
				if (Action.UPDATE.getAction().equals(action)) {
					return Action.UPDATE;
				} else if (Action.DELETE.getAction().equals(action)) {
					return Action.DELETE;
				}
			}
			throw new Exception();
		}
	}

	public enum DomainDirected {
		DIRECTED("directed"), INVERTED("inverted");

		private final String direction;

		DomainDirected(String direction) {
			this.direction = direction;
		}

		public String getDirection() {
			return this.direction;
		}

		public static DomainDirected getDirection(String action) {

			if (DomainDirected.DIRECTED.getDirection().equals(action))
				return DomainDirected.DIRECTED;
			else
				return DomainDirected.INVERTED;
		}
	}

	public void run() {
		try {
			if (action != null) {
				switch (action) {
				case CREATE:
					Log.SOAP.info("ExternalSync - create job started ["+ jobNumber + "]");
					create();
					break;
				case DELETE:
					Log.SOAP.info("ExternalSync - delete job started ["+ jobNumber + "]");
					delete();
					break;
				case UPDATE:
					Log.SOAP.info("ExternalSync - update job started ["+ jobNumber + "]");
					update();
					break;
				default:
					throw new Exception("No action selected");
				}
			} else {
				Log.SOAP.info("External Sync - running the current process has failed, try to star the next job");
				throw new Exception("No action selected");
			}
		} catch (Exception e) {
			Log.SOAP.info("External Sync - running the current process has failed, try to start the next job "+ e);
		}
	}

	/** GETTER / SETTER **/
	public void setIsMaster(boolean isMaster) {
		this.isMaster = isMaster;
	}

	public void setMasterClassName(String name) {
		this.masterClassName = name;
	}

	public void setMasterCardId(int id) {
		this.masterCardId = id;
	}

	public void setDetailClassName(String name) {
		this.detailClassName = name;
	}

	public void setDetailCardId(int id) {
		this.detailCardId = id;
	}

	public void setElementCard(Element element) {
		this.elementCard = element;
	}

	public void setDomainName(String name) {
		this.domainName = name;
	}

	public void setDomainDirection(String name) {
		this.domainName = name;
	}

	public void setAction(Action action) {
		this.action = action;
	}

	public void setDomainDirection(DomainDirected domainDirection) {
		this.domainDirection = domainDirection;
	}
	
	public void setIsShared(boolean isShared) {
		this.isShared = isShared;
	}

	public void setDetailIdentifiers(List<String> identifiers) {
		this.sharedIds = identifiers;
	}
	
	public void setUserContext(UserContext userCtx) {
		this.userCtx = userCtx;
	}

	/**
	 * @throws AxisFault
	 **/

	private void create() throws Exception {
		if (isMaster) {
			createCard();
		} else {
			if (this.masterCardId > 0) {
				if (detailHasReferenceToMaster()) {
					String referenceName = referenceToMaster.get(detailClassName);
					detailCardId = createCard(referenceName);
				} else {
					detailCardId = createCard();
					if (detailCardId > 0)
						createRelation();
				}
			} else
				throw new Exception("MasterCardId is 0");
		}
	}

	private void update() {
		updateCard();
		Log.SOAP.info("ExternalSync - end update card");

	}

	private void delete() throws Exception {
		if (isMaster) {
			deleteCard();
		} else {
			if (this.masterCardId > 0) {
				if ((!this.isShared) || (this.isShared && isLastSharedDetail())) {
					deleteCard();
					Log.SOAP.info("ExternalSync - deleting card ");
				}else{
					Log.SOAP.info("ExternalSync - card detail is shared and has other relations - cannot delete! ");
				}
				if (!detailHasReferenceToMaster())
					deleteRelation();
			} else
				throw new Exception("MasterCardId is 0");
		}
	}

	private boolean isLastSharedDetail() {
		ICard cardDetail = userCtx.tables().get(detailClassName).cards().get(detailCardId);
		IDomain domain = userCtx.domains().get(domainName);

		boolean boolDomainDirection = DomainDirected.DIRECTED.getDirection().equals(this.domainDirection);
		DirectedDomain directedDomain = DirectedDomain.create(domain, boolDomainDirection);
		
		boolean detailHasRelations = false;
		Iterator<IRelation> iteratorRel = userCtx.relations().list(cardDetail).domain(directedDomain).iterator();
		if(iteratorRel.hasNext()){
			iteratorRel.next();
			detailHasRelations=iteratorRel.hasNext();
		}
		return ! detailHasRelations;
	}

	/**********************
	 ** CARD MANAGEMENT **
	 **********************/
	private int createCard() {
		return createCard("");
	}
	
	private int createCard(String referenceName) {
		
		if(this.isShared){
			//searching for an existent object
			ITable detailClass = userCtx.tables().get(this.detailClassName);
			CardQuery cardList=detailClass.cards().list();
			
			for(String attributeName : this.sharedIds){
				String attributeValue=searchAttributeValue(attributeName); 
				cardList.filter(attributeName, AttributeFilterType.EQUALS, attributeValue);
			}
			if(cardList!=null){
				cardList.limit(1);
				Iterator<ICard> cardIterator = cardList.iterator();

				//detail exists
				if (cardIterator.hasNext()) {
					ICard card = cardIterator.next();
					return card.getId();
				}
			}
		}
		//detail does not exist, must be inserted
		Log.SOAP.info("ExternalSync - insert a new card [class:"+ this.detailClassName + "]");
		try {
			ICard card = getNewCard();
			if(!referenceName.equals("")){
				Log.SOAP.info("ExternalSync - the card [class:"+this.detailClassName + "] has a reference to the card-master");
				card.setValue(referenceName, String.valueOf(this.masterCardId));
			}
			card.save();
			return card.getId();
		} catch (CMDBException e) {
			Log.SOAP.error("ExternalSync - exception raised while creating a new card", e);
		}
		return 0;
	}

	private ICard getNewCard() {
		ICard card = userCtx.tables().get(this.detailClassName).cards().create();
		setCardFields(card);
		return card;
	}

	/**
	 * params className the classname of the card to update params cardId the id
	 * of the card to update params elementCard the xml Element containg all
	 * values to insert
	 */
	private void updateCard() {
		Log.SOAP.info("ExternalSync - update card [id:" + this.detailCardId+ " classname: " + this.detailClassName + "]");
		try {
			ITable table = userCtx.tables().get(this.detailClassName);

			if (this.detailCardId > 0) {
				ICard card = table.cards().get(this.detailCardId);
				Log.SOAP.info("ExternalSync - set card fields [id:"+ this.detailCardId + " classname: "+this.detailClassName + "]");
				setCardFields(card);
				Log.SOAP.info("ExternalSync - end set card fields [id:"+ this.detailCardId + " classname: "+ this.detailClassName + "]");
				card.save();
				Log.SOAP.info("ExternalSync - end save card [id:"+ this.detailCardId + " classname: "+ this.detailClassName + "]");
			} else {
				Log.SOAP.warn("ExternalSync - required an update of card with cardId "+ this.detailCardId+ " (classname "+ this.detailClassName + ")");
			}
		} catch (CMDBException e) {
			Log.SOAP.error("ExternalSync - exception raised while updating card [id:"+ this.detailCardId + " classname: "+ this.detailClassName + "]", e);
		}
	}

	/**
	 * params className the classname of the card to update params cardId the id
	 * of the card to update params elementCard the xml Element containg all
	 * values to insert
	 */
	private int deleteCard() {
		Log.SOAP.info("ExternalSync - delete card [id:" + this.detailCardId + " classname: " + this.detailClassName + "]");
		try {
			ICard card = userCtx.tables().get(this.detailClassName).cards().get(this.detailCardId);
			card.delete();
		} catch (CMDBException e) {
			Log.SOAP.error(
					"ExternalSync - exception raised while deleting card [id:"
							+ this.detailCardId + " classname: "
							+ this.detailClassName + "]", e);
		}
		return 0;
	}

	/**
	 * @param card
	 *            the ICard to create/modify/delete
	 * @param elementCard
	 *            the xml Element containg the data to create/modify
	 */
	@SuppressWarnings(value = { "unchecked" })
	private void setCardFields(ICard card) {
		Iterator<Element> attributeIterator = elementCard.elementIterator();
		while (attributeIterator.hasNext()) {
			Element cardAttribute = (Element) attributeIterator.next();
			String attributeName = cardAttribute.getName();
			String attributeValue = cardAttribute.getText();

			IAttribute attribute = card.getSchema().getAttribute(attributeName);

			// check if the attribute is a lookup
			if (attribute.getLookupType() != null) {
				setLookupAttribute(attribute, card, attributeName,
						attributeValue);
			}
			// it isn't a lookup
			else {
				// check if the attribute is a reference
				IDomain idomain = attribute.getReferenceDomain();
				if (idomain != null) {
					setReferenceAttribute(card, attribute, attributeValue);
				}
				// it isn't a reference
				else
					try{
						card.getAttributeValue(attributeName).setValue(attributeValue);
					}catch (ORMException e){}
			}
		}
	}

	/****************************
	 *** RELATION MANAGEMENT ***
	 ****************************/

	private void createRelation() {

		Log.SOAP.info("ExternalSync - create new relation between "
				+ "card [id:" + this.masterCardId + " classname: "
				+ this.masterClassName + "] and " + "card [id:" + detailCardId
				+ " classname: " + detailClassName + "] " + "on domain: "
				+ domainName);
		try {
		
			ICard cardMaster = userCtx.tables().get(this.masterClassName).cards().get(this.masterCardId);
			ICard cardDetail = userCtx.tables().get(detailClassName).cards().get(detailCardId);
			IDomain idomain = userCtx.domains().get(domainName);

			IRelation irelation;

			if (DomainDirected.DIRECTED.equals(this.domainDirection)) {
				Log.SOAP.info("ExternalSync - Relation class1: "
						+ this.masterClassName + "(id1: " + this.masterCardId
						+ ") class2: " + this.detailClassName + "(id2: "
						+ this.detailCardId + ")");
				irelation = userCtx.relations().create(idomain, cardMaster, cardDetail);
			} else {
				Log.SOAP.info("ExternalSync - Relation class1: "
						+ this.detailClassName + "(id1: " + this.detailCardId
						+ ") class2: " + this.masterClassName + "(id2: "
						+ this.masterCardId + ")");
				irelation = userCtx.relations().create(idomain, cardDetail, cardMaster);
			}
			irelation.setSchema(idomain);
			irelation.save();
		} catch (CMDBException e) {
			Log.SOAP.error("ExternalSync - exception raised while creating a new relation",e);
			Log.SOAP.debug("Exception parameters" + e.getExceptionParameters());
		}
	}

	private void deleteRelation() {

		Log.SOAP.info("ExternalSync - deleting relation between " + "card [id:"
				+ this.masterCardId + " classname: " + this.masterClassName
				+ "] and " + "card [id:" + detailCardId + " classname: "
				+ this.detailClassName + "] " + "on domain: " + domainName);
		try {

			ICard cardMaster = userCtx.tables().get(this.masterClassName).cards().get(this.masterCardId);
			ICard cardDetail = userCtx.tables().get(detailClassName).cards().get(detailCardId);
			IDomain idomain = userCtx.domains().get(domainName);

			IRelation irelation;
			if (DomainDirected.DIRECTED.getDirection().equals(this.domainDirection.getDirection())) {
				irelation = userCtx.relations().get(idomain, cardMaster, cardDetail);
			} else {
				irelation = userCtx.relations().get(idomain, cardDetail, cardMaster);
			}
			irelation.delete();

		} catch (CMDBException e) {
			Log.SOAP.error("ExternalSync - exception raised while creating a new relation",e);
		}
	}

	private boolean detailHasReferenceToMaster() {

		if (referenceToMaster.containsKey(this.detailClassName)) {
			String referenceName = referenceToMaster.get(this.detailClassName);
			return referenceName != null && !referenceName.equals("");
		} else {
			ITable table = userCtx.tables().get(this.detailClassName);

			Map<String, IAttribute> attributes = table.getAttributes();
			Set<String> attributeNames = attributes.keySet();
			for (String attrName : attributeNames) {
				IAttribute attribute = attributes.get(attrName);
				IDomain domain = attribute.getReferenceDomain();
				if (domain != null && domainName.equals(domain.getName())) {
					referenceToMaster.put(this.detailClassName, attrName);
					return true;
				}
			}
			referenceToMaster.put(domainName, "");
			return false;
		}
	}

	private void setReferenceAttribute(ICard card, IAttribute cardAttribute,String attributeValue) {
		String attributeName = cardAttribute.getName();

		if (attributeValue.equals("")) {
			Log.SOAP.warn("ExternalSync - The external Connector has request to modify a reference but doesn't know the value to insert");
		} else {
			ITable referencedClass = userCtx.tables().get(cardAttribute.getReferenceTarget().getName());

			String referencedAttributeName = "Description";
			CardQuery referencedCardList = referencedClass.cards().list().filter(referencedAttributeName, AttributeFilterType.EQUALS, attributeValue).limit(1);
		    
			Iterator<ICard> cardIterator = referencedCardList.iterator();
			if (cardIterator.hasNext()) {
				ICard referencedCard = cardIterator.next();
				AttributeValue  av = card.getAttributeValue(attributeName);
				av.setValue(new Reference(av.getSchema().getReferenceDirectedDomain(), referencedCard.getId(),referencedCard.getDescription()));
			} else {
				Log.SOAP.error("ExternalSync - Reference not inserted - No cards found having "
								+ referencedClass.getName()
								+ "."
								+ referencedAttributeName
								+ ": "
								+ attributeValue);
			}
		}
	}

	private void setLookupAttribute(IAttribute attribute, ICard card,
			String attributeName, String attributeValue) {
		String lookupType = attribute.getLookupType().getType();
		LookupOperation lo = new LookupOperation(userCtx);
		Iterable<Lookup> lookupList = lo.getLookupList(lookupType);

		for (Lookup l : lookupList) {
			if (attributeValue.equals(l.getDescription())) {
				card.getAttributeValue(attributeName).setValue(l);
				break;
			}
		}
		Lookup lookup = card.getAttributeValue(attributeName).getLookup();
		if (lookup == null || lookup.equals("")) {
			lookup = new Lookup();
			lookup.setType(lookupType);
			lookup.setDescription(attributeValue);
			lookup.save();
		}
	}
	
	@SuppressWarnings(value = { "unchecked" })
	private String searchAttributeValue(String attributeName){
		Iterator<Element> attributeIterator = elementCard.elementIterator();
		while (attributeIterator.hasNext()) {
			Element cardAttribute = (Element) attributeIterator.next();
			if(cardAttribute.getName().equals(attributeName)){
				return cardAttribute.getText();
			}
		}
		return "";
	}
}
