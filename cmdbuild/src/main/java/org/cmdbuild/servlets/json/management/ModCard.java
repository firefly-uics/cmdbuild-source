package org.cmdbuild.servlets.json.management;

import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import org.cmdbuild.common.annotations.CheckIntegration;
import org.cmdbuild.common.annotations.OldDao;
import org.cmdbuild.dao.entry.CMCard;
import org.cmdbuild.elements.Lookup;
import org.cmdbuild.elements.filters.AbstractFilter;
import org.cmdbuild.elements.filters.AttributeFilter;
import org.cmdbuild.elements.filters.AttributeFilter.AttributeFilterType;
import org.cmdbuild.elements.filters.CompositeFilter;
import org.cmdbuild.elements.filters.CompositeFilter.CompositeFilterItem;
import org.cmdbuild.elements.filters.FilterOperator;
import org.cmdbuild.elements.filters.OrderFilter;
import org.cmdbuild.elements.filters.OrderFilter.OrderFilterType;
import org.cmdbuild.elements.interfaces.CardQuery;
import org.cmdbuild.elements.interfaces.IAttribute;
import org.cmdbuild.elements.interfaces.ICard;
import org.cmdbuild.elements.interfaces.IRelation;
import org.cmdbuild.elements.interfaces.ITableFactory;
import org.cmdbuild.elements.interfaces.Process.ProcessAttributes;
import org.cmdbuild.elements.interfaces.RelationFactory;
import org.cmdbuild.exception.CMDBException;
import org.cmdbuild.logic.GISLogic;
import org.cmdbuild.logic.LogicDTO.Card;
import org.cmdbuild.logic.LogicDTO.DomainWithSource;
import org.cmdbuild.logic.TemporaryObjectsBeforeSpringDI;
import org.cmdbuild.logic.commands.GetRelationHistory.GetRelationHistoryResponse;
import org.cmdbuild.logic.commands.GetRelationList.GetRelationListResponse;
import org.cmdbuild.logic.data.DataAccessLogic;
import org.cmdbuild.logic.data.DataAccessLogic.CardDTO;
import org.cmdbuild.logic.data.DataAccessLogic.FetchCardListResponse;
import org.cmdbuild.logic.data.DataAccessLogic.RelationDTO;
import org.cmdbuild.logic.data.QueryOptions;
import org.cmdbuild.services.auth.UserContext;
import org.cmdbuild.servlets.json.JSONBase;
import org.cmdbuild.servlets.json.serializers.CardSerializer;
import org.cmdbuild.servlets.json.serializers.JsonGetRelationHistoryResponse;
import org.cmdbuild.servlets.json.serializers.JsonGetRelationListResponse;
import org.cmdbuild.servlets.json.serializers.Serializer;
import org.cmdbuild.servlets.utils.OverrideKeys;
import org.cmdbuild.servlets.utils.Parameter;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.common.collect.Maps;

public class ModCard extends JSONBase {

	/**
	 * Retrieves the cards for the specified class. If a filter is defined, only
	 * the cards that match the filter are retrieved. The fetched cards are
	 * sorted if a sorter is defined. Note that the max number of retrieved
	 * cards is the 'limit' parameter
	 * 
	 * @param className
	 *            the name of the class for which I want to retrieve the cards
	 * @param filter
	 *            null if no filter is defined. It retrieves all the active
	 *            cards for the specified class that match the filter
	 * @param limit
	 *            max number of retrieved cards (for pagination it is the max
	 *            number of cards in a page)
	 * @param offset
	 *            is the offset from the first card (for pagination)
	 * @param sorters
	 *            null if no sorter is defined
	 */
	@JSONExported
	public JSONObject getCardList(final JSONObject serializer, //
			@Parameter(value = PARAMETER_CLASS_NAME) final String className, //
			@Parameter(value = PARAMETER_FILTER, required = false) final JSONObject filter, //
			@Parameter(PARAMETER_LIMIT) final int limit, //
			@Parameter(PARAMETER_START) final int offset, //
			@Parameter(value = PARAMETER_SORT, required = false) final JSONArray sorters //
	) throws JSONException {

		return getCardList(className, filter, limit, offset, sorters, null);
	}

