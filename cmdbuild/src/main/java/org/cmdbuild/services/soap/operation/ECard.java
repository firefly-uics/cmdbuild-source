package org.cmdbuild.services.soap.operation;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.cmdbuild.elements.filters.AttributeFilter.AttributeFilterType;
import org.cmdbuild.elements.filters.OrderFilter.OrderFilterType;
import org.cmdbuild.elements.interfaces.CardQuery;
import org.cmdbuild.elements.interfaces.IAttribute;
import org.cmdbuild.elements.interfaces.ICard;
import org.cmdbuild.elements.interfaces.ITable;
import org.cmdbuild.elements.interfaces.BaseSchema.Mode;
import org.cmdbuild.elements.wrappers.PrivilegeCard.PrivilegeType;
import org.cmdbuild.exception.ORMException;
import org.cmdbuild.logger.Log;
import org.cmdbuild.services.auth.UserContext;
import org.cmdbuild.services.meta.MetadataService;
import org.cmdbuild.services.soap.structure.AttributeSchema;
import org.cmdbuild.services.soap.types.Attribute;
import org.cmdbuild.services.soap.types.CQLParameter;
import org.cmdbuild.services.soap.types.CQLQuery;
import org.cmdbuild.services.soap.types.Card;
import org.cmdbuild.services.soap.types.CardList;
import org.cmdbuild.services.soap.types.Metadata;
import org.cmdbuild.services.soap.types.Order;
import org.cmdbuild.services.soap.types.Query;
import org.cmdbuild.services.soap.types.Reference;
import org.cmdbuild.utils.CQLFacadeCompiler;
import org.cmdbuild.workflow.operation.ActivityDO;
import org.cmdbuild.workflow.operation.SharkFacade;

/**
 * Effective SOAP Card implementation
 */
public class ECard {

	private final UserContext userCtx;

	public ECard(final UserContext userCtx) {
		this.userCtx = userCtx;
	}

	public CardList getCardList(final String className, final Attribute[] attributeList, final Query queryType,
			final Order[] orderType, final Integer limit, Integer offset, final String fullText, final CQLQuery cqlQuery) {

		final List<Card> list = new LinkedList<Card>();

		CardQuery cardQuery;
		ITable table;
		if (cqlQuery != null) {
			final HashMap<String, Object> cqlParameters = serializeCQLParameters(cqlQuery.getParameters());
			cardQuery = CQLFacadeCompiler.naiveCmbuildCompileSystemUser(cqlQuery.getCqlQuery(), cqlParameters);
			table = cardQuery.getTable();
		} else {
			table = userCtx.tables().get(className);
			cardQuery = table.cards().list();
			if (queryType != null) {
				cardQuery.filter(queryType.toAbstractFilter(table));
			}
		}
		final String fullTextQuery = fullText;
		Log.SOAP.debug("Getting list of " + className + " cards");

		if (offset == null) {
			offset = 0;
		}
		if (limit != null && offset != null && limit.intValue() > 0) {
			cardQuery.subset(offset, limit);
		}

		if (fullTextQuery != null && !"".equals(fullTextQuery)) {
			cardQuery.fullText(fullTextQuery);
		}

		if (orderType != null && orderType.length > 0) {
			cardQuery = addOrder(orderType, cardQuery);
		}

		if (table.isActivity()) {
			Log.SOAP.debug("Requested class derives from Activity");
			cardQuery = applyFilters(userCtx, cardQuery);
		}

		for (final ICard result : cardQuery.count()) {
			final Card card = prepareCard(attributeList, result);
			card.setMetadata(addMetadata(userCtx, card, result, table));
			list.add(card);
		}
		final int count = cardQuery.getTotalRows();

		final CardList clist = new CardList();
		clist.setTotalRows(count);
		clist.setCards(list);

		return clist;
	}

	private HashMap<String, Object> serializeCQLParameters(final List<CQLParameter> parameters) {
		final HashMap<String, Object> cqlParameters = new HashMap<String, Object>();
		if (parameters != null) {
			for (final CQLParameter parameter : parameters) {
				cqlParameters.put(parameter.getKey(), parameter.getValue());
			}
		}
		return cqlParameters;
	}

	private Card prepareCard(final Attribute[] attributeList, final ICard result) {
		Card card;
		if (attributeList != null && attributeList.length > 0 && attributeList[0].getName() != null) {
			card = new Card(result, attributeList);
		} else {
			card = new Card(result);
		}
		return card;
	}

