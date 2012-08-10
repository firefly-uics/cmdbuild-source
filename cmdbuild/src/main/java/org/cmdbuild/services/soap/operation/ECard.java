package org.cmdbuild.services.soap.operation;

import static java.lang.String.format;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.cmdbuild.elements.filters.AttributeFilter.AttributeFilterType;
import org.cmdbuild.elements.filters.OrderFilter.OrderFilterType;
import org.cmdbuild.elements.interfaces.BaseSchema.Mode;
import org.cmdbuild.elements.interfaces.CardQuery;
import org.cmdbuild.elements.interfaces.IAttribute;
import org.cmdbuild.elements.interfaces.ICard;
import org.cmdbuild.elements.interfaces.ITable;
import org.cmdbuild.elements.interfaces.Process.ProcessAttributes;
import org.cmdbuild.elements.wrappers.PrivilegeCard.PrivilegeType;
import org.cmdbuild.exception.ORMException;
import org.cmdbuild.logger.Log;
import org.cmdbuild.services.auth.UserContext;
import org.cmdbuild.services.meta.MetadataService;
import org.cmdbuild.services.soap.structure.AttributeSchema;
import org.cmdbuild.services.soap.structure.ClassSchema;
import org.cmdbuild.services.soap.types.Attribute;
import org.cmdbuild.services.soap.types.CQLParameter;
import org.cmdbuild.services.soap.types.CQLQuery;
import org.cmdbuild.services.soap.types.Card;
import org.cmdbuild.services.soap.types.CardExt;
import org.cmdbuild.services.soap.types.CardList;
import org.cmdbuild.services.soap.types.CardListExt;
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

	private final String ACTIVITY_DESCRIPTION = "ActivityDescription";

	private final UserContext userCtx;

	public ECard(final UserContext userCtx) {
		this.userCtx = userCtx;
	}

	public CardList getCardList(final String className, final Attribute[] attributeList, final Query query,
			final Order[] order, final Integer limit, final Integer offset, final String fullText,
			final CQLQuery cqlQuery, final boolean enableLongDateFormat) {

		Log.SOAP.debug("Getting list of " + className + " cards");

		return new AbstractCardListCommand(className, attributeList, query, order, limit, offset, fullText, cqlQuery) {
			CardList getOutput() {
				final CardList cardList = new CardList();
				for (final ICard card : cards) {
					final Card wfCard = prepareCard(attributeList, card, activityMap, enableLongDateFormat);
					wfCard.setMetadata(addMetadata(userCtx, wfCard, card, table));
					cardList.addCard(wfCard);
				}
				cardList.setTotalRows(count);
				return cardList;
			}
		}.getOutput();
	}

	public CardListExt getCardListExt(final String className, final Attribute[] attributeList, final Query query,
			final Order[] order, final Integer limit, final Integer offset, final String fullText,
			final CQLQuery cqlQuery, final boolean enableLongDateFormat) {

		Log.SOAP.debug("Getting list of " + className + " cards");

		return new AbstractCardListCommand(className, attributeList, query, order, limit, offset, fullText, cqlQuery) {
			CardListExt getOutput() {
				final CardListExt cardList = new CardListExt();
				for (final ICard card : cards) {
					final CardExt wfCard = prepareCardExt(attributeList, card, activityMap, enableLongDateFormat);
					wfCard.setMetadata(addMetadata(userCtx, wfCard, card, table));
					cardList.addCard(wfCard);
				}
				cardList.setTotalRows(count);
				return cardList;
			}
		}.getOutput();
	}

	private abstract class AbstractCardListCommand {

		protected final ITable table;
		protected final Integer count;
		protected final List<ICard> cards;
		protected final Map<Integer, ActivityDO> activityMap;

		public AbstractCardListCommand(final String className, final Attribute[] attributeList, final Query query,
			final Order[] order, final Integer limit, final Integer offset, final String fullText,
			final CQLQuery cqlQuery) {
			final CardQueryBuilder cqb = createCardQueryBuilder(className, query, cqlQuery);
			cqb.setPage(limit, offset);
			cqb.setFullText(fullText);
			cqb.setOrder(order);
			cqb.applyActivityFilters(userCtx);
			final CardQuery cardQuery = cqb.getCardQuery();
			this.table = cardQuery.getTable();
			this.cards = new ArrayList<ICard>();
			for (final ICard card : cardQuery.count()) {
				cards.add(card);
			}
			this.count = cardQuery.getTotalRows();
			this.activityMap = getActivityMapIfNeeded(table, cards, attributeList);
		}
	}

	private CardQueryBuilder createCardQueryBuilder(final String className, final Query query, final CQLQuery cqlQuery) {
		return new CardQueryBuilder(userCtx, className, query, cqlQuery);
	}

	private Map<Integer, ActivityDO> getActivityMapIfNeeded(final ITable table, final List<ICard> cards,
			final Attribute[] attributeList) {
		if (attributeList == null) { // Every attribute!
			return getActivityMap(table, cards);
		}
		for (final Attribute a : attributeList) {
			if (ACTIVITY_DESCRIPTION.equals(a.getName())) {
				return getActivityMap(table, cards);
			}
		}
		return new HashMap<Integer, ActivityDO>();
	}

	private Map<Integer, ActivityDO> getActivityMap(final ITable table, final List<ICard> cards) {
		final SharkFacade sharkFacade = new SharkFacade(userCtx);
		return sharkFacade.getActivityMap(table, cards);
	}

	private Card prepareCard(final Attribute[] attributeList, final ICard card,
			final Map<Integer, ActivityDO> activityMap, boolean enableLongDateFormat) {
		Card wfCard;
		final Card.ValueSerializer cardSerializer = Card.ValueSerializer.forLongDateFormat(enableLongDateFormat);
		addActivityDecription(card, activityMap.get(card.getId()));
		if (attributeList != null && attributeList.length > 0 && attributeList[0].getName() != null) {
			wfCard = new Card(card, attributeList, cardSerializer);
		} else {
			wfCard = new Card(card, cardSerializer);
		}
		return wfCard;
	}

	// FIXME Refactoring with unit tests (it's a total mess!)
	private CardExt prepareCardExt(final Attribute[] attributeList, final ICard card,
			final Map<Integer, ActivityDO> activityMap, boolean enableLongDateFormat) {
		CardExt wfCard;
		final Card.ValueSerializer cardSerializer = Card.ValueSerializer.forLongDateFormat(enableLongDateFormat);
		addActivityDecription(card, activityMap.get(card.getId()));
		if (attributeList != null && attributeList.length > 0 && attributeList[0].getName() != null) {
			wfCard = new CardExt(card, attributeList, cardSerializer);
		} else {
			wfCard = new CardExt(card, cardSerializer);
		}
		return wfCard;
	}

	private void addActivityDecription(final ICard card) {
		if (card.getSchema().isActivity()) {
			final SharkFacade sharkFacade = new SharkFacade(userCtx);
			final ActivityDO activityDo = sharkFacade.getActivityList(card);
			addActivityDecription(card, activityDo);
		}
	}

	private void addActivityDecription(final ICard card, final ActivityDO activityDo) {
		if (activityDo != null) {
			final String activityDescription = activityDo.getActivityInfo().getActivityDescription();
			card.setValue(ACTIVITY_DESCRIPTION, activityDescription);
		}
	}

	public Card getCard(final String className, final Integer cardId, final Attribute[] attributeList) {
		Card wfCard;
		final ITable table = table(className);
		final ICard card = table.cards().get(cardId);
		addActivityDecription(card);

		Log.SOAP.debug("Getting card " + cardId + " from " + className);
		if (attributeList != null && attributeList.length > 0 && attributeList[0].getName() != null) {
			wfCard = new Card(card, attributeList);
		} else {
			wfCard = new Card(card);
		}

		wfCard.setMetadata(addMetadata(userCtx, wfCard, card, table));
		return wfCard;
	}

	private List<Metadata> addMetadata(final UserContext userCtx, final Card cardType, final ICard card,
			final ITable table) {
		final List<Metadata> metadataList = new LinkedList<Metadata>();
		if (table.isActivity()) {
			final EAdministration operation = new EAdministration(userCtx);
			final SharkFacade sharkFacade = new SharkFacade(userCtx);
			final ActivityDO activity = sharkFacade.getActivityList(card);
			operation.serializeMetadata(table, activity);
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
		final ITable table = table(className);
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
		final ICard card = table(className).cards().get(cardId);
		card.delete();
		return true;
	}

	public AttributeSchema[] getAttributeList(final String className) {
		Log.SOAP.info(format("getting attributes schema for class '%s'", className));
		final List<AttributeSchema> attributes = getClassSchema(className).getAttributes();
		return attributes.toArray(new AttributeSchema[attributes.size()]);
	}

	public Reference[] getReference(final String classname, final Query query, final Order[] order,
			final Integer limit, final Integer offset, final String fullText, final CQLQuery cqlQuery) {

		final CardQueryBuilder cardQueryBuilder = createCardQueryBuilder(classname, query, cqlQuery);
		cardQueryBuilder.setQuery(query);
		cardQueryBuilder.setPage(limit, offset);
		cardQueryBuilder.setFullText(fullText);
		cardQueryBuilder.setOrder(order);

		final List<Reference> referenceList = new LinkedList<Reference>();
		final CardQuery cardQuery = cardQueryBuilder.getCardQuery().count();
		for (final ICard card : cardQuery) {
			final int count = cardQuery.getTotalRows();
			final Reference reference = prepareReference(classname, card, count);
			referenceList.add(reference);
		}

		final Reference[] array = referenceList.toArray(new Reference[referenceList.size()]);
		return array;
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

	private static class CardQueryBuilder {

		private final ITable table;
		private CardQuery cardQuery;

		public CardQueryBuilder(final UserContext userCtx, final String className, final Query query,
				final CQLQuery cqlQuery) {
			if (cqlQuery != null) {
				final HashMap<String, Object> cqlParameters = serializeCQLParameters(cqlQuery.getParameters());
				cardQuery = CQLFacadeCompiler.naiveCmbuildCompileSystemUser(cqlQuery.getCqlQuery(), cqlParameters);
				table = cardQuery.getTable();
			} else {
				table = userCtx.tables().get(className);
				cardQuery = table.cards().list();
				if (query != null) {
					cardQuery.filter(query.toAbstractFilter(table));
				}
			}
		}

		public CardQuery getCardQuery() {
			return cardQuery;
		}

		public void applyActivityFilters(final UserContext userContext) {
			if (table.isActivity()) {
				cardQuery = applyFilters(userContext, cardQuery);
			}
		}

		public void setOrder(final Order[] order) {
			if (ArrayUtils.isNotEmpty(order)) {
				cardQuery = addOrder(order, cardQuery);
			}
		}

		public void setQuery(final Query query) {
			if (query != null) {
				cardQuery.filter(query.toAbstractFilter(table));
			}
		}

		public void setFullText(final String fullText) {
			if (StringUtils.isNotBlank(fullText)) {
				cardQuery.fullText(fullText);
			}
		}

		public void setPage(final Integer limit, final Integer offset) {
			if (limit != null && limit.intValue() > 0) {
				cardQuery.subset((offset == null) ? 0 : offset, limit);
			}
		}

		private static HashMap<String, Object> serializeCQLParameters(final List<CQLParameter> parameters) {
			final HashMap<String, Object> cqlParameters = new HashMap<String, Object>();
			if (parameters != null) {
				for (final CQLParameter parameter : parameters) {
					cqlParameters.put(parameter.getKey(), parameter.getValue());
				}
			}
			return cqlParameters;
		}

		private CardQuery addOrder(final Order[] orderType, final CardQuery cardQuery) {
			final CardQuery orderedCardQuery = (CardQuery) cardQuery.clone();
			if (orderType[0].getColumnName() != null) {
				Log.SOAP.debug("Ordering result with following condition(s)");
				for (int i = 0; i < orderType.length; i++) {
					orderedCardQuery.order(orderType[i].getColumnName(), Enum.valueOf(OrderFilterType.class,
							orderType[i].getType()));
				}
			}
			return orderedCardQuery;
		}

		private CardQuery applyFilters(final UserContext userCtx, final CardQuery cardQuery) {
			CardQuery filteredCardQuery = guestFilter(cardQuery, userCtx);
			if (filteredCardQuery == null) {
				cardQuery.setPrevExecutorsFilter(userCtx);
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

	}

	public ClassSchema getClassSchema(final String className) {
		Log.SOAP.info(format("getting schema for class '%s'", className));
		final ClassSchema classSchema = new ClassSchema();
		final ITable table = table(className);

		classSchema.setName(table.getName());
		classSchema.setDescription(table.getDescription());
		classSchema.setSuperClass(table.isSuperClass());
	
		final List<AttributeSchema> attributes = new ArrayList<AttributeSchema>();
		for (final IAttribute attribute : table.getAttributes().values()) {
			if (keepAttribute(attribute)) {
				final AttributeSchema attributeSchema = attributeSchema(attribute);
				attributes.add(attributeSchema);
			}
		}
		classSchema.setAttributes(attributes);

		return classSchema;
	}

	private ITable table(final String className) {
		Log.SOAP.info(format("getting table for class '%s'", className));
		return userCtx.tables().get(className);
	}

	private boolean keepAttribute(final IAttribute attribute) {
		final boolean keep;
		if (attribute.getMode().equals(Mode.RESERVED)) {
			keep = false;
		} else if (!attribute.getStatus().isActive()) {
			keep = false;
		} else {
			keep = true;
		}
		Log.SOAP.info(format("attribute '%s' kept: %b", attribute.getName(), keep));
		return keep;
	}

	private AttributeSchema attributeSchema(final IAttribute attribute) {
		Log.SOAP.info(format("serializing attribute '%s'", attribute.getName()));
		final EAdministration administration = new EAdministration(userCtx);
		final AttributeSchema attributeSchema = administration.serialize(attribute);
		return attributeSchema;
	}

}