	/**
	 * Retrieves a list of cards for the specified class, returning only the
	 * values for a subset of values
	 * 
	 * @param filter
	 *            null if no filter is specified
	 * @param sorters
	 *            null if no sorter is specified
	 * @param attributes
	 *            null if all attributes for the specified class are required
	 *            (it is equivalent to the getCardList method)
	 * @return
	 */
	@CheckIntegration
	@JSONExported
	// TODO: check the input parameters and serialization
	public JSONObject getCardListShort(final JSONObject serializer, //
			@Parameter(value = PARAMETER_CLASS_NAME) final String className, //
			@Parameter(PARAMETER_LIMIT) final int limit, //
			@Parameter(PARAMETER_START) final int offset, //
			@Parameter(value = PARAMETER_FILTER, required = false) final JSONObject filter, //
			@Parameter(value = PARAMETER_SORT, required = false) final JSONArray sorters, //
			@Parameter(value = PARAMETER_ATTRIBUTES, required = false) final JSONArray attributes)
			throws JSONException, CMDBException {

		return getCardList(className, filter, limit, offset, sorters, attributes);
	}

	/**
	 * Retrieves the cards for the specified class. If a filter is defined, only
	 * the cards that match the filter are retrieved. The fetched cards are
	 * sorted if a sorter is defined. Note that the max number of retrieved
	 * cards is the 'limit' parameter
	 * 
	 * @param className
	 *            the name of the class for which I want to retrieve the cards
	 * @param filter
	 *            null if no filter is defined. It retrieves all the active
	 *            cards for the specified class that match the filter
	 * @param limit
	 *            max number of retrieved cards (for pagination it is the max
	 *            number of cards in a page)
	 * @param offset
	 *            is the offset from the first card (for pagination)
	 * @param sorters
	 *            null if no sorter is defined
	 */
	@JSONExported
	public JSONObject getDetailList(@Parameter(value = PARAMETER_CLASS_NAME) final String className, //
			@Parameter(value = PARAMETER_FILTER, required = false) final JSONObject filter, //
			@Parameter(PARAMETER_LIMIT) final int limit, //
			@Parameter(PARAMETER_START) final int offset, //
			@Parameter(value = PARAMETER_SORT, required = false) final JSONArray sorters //
	) throws JSONException {

		return getCardList(className, filter, limit, offset, sorters, null);
	}

	private JSONObject getCardList(final String className, final JSONObject filter, final int limit, final int offset,
			final JSONArray sorters, final JSONArray attributes) throws JSONException {
		final DataAccessLogic dataLogic = TemporaryObjectsBeforeSpringDI.getDataAccessLogic();
		final QueryOptions queryOptions = QueryOptions.newQueryOption() //
				.limit(limit) //
				.offset(offset) //
				.orderBy(sorters) //
				.filter(filter) //
				.build();
		final FetchCardListResponse response = dataLogic.fetchCards(className, queryOptions);
		return CardSerializer.toClient(response.getPaginatedCards(), response.getTotalNumberOfCards());
	}

	@CheckIntegration
	@JSONExported
	public JSONObject getCard(@Parameter(value = PARAMETER_CLASS_NAME) final String className,
			@Parameter(value = PARAMETER_CARD_ID) final Long cardId) throws JSONException {
		final DataAccessLogic dataLogic = TemporaryObjectsBeforeSpringDI.getDataAccessLogic();
		final CMCard fetchedCard = dataLogic.fetchCard(className, cardId);

		return CardSerializer.toClient(fetchedCard, "card");
	}

	@OldDao
	@JSONExported
	public JSONObject getCardPosition(
			@Parameter(value = "retryWithoutFilter", required = false) final boolean retryWithoutFilter,
			final JSONObject serializer, final ICard card, final CardQuery currentCardQuery) throws JSONException {
		final CardQuery cardQuery = (CardQuery) currentCardQuery.clone();

		final Lookup flowStatusLookup;
		if (card.getSchema().isActivity()) {
			flowStatusLookup = (Lookup) card.getValue(ProcessAttributes.FlowStatus.dbColumnName());
			serializer.put("FlowStatus", flowStatusLookup.getCode());
		} else {
			flowStatusLookup = null;
		}

		int position = queryPosition(card, cardQuery);

		if (position < 0 && retryWithoutFilter) {
			// Not found in the current filter. Try without it.
			cardQuery.reset();
			if (flowStatusLookup != null) {
				cardQuery.filterUpdate(ProcessAttributes.FlowStatus.toString(), AttributeFilterType.EQUALS,
						flowStatusLookup.getId());
			}
			position = queryPosition(card, cardQuery);
			serializer.put("notFoundInFilter", true);
		}

		serializer.put("position", position);
		return serializer;
	}

