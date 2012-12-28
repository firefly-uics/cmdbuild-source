package org.cmdbuild.services.soap.syncscheduler;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.axis.AxisFault;
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
import org.cmdbuild.services.auth.UserOperations;
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
	private List<String> sharedIds; // the "id" used to search for the details
	private Element elementCard;
	private final int jobNumber;
	private static int jobNumberCounter = 0;

	public ConnectorJob() {
		jobNumber = ++jobNumberCounter;
	}

	private final Map<String, String> referenceToMaster = new HashMap<String, String>();

	public enum Action {
		CREATE("create"), UPDATE("update"), DELETE("delete");

		private final String action;

		Action(final String action) {
			this.action = action;
		}

		public String getAction() {
			return action;
		}

		public static Action getAction(final String action) throws Exception {
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

		DomainDirected(final String direction) {
			this.direction = direction;
		}

		public String getDirection() {
			return this.direction;
		}

		public static DomainDirected getDirection(final String action) {

			if (DomainDirected.DIRECTED.getDirection().equals(action))
				return DomainDirected.DIRECTED;
			else
				return DomainDirected.INVERTED;
		}
	}

	@Override
	public void run() {
		try {
			if (action != null) {
				switch (action) {
				case CREATE:
					Log.SOAP.info("ExternalSync - create job started [" + jobNumber + "]");
					create();
					break;
				case DELETE:
					Log.SOAP.info("ExternalSync - delete job started [" + jobNumber + "]");
					delete();
					break;
				case UPDATE:
					Log.SOAP.info("ExternalSync - update job started [" + jobNumber + "]");
					update();
					break;
				default:
					throw new Exception("No action selected");
				}
			} else {
				Log.SOAP.info("External Sync - running the current process has failed, try to star the next job");
				throw new Exception("No action selected");
			}
		} catch (final Exception e) {
			Log.SOAP.info("External Sync - running the current process has failed, try to start the next job " + e);
		}
	}

	/** GETTER / SETTER **/
	public void setIsMaster(final boolean isMaster) {
		this.isMaster = isMaster;
	}

	public void setMasterClassName(final String name) {
		this.masterClassName = name;
	}

	public void setMasterCardId(final int id) {
		this.masterCardId = id;
	}

	public void setDetailClassName(final String name) {
		this.detailClassName = name;
	}

	public void setDetailCardId(final int id) {
		this.detailCardId = id;
	}

	public void setElementCard(final Element element) {
		this.elementCard = element;
	}

	public void setDomainName(final String name) {
		this.domainName = name;
	}

	public void setDomainDirection(final String name) {
		this.domainName = name;
	}

	public void setAction(final Action action) {
		this.action = action;
	}

	public void setDomainDirection(final DomainDirected domainDirection) {
		this.domainDirection = domainDirection;
	}

	public void setIsShared(final boolean isShared) {
		this.isShared = isShared;
	}

	public void setDetailIdentifiers(final List<String> identifiers) {
		this.sharedIds = identifiers;
	}

	public void setUserContext(final UserContext userCtx) {
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
					final String referenceName = referenceToMaster.get(detailClassName);
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
				} else {
					Log.SOAP.info("ExternalSync - card detail is shared and has other relations - cannot delete! ");
				}
				if (!detailHasReferenceToMaster())
					deleteRelation();
			} else
				throw new Exception("MasterCardId is 0");
		}
	}

	private boolean isLastSharedDetail() {
		final ICard cardDetail = UserOperations.from(userCtx).tables().get(detailClassName).cards().get(detailCardId);
		final IDomain domain = UserOperations.from(userCtx).domains().get(domainName);

		final boolean boolDomainDirection = DomainDirected.DIRECTED.getDirection().equals(this.domainDirection);
		final DirectedDomain directedDomain = DirectedDomain.create(domain, boolDomainDirection);

		boolean detailHasRelations = false;
		final Iterator<IRelation> iteratorRel = UserOperations.from(userCtx).relations().list(cardDetail)
				.domain(directedDomain).iterator();
		if (iteratorRel.hasNext()) {
			iteratorRel.next();
			detailHasRelations = iteratorRel.hasNext();
		}
		return !detailHasRelations;
	}

	/**********************
	 ** CARD MANAGEMENT **
	 **********************/
	private int createCard() {
		return createCard("");
	}

	private int createCard(final String referenceName) {

		if (this.isShared) {
			// searching for an existent object
			final ITable detailClass = UserOperations.from(userCtx).tables().get(this.detailClassName);
			final CardQuery cardList = detailClass.cards().list();

			for (final String attributeName : this.sharedIds) {
				final String attributeValue = searchAttributeValue(attributeName);
				cardList.filter(attributeName, AttributeFilterType.EQUALS, attributeValue);
			}
			if (cardList != null) {
				cardList.limit(1);
				final Iterator<ICard> cardIterator = cardList.iterator();

				// detail exists
				if (cardIterator.hasNext()) {
					final ICard card = cardIterator.next();
					return card.getId();
				}
			}
		}
		// detail does not exist, must be inserted
		Log.SOAP.info("ExternalSync - insert a new card [class:" + this.detailClassName + "]");
		try {
			final ICard card = getNewCard();
			if (!referenceName.equals("")) {
				Log.SOAP.info("ExternalSync - the card [class:" + this.detailClassName
						+ "] has a reference to the card-master");
				card.setValue(referenceName, String.valueOf(this.masterCardId));
			}
			card.save();
			return card.getId();
		} catch (final CMDBException e) {
			Log.SOAP.error("ExternalSync - exception raised while creating a new card", e);
		}
		return 0;
	}

	private ICard getNewCard() {
		final ICard card = UserOperations.from(userCtx).tables().get(this.detailClassName).cards().create();
		setCardFields(card);
		return card;
	}

	/**
	 * params className the classname of the card to update params cardId the id
	 * of the card to update params elementCard the xml Element containg all
	 * values to insert
	 */
	private void updateCard() {
		Log.SOAP.info("ExternalSync - update card [id:" + this.detailCardId + " classname: " + this.detailClassName
				+ "]");
		try {
			final ITable table = UserOperations.from(userCtx).tables().get(this.detailClassName);

			if (this.detailCardId > 0) {
				final ICard card = table.cards().get(this.detailCardId);
				Log.SOAP.info("ExternalSync - set card fields [id:" + this.detailCardId + " classname: "
						+ this.detailClassName + "]");
				setCardFields(card);
				Log.SOAP.info("ExternalSync - end set card fields [id:" + this.detailCardId + " classname: "
						+ this.detailClassName + "]");
				card.save();
				Log.SOAP.info("ExternalSync - end save card [id:" + this.detailCardId + " classname: "
						+ this.detailClassName + "]");
			} else {
				Log.SOAP.warn("ExternalSync - required an update of card with cardId " + this.detailCardId
						+ " (classname " + this.detailClassName + ")");
			}
		} catch (final CMDBException e) {
			Log.SOAP.error("ExternalSync - exception raised while updating card [id:" + this.detailCardId
					+ " classname: " + this.detailClassName + "]", e);
		}
	}

	/**
	 * params className the classname of the card to update params cardId the id
	 * of the card to update params elementCard the xml Element containg all
	 * values to insert
	 */
	private int deleteCard() {
		Log.SOAP.info("ExternalSync - delete card [id:" + this.detailCardId + " classname: " + this.detailClassName
				+ "]");
		try {
			final ICard card = UserOperations.from(userCtx).tables().get(this.detailClassName).cards()
					.get(this.detailCardId);
			card.delete();
		} catch (final CMDBException e) {
			Log.SOAP.error("ExternalSync - exception raised while deleting card [id:" + this.detailCardId
					+ " classname: " + this.detailClassName + "]", e);
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
	private void setCardFields(final ICard card) {
		final Iterator<Element> attributeIterator = elementCard.elementIterator();
		while (attributeIterator.hasNext()) {
			final Element cardAttribute = attributeIterator.next();
			final String attributeName = cardAttribute.getName();
			final String attributeValue = cardAttribute.getText();

			final IAttribute attribute = card.getSchema().getAttribute(attributeName);

			// check if the attribute is a lookup
			if (attribute.getLookupType() != null) {
				setLookupAttribute(attribute, card, attributeName, attributeValue);
			}
			// it isn't a lookup
			else {
				// check if the attribute is a reference
				final IDomain idomain = attribute.getReferenceDomain();
				if (idomain != null) {
					setReferenceAttribute(card, attribute, attributeValue);
				}
				// it isn't a reference
				else
					try {
						card.getAttributeValue(attributeName).setValue(attributeValue);
					} catch (final ORMException e) {
					}
			}
		}
	}

	/****************************
	 *** RELATION MANAGEMENT ***
	 ****************************/

	private void createRelation() {

		Log.SOAP.info("ExternalSync - create new relation between " + "card [id:" + this.masterCardId + " classname: "
				+ this.masterClassName + "] and " + "card [id:" + detailCardId + " classname: " + detailClassName
				+ "] " + "on domain: " + domainName);
		try {

			final ICard cardMaster = UserOperations.from(userCtx).tables().get(this.masterClassName).cards()
					.get(this.masterCardId);
			final ICard cardDetail = UserOperations.from(userCtx).tables().get(detailClassName).cards()
					.get(detailCardId);
			final IDomain idomain = UserOperations.from(userCtx).domains().get(domainName);

			IRelation irelation;

			if (DomainDirected.DIRECTED.equals(this.domainDirection)) {
				Log.SOAP.info("ExternalSync - Relation class1: " + this.masterClassName + "(id1: " + this.masterCardId
						+ ") class2: " + this.detailClassName + "(id2: " + this.detailCardId + ")");
				irelation = UserOperations.from(userCtx).relations().create(idomain, cardMaster, cardDetail);
			} else {
				Log.SOAP.info("ExternalSync - Relation class1: " + this.detailClassName + "(id1: " + this.detailCardId
						+ ") class2: " + this.masterClassName + "(id2: " + this.masterCardId + ")");
				irelation = UserOperations.from(userCtx).relations().create(idomain, cardDetail, cardMaster);
			}
			irelation.setSchema(idomain);
			irelation.save();
		} catch (final CMDBException e) {
			Log.SOAP.error("ExternalSync - exception raised while creating a new relation", e);
			Log.SOAP.debug("Exception parameters" + e.getExceptionParameters());
		}
	}

	private void deleteRelation() {

		Log.SOAP.info("ExternalSync - deleting relation between " + "card [id:" + this.masterCardId + " classname: "
				+ this.masterClassName + "] and " + "card [id:" + detailCardId + " classname: " + this.detailClassName
				+ "] " + "on domain: " + domainName);
		try {

			final ICard cardMaster = UserOperations.from(userCtx).tables().get(this.masterClassName).cards()
					.get(this.masterCardId);
			final ICard cardDetail = UserOperations.from(userCtx).tables().get(detailClassName).cards()
					.get(detailCardId);
			final IDomain idomain = UserOperations.from(userCtx).domains().get(domainName);

			IRelation irelation;
			if (DomainDirected.DIRECTED.getDirection().equals(this.domainDirection.getDirection())) {
				irelation = UserOperations.from(userCtx).relations().get(idomain, cardMaster, cardDetail);
			} else {
				irelation = UserOperations.from(userCtx).relations().get(idomain, cardDetail, cardMaster);
			}
			irelation.delete();

		} catch (final CMDBException e) {
			Log.SOAP.error("ExternalSync - exception raised while creating a new relation", e);
		}
	}

	private boolean detailHasReferenceToMaster() {

		if (referenceToMaster.containsKey(this.detailClassName)) {
			final String referenceName = referenceToMaster.get(this.detailClassName);
			return referenceName != null && !referenceName.equals("");
		} else {
			final ITable table = UserOperations.from(userCtx).tables().get(this.detailClassName);

			final Map<String, IAttribute> attributes = table.getAttributes();
			final Set<String> attributeNames = attributes.keySet();
			for (final String attrName : attributeNames) {
				final IAttribute attribute = attributes.get(attrName);
				final IDomain domain = attribute.getReferenceDomain();
				if (domain != null && domainName.equals(domain.getName())) {
					referenceToMaster.put(this.detailClassName, attrName);
					return true;
				}
			}
			referenceToMaster.put(domainName, "");
			return false;
		}
	}

	private void setReferenceAttribute(final ICard card, final IAttribute cardAttribute, final String attributeValue) {
		final String attributeName = cardAttribute.getName();
		final AttributeValue av = card.getAttributeValue(attributeName);

		if (attributeValue.equals("")) {
			av.setValue(null);
		} else {
			final ITable referencedClass = UserOperations.from(userCtx).tables()
					.get(cardAttribute.getReferenceTarget().getName());

			final String referencedAttributeName = "Description";
			final CardQuery referencedCardList = referencedClass.cards().list()
					.filter(referencedAttributeName, AttributeFilterType.EQUALS, attributeValue).limit(1);

			final Iterator<ICard> cardIterator = referencedCardList.iterator();
			if (cardIterator.hasNext()) {
				final ICard referencedCard = cardIterator.next();
				av.setValue(new Reference(av.getSchema().getReferenceDirectedDomain(), referencedCard.getId(),
						referencedCard.getDescription()));
			} else {
				Log.SOAP.error("ExternalSync - Reference not inserted - No cards found having "
						+ referencedClass.getName() + "." + referencedAttributeName + ": " + attributeValue);
			}
		}
	}

	private void setLookupAttribute(final IAttribute attribute, final ICard card, final String attributeName,
			final String attributeValue) {

		if (attributeValue == null || attributeValue.length() <= 0) {
			Log.SOAP.error("ExternalSync - Setting a null lookup value in: " + attributeName);
			card.getAttributeValue(attributeName).setValue(null);
			return;
		}

		final String lookupType = attribute.getLookupType().getType();
		final LookupOperation lo = new LookupOperation(userCtx);
		final Iterable<Lookup> lookupList = lo.getLookupList(lookupType);

		Lookup lookup = null;

		for (final Lookup l : lookupList) {
			if (attributeValue.equals(l.getDescription())) {
				lookup = l;
				break;
			}
		}

		if (lookup == null) {
			lookup = new Lookup();
			lookup.setType(lookupType);
			lookup.setDescription(attributeValue);
			lookup.save();
		}

		card.getAttributeValue(attributeName).setValue(lookup);
	}

	@SuppressWarnings(value = { "unchecked" })
	private String searchAttributeValue(final String attributeName) {
		final Iterator<Element> attributeIterator = elementCard.elementIterator();
		while (attributeIterator.hasNext()) {
			final Element cardAttribute = attributeIterator.next();
			if (cardAttribute.getName().equals(attributeName)) {
				return cardAttribute.getText();
			}
		}
		return "";
	}
}