	private CardQuery addOrder(final Order[] orderType, final CardQuery cardQuery) {
		final CardQuery orderedCardQuery = (CardQuery) cardQuery.clone();
		if (orderType[0].getColumnName() != null) {
			Log.SOAP.debug("Ordering result with following condition(s)");
			for (int i = 0; i < orderType.length; i++) {
				orderedCardQuery.order(orderType[i].getColumnName(), Enum.valueOf(OrderFilterType.class, orderType[i]
						.getType()));
			}
		}
		return orderedCardQuery;
	}

	private CardQuery applyFilters(final UserContext userCtx, final CardQuery cardQuery) {
		CardQuery filteredCardQuery = guestFilter(cardQuery, userCtx);
		if (filteredCardQuery == null) {
			cardQuery.setNextExecutorFilter(userCtx);
			filteredCardQuery = cardQuery;
		}
		return filteredCardQuery;
	}

	private CardQuery guestFilter(final CardQuery cardQuery, final UserContext userCtx) {
		if (userCtx.isGuest()) {
			for (final IAttribute attribute : cardQuery.getTable().getAttributes().values()) {
				final TreeMap<String, Object> metadata = attribute.getMetadata();
				String targetAttributeName = null;
				if (metadata.get("org.cmdbuild.portlet.user.id") != null) {
					final String metadataValue = metadata.get("org.cmdbuild.portlet.user.id").toString();
					targetAttributeName = checkMetadataValue(targetAttributeName, metadataValue);
				}
				if (targetAttributeName != null) {
					final CardQuery filteredCardQuery = (CardQuery) cardQuery.clone();
					final ITable userTable = attribute.getReferenceTarget();
					final CardQuery userQuery = userTable.cards().list().filter(targetAttributeName,
							AttributeFilterType.EQUALS, userCtx.getRequestedUsername());
					filteredCardQuery.cardInRelation(attribute.getReferenceDirectedDomain(), userQuery);
					return filteredCardQuery;
				}
			}
		}
		return null;
	}

	private String checkMetadataValue(String targetAttributeName, final String metadataValue) {
		if (metadataValue != null && !metadataValue.equals("") && metadataValue.contains(".")) {
			targetAttributeName = metadataValue.split("\\.")[1];
		} else {
			targetAttributeName = null;
		}
		return targetAttributeName;
	}

	public Card getCard(final String className, final Integer cardId, final Attribute[] attributeList) {

		Card cardType = null;
		final ITable table = userCtx.tables().get(className);
		final ICard card = table.cards().get(cardId);

		Log.SOAP.debug("Getting card " + cardId + " from " + className);
		if (attributeList != null && attributeList.length > 0 && attributeList[0].getName() != null) {
			cardType = new Card(card, attributeList);
		} else {
			cardType = new Card(card);
			// add metadata only if the whole card is requested, to fix loops in
			// shark tools
		}
		cardType.setMetadata(addMetadata(userCtx, cardType, card, table));
		return cardType;
	}

	private List<Metadata> addMetadata(final UserContext userCtx, final Card cardType, final ICard card,
			final ITable table) {
		final List<Metadata> metadataList = new LinkedList<Metadata>();
		if (table.isActivity()) {
			final EAdministration operation = new EAdministration(userCtx);
			final SharkFacade sharkFacade = new SharkFacade(userCtx);
			final ActivityDO activity = sharkFacade.getActivityList(card);
			operation.serializeMetadata(table.getMetadata(), activity);
			final Metadata processIsEditableMetadata = addIsEditableProcessMetadata(userCtx, card);
			if (processIsEditableMetadata != null) {
				metadataList.add(processIsEditableMetadata);
			}
		} else {
			final PrivilegeType privileges = userCtx.privileges().getPrivilege(table);
			final Metadata meta = new Metadata();
			meta.setKey(MetadataService.RUNTIME_PRIVILEGES_KEY);
			meta.setValue(privileges.toString().toLowerCase());
			metadataList.add(meta);
		}

		return metadataList;
	}

	private Metadata addIsEditableProcessMetadata(final UserContext userCtx, final ICard card) {
		final SharkFacade sharkFacade = new SharkFacade(userCtx);
		final ActivityDO activity = sharkFacade.getActivityList(card);
		if (activity != null) {
			final Metadata meta = new Metadata();
			meta.setKey(MetadataService.RUNTIME_PRIVILEGES_KEY);
			if (activity.isEditable()) {
				meta.setValue("write");
			} else {
				meta.setValue("read");
			}
			return meta;
		} else {
			return null;
		}
	}