	private int queryPosition(final ICard card, final CardQuery cardQuery) {
		removeAttributesNotNeededForPositionQuery(cardQuery);
		return cardQuery.position(card.getId());
	}

	/*
	 * If there is no full text query, we can remove unnecessary attributes
	 */
	private void removeAttributesNotNeededForPositionQuery(final CardQuery cardQuery) {
		final String fullTextQuery = cardQuery.getFullTextQuery();
		if (fullTextQuery == null) {
			final Set<String> attrList = getStandardAttributes(cardQuery);
			addFilterAttributes(cardQuery.getFilter(), attrList);
			addOrderingAttributes(cardQuery, attrList);
			cardQuery.attributes(attrList.toArray(new String[attrList.size()]));
		}
	}

	private Set<String> getStandardAttributes(final CardQuery cardQuery) {
		final Set<String> attrList = new HashSet<String>();
		attrList.add(ICard.CardAttributes.Id.toString());
		attrList.add(ICard.CardAttributes.Status.toString());
		if (cardQuery.getTable().isActivity()) {
			attrList.add(ProcessAttributes.FlowStatus.toString());
		}
		return attrList;
	}

	private void addFilterAttributes(final AbstractFilter abstractFilter, final Set<String> attrList) {
		if (abstractFilter instanceof CompositeFilter) {
			final CompositeFilter compositeFilter = (CompositeFilter) abstractFilter;
			for (final CompositeFilterItem item : compositeFilter.getItems()) {
				addFilterAttributes(item.getFilter(), attrList);
			}
		} else if (abstractFilter instanceof FilterOperator) {
			final FilterOperator filterOperator = (FilterOperator) abstractFilter;
			for (final AbstractFilter filter : filterOperator.getExpressions()) {
				addFilterAttributes(filter, attrList);
			}
		} else if (abstractFilter instanceof AttributeFilter) {
			final AttributeFilter attributeFilter = (AttributeFilter) abstractFilter;
			attrList.add(attributeFilter.getAttributeName());
		}
	}

	private void addOrderingAttributes(final CardQuery cardFilter, final Set<String> attrList) {
		for (final OrderFilter f : cardFilter.getOrdering()) {
			attrList.add(f.getAttributeName());
		}
	}

	// TODO: replace card parameter with className and Id (only if it is a
	// modified card...else id = -1)
	@CheckIntegration
	@JSONExported
	public JSONObject updateCard(final ICard card, final Map<String, Object> attributes, final JSONObject serializer)
			throws Exception {

		final DataAccessLogic dataLogic = TemporaryObjectsBeforeSpringDI.getDataAccessLogic();
		final String className = card.getSchema().getName();
		final CardDTO cardToBeCreatedOrUpdated = new CardDTO(card.getId(), className, attributes);
		final boolean cardMustBeCreated = card.getId() == -1;
		if (cardMustBeCreated) {
			final Long createdCardId = dataLogic.createCard(cardToBeCreatedOrUpdated);
			serializer.put("id", createdCardId);
		} else {
			dataLogic.updateCard(cardToBeCreatedOrUpdated);
		}
		updateGisFeatures(card, attributes);
		return serializer;
	}

	private void updateGisFeatures(final ICard card, final Map<String, Object> attributes) throws Exception {
		final GISLogic gisLogic = TemporaryObjectsBeforeSpringDI.getGISLogic();
		if (gisLogic.isGisEnabled()) {
			gisLogic.updateFeatures(card, attributes);
		}
	}

	@OldDao
	@JSONExported
	public JSONObject updateBulkCards(final Map<String, String> attributes,
			@Parameter(value = "selections", required = false) final String[] cardsToUpdate,
			@Parameter(value = "fullTextQuery", required = false) final String fullTextQuery,
			@Parameter("isInverted") final boolean isInverted,
			@Parameter(value = "confirmed", required = false) final boolean updateConfirmed, CardQuery cardQuery,
			final ITableFactory tf) throws JSONException, CMDBException {
		final JSONObject out = new JSONObject();

		cardQuery = (CardQuery) cardQuery.clone();

		if (fullTextQuery != null) {
			cardQuery.fullText(fullTextQuery.trim());
		}

		final List<ICard> cardsList = buildCardListToBulkUpdate(cardsToUpdate, tf);
		if (isInverted) {
			if (!cardsList.isEmpty()) {
				cardQuery.excludeCards(cardsList);
			}
		} else {
			cardQuery.cards(cardsList);
		}
		cardQuery.clearOrder().subset(0, 0);

		if (updateConfirmed) {
			final ICard card = cardQuery.getTable().cards().create(); // Unprivileged
			// card
			// as a
			// template
			setCardAttributes(card, attributes, true);
			cardQuery.update(card);
		} else {
			cardQuery.count().iterator();
			out.put("count", cardQuery.getTotalRows());
		}

		return out;
	}

	public static void setCardAttributes(final ICard card, final Map<String, String> attributes,
			final Boolean forceChange) {
		for (final IAttribute attribute : card.getSchema().getAttributes().values()) {
			if (!attribute.isDisplayable()) {
				continue;
			}
			final String attrName = attribute.getName();
			final String attrNewValue = attributes.get(attrName);
			if (null != attrNewValue) {
				if (forceChange) {
					card.getAttributeValue(attrName).setValueForceChange(attrNewValue);
				} else {
					card.getAttributeValue(attrName).setValue(attrNewValue);
				}
			}
		}
	}

	private List<ICard> buildCardListToBulkUpdate(final String[] cardsToUpdate, final ITableFactory tf) {
		final List<ICard> cardsList = new LinkedList<ICard>();
		if (cardsToUpdate != null && cardsToUpdate[0] != "") { // if the first
			// element is an
			// empty string
			// the array is empty
			for (final String cardIdAndClass : cardsToUpdate) {
				final ICard cardToUpdate = stringToCard(tf, cardIdAndClass);
				cardsList.add(cardToUpdate);
			}
		}
		return cardsList;
	}

	@CheckIntegration
	@JSONExported
	public void deleteCard(final ICard card) throws JSONException, CMDBException {
		final DataAccessLogic dataLogic = TemporaryObjectsBeforeSpringDI.getDataAccessLogic();
		final String className = card.getSchema().getName();
		dataLogic.deleteCard(className, card.getId());
	}

	@OldDao
	@JSONExported
	public JSONObject deleteDetailCard(final JSONObject serializer, final IRelation relation, @OverrideKeys(key = {
			"Id", "IdClass" }, newKey = { "CardId", "ClassId" }) final ICard detailCard) {

		relation.delete();
		detailCard.delete();
		return serializer;
	}

	@OldDao
	@JSONExported
	public JSONObject getCardHistory(final ICard card, final ITableFactory tf, final RelationFactory rf,
			final UserContext userCtx) throws JSONException, CMDBException {
		if (card.getSchema().isActivity()) {
			return getProcessHistory(new JSONObject(), card, tf);
		}

		final DataAccessLogic dataAccesslogic = applicationContext.getBean(DataAccessLogic.class);
		final Card src = new Card(card.getSchema().getName(), card.getId());
		final GetRelationHistoryResponse out = dataAccesslogic.getRelationHistory(src);
		final JSONObject jsonOutput = new JsonGetRelationHistoryResponse(out).toJson();

		// Old query for card attribute history
		final CardQuery cardQuery = tf.get(card.getIdClass()).cards().list().history(card.getId());
		Serializer.serializeCardAttributeHistory(card, cardQuery, jsonOutput);

		return jsonOutput;
	}