	public CardList getCardHistory(final String className, final int cardId, final Integer limit, Integer offset) {

		final List<Card> list = new LinkedList<Card>();
		final ITable table = userCtx.tables().get(className);
		final ICard currentICard = table.cards().get(cardId);
		final Card currentCard = new Card(currentICard);
		list.add(currentCard);
		Log.SOAP.debug("Getting history for " + className + " card with id " + cardId);

		final CardQuery cardList = table.cards().list().history(cardId).filter("User",
				AttributeFilterType.DONTCONTAINS, "System").order(ICard.CardAttributes.BeginDate.toString(),
				OrderFilterType.DESC);

		if (offset == null)
			offset = 0;

		if (limit != null && offset != null && limit.intValue() > 0) {
			cardList.subset(offset, limit);
		}

		for (final ICard result : cardList.count()) {
			final Card card = new Card(result);
			list.add(card);
		}

		final CardList clist = new CardList();
		clist.setTotalRows(cardList.getTotalRows());
		clist.setCards(list);

		return clist;
	}

	public int createCard(final Card card) {

		ITable table;
		Log.SOAP.debug("Creating card with classname " + card.getClassName());
		table = userCtx.tables().get(card.getClassName());

		final ICard icard = table.cards().create();
		setCardAttributes(icard, card.getAttributeList());
		icard.save();
		return icard.getId();

	}

	public boolean updateCard(final Card card) {

		ITable table;
		Log.SOAP.debug("Trying to update card " + card.getId());
		Log.SOAP.debug("Updating card with classname " + card.getClassName());
		table = userCtx.tables().get(card.getClassName());

		final ICard icard = table.cards().get(card.getId());
		setCardAttributes(icard, card.getAttributeList());
		icard.save();
		return true;
	}

	public boolean deleteCard(final String className, final int cardId) {
		Log.SOAP.debug("Deleting card " + cardId + "from " + className);
		final ICard card = userCtx.tables().get(className).cards().get(cardId);
		card.delete();
		return true;
	}

	public AttributeSchema[] getAttributeList(final String className) {
		final ITable table = userCtx.tables().get(className);
		Log.SOAP.debug("Getting Attribute Schema for class " + className);
		final Map<String, IAttribute> attributes = table.getAttributes();
		final List<AttributeSchema> list = new LinkedList<AttributeSchema>();
		for (final IAttribute attribute : attributes.values()) {
			if (attribute.getMode().equals(Mode.RESERVED))
				continue;
			if (!attribute.getStatus().isActive())
				continue;
			final EAdministration administration = new EAdministration(userCtx);
			list.add(administration.serialize(attribute));
		}

		AttributeSchema[] attrs = new AttributeSchema[list.size()];
		attrs = list.toArray(attrs);
		return attrs;
	}

	public Reference[] getReference(final String classname, final Query query, final Order[] order,
			final Integer limit, Integer offset, final String fullText) {

		final List<Reference> list = new LinkedList<Reference>();

		final ITable table = userCtx.tables().get(classname);
		CardQuery cardList = table.cards().list();

		if (query != null) {
			cardList.filter(query.toAbstractFilter(table));
		}

		if (offset == null)
			offset = 0;

		if (limit != null && offset != null && limit.intValue() > 0) {
			cardList.subset(offset, limit);
		}

		if (fullText != null && !"".equals(fullText)) {
			cardList.fullText(fullText);
		}

		if (order != null && order.length > 0) {
			cardList = addOrder(order, cardList);
		}

		final CardQuery cardQuery = cardList.count();
		for (final ICard result : cardQuery) {
			final int count = cardQuery.getTotalRows();
			final Reference reference = prepareReference(classname, result, count);
			list.add(reference);
		}

		Reference[] cardarray = new Reference[list.size()];
		cardarray = list.toArray(cardarray);

		return cardarray;
	}

	private Reference prepareReference(final String classname, final ICard result, final int count) {
		final Reference reference = new Reference();
		reference.setId(result.getId());
		reference.setClassname(classname);
		reference.setDescription(result.getDescription());
		reference.setTotalRows(count);
		return reference;
	}

	private void setCardAttributes(final ICard card, final List<Attribute> attributes) {
		if (attributes != null) {
			for (final Attribute attribute : attributes) {
				final String attrName = attribute.getName();
				final String attrNewValue = attribute.getValue();
				if (null != attrNewValue) {
					try {
						card.getAttributeValue(attrName).setValue(attrNewValue);
					} catch (final ORMException e) {
						Log.SOAP.debug("Error setting attribute " + attrName + " with value " + attrNewValue, e
								.fillInStackTrace());
						Log.SOAP.warn("Exception while setting: " + attrName + " = " + attrNewValue, e);
					}
				}
			}
		}
	}
}