	private JSONObject getProcessHistory(final JSONObject serializer, final ICard card, final ITableFactory tf)
			throws JSONException, CMDBException {
		final CardQuery cardQuery = tf.get(card.getIdClass()).cards().list().history(card.getId())
				.filter("User", AttributeFilterType.DONTCONTAINS, "RemoteApi")
				.filter("User", AttributeFilterType.DONTCONTAINS, "System")
				.order(ICard.CardAttributes.BeginDate.toString(), OrderFilterType.ASC);
		return Serializer.serializeProcessAttributeHistory(card, cardQuery);
	}

	/*
	 * Relations
	 */

	// TODO: replace the parameter card with cardId and className
	@CheckIntegration
	@JSONExported
	public JSONObject getRelationList( //
			@Parameter(value = PARAMETER_CARD_ID) final Long cardId, //
			@Parameter(value = PARAMETER_CLASS_NAME) final String className, //
			@Parameter(value = PARAMETER_DOMAIN_LIMIT, required = false) final int domainlimit, //
			@Parameter(value = PARAMETER_DOMAIN_ID, required = false) final Long domainId, // TODO
			// Use
			// the
			// name
			@Parameter(value = PARAMETER_DOMAIN_SOURCE, required = false) final String querySource)
			throws JSONException {

		final DataAccessLogic dataAccesslogic = TemporaryObjectsBeforeSpringDI.getDataAccessLogic();
		final Card src = new Card(className, cardId);
		final DomainWithSource dom = DomainWithSource.create(domainId, querySource);
		final GetRelationListResponse out = dataAccesslogic.getRelationList(src, dom);
		return new JsonGetRelationListResponse(out, domainlimit).toJson();
	}

	/**
	 * 
	 * @param domainName
	 *            is the domain between the source class and the destination
	 *            class
	 * @param master
	 *            identifies the side of the "parent" card (_1 or _2)
	 * @param attributes
	 *            are the relation attributes and the cards (id and className)
	 *            that will be created. _1 and _2 represents the source and the
	 *            destination cards
	 * @throws JSONException
	 */
	@JSONExported
	@Transacted
	public void createRelations(@Parameter(PARAMETER_DOMAIN_NAME) final String domainName,
			@Parameter(PARAMETER_MASTER) final String master,
			@Parameter(PARAMETER_ATTRIBUTES) final JSONObject attributes) throws JSONException {
		final DataAccessLogic dataAccessLogic = TemporaryObjectsBeforeSpringDI.getDataAccessLogic();
		final RelationDTO relationDTO = new RelationDTO();
		relationDTO.domainName = domainName;
		relationDTO.master = master;
		relationDTO.relationId = null;
		relationDTO.relationAttributeToValue = extractOnlyRelationAttributes(attributes);

		final JSONArray srcCards = attributes.getJSONArray("_1");
		final Map<Long, String> srcCardIdToClassName = extractCards(srcCards);
		relationDTO.srcCardIdToClassName = srcCardIdToClassName;

		final JSONArray dstCards = attributes.getJSONArray("_2");
		final Map<Long, String> dstCardIdToClassName = extractCards(dstCards);
		relationDTO.dstCardIdToClassName = dstCardIdToClassName;

		dataAccessLogic.createRelations(relationDTO);
	}

	@SuppressWarnings("unchecked")
	private Map<String, Object> extractOnlyRelationAttributes(final JSONObject attributes) throws JSONException {
		final Map<String, Object> relationAttributeToValue = Maps.newHashMap();
		final Iterator<String> iterator = attributes.keys();
		while (iterator.hasNext()) {
			final String attributeName = iterator.next();
			if (!attributeName.equals("_1") && !attributeName.equals("_2")) {
				final Object attributeValue = attributes.get(attributeName);
				relationAttributeToValue.put(attributeName, attributeValue);
			}
		}
		return relationAttributeToValue;
	}

	private Map<Long, String> extractCards(final JSONArray cards) throws JSONException {
		final Map<Long, String> cardIdToClassName = Maps.newHashMap();
		for (int i = 0; i < cards.length(); i++) {
			final JSONObject card = cards.getJSONObject(i);
			final Long cardId = card.getLong("cardId");
			final String className = card.getString("className");
			cardIdToClassName.put(cardId, className);
		}
		return cardIdToClassName;
	}

	@CheckIntegration
	@JSONExported
	@Transacted
	public void modifyRelation(@Parameter(PARAMETER_RELATION_ID) final Long relationId,
			@Parameter(PARAMETER_DOMAIN_NAME) final String domainName,
			@Parameter(PARAMETER_MASTER) final String master,
			@Parameter(PARAMETER_ATTRIBUTES) final JSONObject attributes) throws JSONException {
		// saveRelation(JSON, false);
	}

	// private void saveRelation(final JSONObject JSON, final boolean
	// createRelation) throws JSONException {
	// final DataAccessLogic dataAccessLogic =
	// TemporaryObjectsBeforeSpringDI.getDataAccessLogic();
	// RelationDTO relationDTO = buildRelationDTOFromJson(JSON);
	// if (createRelation) {
	// dataAccessLogic.createRelations(relationDTO);
	// } else { // update
	// dataAccessLogic.updateRelation(relationDTO);
	// }
	// }

	// TODO: refactor it!!! too long!
	@SuppressWarnings("unchecked")
	// private RelationDTO buildRelationDTOFromJson(JSONObject jsonRequest)
	// throws JSONException {
	// final Long relationId = jsonRequest.optLong("relationId", -1);
	// final String domainName = jsonRequest.getString("domainName");
	// final String master = jsonRequest.getString("master");
	// final JSONObject attributes = jsonRequest.getJSONObject("attributes");
	// Iterator<String> keyIterator = (Iterator<String>) attributes.keys();
	//
	// Map<String, Object> relationAttributes = Maps.newHashMap();
	// List<Long> srcCardIds = Lists.newArrayList();
	// List<Long> dstCardIds = Lists.newArrayList();
	// JSONArray srcAttributes = null;
	// JSONArray dstAttributes = null;
	// RelationDTO relationDTO = new RelationDTO();
	// relationDTO.master = master;
	// relationDTO.domainName = domainName;
	//
	// while (keyIterator.hasNext()) {
	// String key = keyIterator.next();
	// if (key.equals("_1")) {
	// srcAttributes = attributes.optJSONArray("_1");
	// for (int i = 0; i < srcAttributes.length(); i++) {
	// Long srcCardId = srcAttributes.optJSONObject(i).getLong("cardId");
	// String srcClassName =
	// srcAttributes.optJSONObject(i).getString("className");
	// relationDTO.srcClassName = srcClassName;
	// srcCardIds.add(srcCardId);
	// }
	// relationDTO.srcCardIds = srcCardIds;
	// } else if (key.equals("_2")) {
	// dstAttributes = attributes.optJSONArray("_2");
	// for (int i = 0; i < dstAttributes.length(); i++) {
	// Long dstCardId = dstAttributes.optJSONObject(i).getLong("cardId");
	// String dstClassName =
	// dstAttributes.optJSONObject(i).getString("className");
	// relationDTO.dstClassName = dstClassName;
	// dstCardIds.add(dstCardId);
	// }
	// relationDTO.dstCardIds = dstCardIds;
	// } else {
	// relationAttributes.put(key, attributes.get(key));
	// }
	// }
	// relationDTO.relationAttributeToValue = relationAttributes;
	// if (relationId > 0) {
	// relationDTO.relationId = relationId;
	// }
	// return relationDTO;
	// }
	@OldDao
	@JSONExported
	public void deleteRelation(@Parameter(PARAMETER_RELATION_ID) final Long relationId,
			@Parameter(PARAMETER_DOMAIN_NAME) final String domainName,
			@Parameter(PARAMETER_ATTRIBUTES) final JSONObject attributes, final UserContext userCtx)
			throws JSONException {
		// if (oldWayOfIdentifyingARelation == null) {
		// final int relId = JSON.optInt("id");
		// final int domainId = JSON.getInt("did");
		// final IDomain domain =
		// UserOperations.from(userCtx).domains().get(domainId);
		// UserOperations.from(userCtx).relations().get(domain, relId).delete();
		// } else {
		// oldWayOfIdentifyingARelation.delete();
		// }
	}

	private static ICard stringToCard(final ITableFactory tf, final String string) {
		final StringTokenizer st = new StringTokenizer(string, "_");
		final int classId = Integer.parseInt(st.nextToken());
		final int cardId = Integer.parseInt(st.nextToken());
		return tf.get(classId).cards().get(cardId);
	}

}
