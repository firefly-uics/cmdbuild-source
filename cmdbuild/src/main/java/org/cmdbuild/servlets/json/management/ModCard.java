package org.cmdbuild.servlets.json.management;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.cmdbuild.common.annotations.CheckIntegration;
import org.cmdbuild.dao.entry.CMCard;
import org.cmdbuild.elements.interfaces.ICard;
import org.cmdbuild.exception.CMDBException;
import org.cmdbuild.logic.GISLogic;
import org.cmdbuild.logic.LogicDTO.Card;
import org.cmdbuild.logic.LogicDTO.DomainWithSource;
import org.cmdbuild.logic.TemporaryObjectsBeforeSpringDI;
import org.cmdbuild.logic.commands.GetCardHistory.GetCardHistoryResponse;
import org.cmdbuild.logic.commands.GetRelationHistory.GetRelationHistoryResponse;
import org.cmdbuild.logic.commands.GetRelationList.GetRelationListResponse;
import org.cmdbuild.logic.data.DataAccessLogic;
import org.cmdbuild.logic.data.DataAccessLogic.CardDTO;
import org.cmdbuild.logic.data.DataAccessLogic.FetchCardListResponse;
import org.cmdbuild.logic.data.DataAccessLogic.RelationDTO;
import org.cmdbuild.logic.data.QueryOptions;
import org.cmdbuild.servlets.json.JSONBase;
import org.cmdbuild.servlets.json.serializers.CardSerializer;
import org.cmdbuild.servlets.json.serializers.JsonGetRelationHistoryResponse;
import org.cmdbuild.servlets.json.serializers.JsonGetRelationListResponse;
import org.cmdbuild.servlets.json.serializers.Serializer;
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

	@JSONExported
	public JSONObject getCardPosition(
			@Parameter(value = "retryWithoutFilter", required = false) final boolean retryWithoutFilter,
			@Parameter(value = "className", required = true) final String className,
			@Parameter(value = "Id", required = true) final Long cardId, final JSONObject serializer)
			throws JSONException {
		// FIXME: manage the filter...
		// and the sorting
		final DataAccessLogic dataAccessLogic = TemporaryObjectsBeforeSpringDI.getDataAccessLogic();
		final Long position = dataAccessLogic.getCardPosition(className, cardId);
		serializer.put("position", position);
		return serializer;
	}

	// TODO: replace card parameter with className and Id (only if it is a
	// modified card...else id = -1)
	@CheckIntegration
	@JSONExported
	public JSONObject updateCard(final ICard card, final Map<String, Object> attributes, final JSONObject serializer)
			throws Exception {

		final DataAccessLogic dataLogic = TemporaryObjectsBeforeSpringDI.getDataAccessLogic();
		final String className = card.getSchema().getName();
		final CardDTO cardToBeCreatedOrUpdated = new CardDTO(Long.valueOf(card.getId()), className, attributes);
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

	@CheckIntegration
	@JSONExported
	public JSONObject bulkUpdate(final Map<String, Object> attributes,
			@Parameter(value = PARAMETER_CARDS, required = false) final JSONArray cards,
			@Parameter(value = PARAMETER_CONFIRMED, required = false) final boolean confirmed) throws JSONException {
		final JSONObject out = new JSONObject();
		if (!confirmed) { // needs confirmation from user
			return out.put("count", cards.length());
		}
		final DataAccessLogic dataLogic = TemporaryObjectsBeforeSpringDI.getDataAccessLogic();
		final Map<Long, String> cardIdToClassName = extractCardsFromJsonArray(cards);
		attributes.remove(PARAMETER_CARDS);
		attributes.remove(PARAMETER_CONFIRMED);
		for (final Entry<Long, String> entry : cardIdToClassName.entrySet()) {
			final CardDTO cardToUpdate = new CardDTO(entry.getKey(), entry.getValue(), attributes);
			dataLogic.updateCard(cardToUpdate);
		}
		return out;
	}

	@CheckIntegration
	@JSONExported
	public JSONObject bulkUpdateFromFilter(final Map<String, Object> attributes,
			@Parameter(value = PARAMETER_CLASS_NAME, required = false) final String className,
			@Parameter(value = PARAMETER_CARDS, required = false) final JSONArray cards,
			@Parameter(value = PARAMETER_FILTER, required = false) final JSONObject filter,
			@Parameter(value = PARAMETER_CONFIRMED, required = false) final boolean confirmed) throws JSONException {
		final JSONObject out = new JSONObject();
		final DataAccessLogic dataLogic = TemporaryObjectsBeforeSpringDI.getDataAccessLogic();
		final QueryOptions queryOptions = QueryOptions.newQueryOption() //
				.filter(filter) //
				.build();
		final FetchCardListResponse response = dataLogic.fetchCards(className, queryOptions);
		if (!confirmed) {
			final int numberOfCardsToUpdate = response.getTotalNumberOfCards() - cards.length();
			return out.put("count", numberOfCardsToUpdate);
		}
		final Iterable<CMCard> fetchedCards = response.getPaginatedCards();
		attributes.remove(PARAMETER_CLASS_NAME);
		attributes.remove(PARAMETER_CARDS);
		attributes.remove(PARAMETER_FILTER);
		attributes.remove(PARAMETER_CONFIRMED);
		for (final CMCard cardToUpdate : fetchedCards) {
			if (cardNeedToBeUpdated(cards, cardToUpdate.getId())) {
				dataLogic.updateFetchedCard(cardToUpdate, attributes);
			}
		}
		return out;
	}

	private boolean cardNeedToBeUpdated(final JSONArray cardsNotToUpdate, final Long cardId) throws JSONException {
		final Map<Long, String> cardIdToClassName = extractCardsFromJsonArray(cardsNotToUpdate);
		final String className = cardIdToClassName.get(cardId);
		return className == null;
	}

	@CheckIntegration
	@JSONExported
	public void deleteCard(final ICard card) throws JSONException, CMDBException {
		final DataAccessLogic dataLogic = TemporaryObjectsBeforeSpringDI.getDataAccessLogic();
		final String className = card.getSchema().getName();
		dataLogic.deleteCard(className, card.getId());
	}

	// TODO: replace card parameter with className and cardId
	@CheckIntegration
	@JSONExported
	public JSONObject getCardHistory(final ICard card) throws JSONException {

		// FIXME: fix process history...
		// if (card.getSchema().isActivity()) {
		// return getProcessHistory(new JSONObject(), card, tf);
		// }

		final DataAccessLogic dataAccessLogic = TemporaryObjectsBeforeSpringDI.getDataAccessLogic();
		final CMCard activeCard = dataAccessLogic.fetchCard(card.getSchema().getName(), Long.valueOf(card.getId()));
		final Card src = new Card(card.getSchema().getName(), activeCard.getId());

		final GetRelationHistoryResponse relationResponse = dataAccessLogic.getRelationHistory(src);
		final JSONObject jsonRelations = new JsonGetRelationHistoryResponse(relationResponse).toJson();

		final GetCardHistoryResponse responseContainingOnlyUpdatedCards = dataAccessLogic.getCardHistory(src);
		Serializer.serializeCardAttributeHistory(activeCard, responseContainingOnlyUpdatedCards, jsonRelations);

		return jsonRelations;
	}

	// private JSONObject getProcessHistory(final JSONObject serializer, final
	// ICard card, final ITableFactory tf)
	// throws JSONException, CMDBException {
	// final CardQuery cardQuery =
	// tf.get(card.getIdClass()).cards().list().history(card.getId())
	// .filter("User", AttributeFilterType.DONTCONTAINS, "RemoteApi")
	// .filter("User", AttributeFilterType.DONTCONTAINS, "System")
	// .order(ICard.CardAttributes.BeginDate.toString(), OrderFilterType.ASC);
	// return Serializer.serializeProcessAttributeHistory(card, cardQuery);
	// }

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
			@Parameter(value = PARAMETER_DOMAIN_ID, required = false) final Long domainId, //
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
		final Map<Long, String> srcCardIdToClassName = extractCardsFromJsonArray(srcCards);
		relationDTO.srcCardIdToClassName = srcCardIdToClassName;

		final JSONArray dstCards = attributes.getJSONArray("_2");
		final Map<Long, String> dstCardIdToClassName = extractCardsFromJsonArray(dstCards);
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

	private Map<Long, String> extractCardsFromJsonArray(final JSONArray cards) throws JSONException {
		final Map<Long, String> cardIdToClassName = Maps.newHashMap();
		for (int i = 0; i < cards.length(); i++) {
			final JSONObject card = cards.getJSONObject(i);
			final Long cardId = card.getLong("cardId");
			final String className = card.getString("className");
			cardIdToClassName.put(cardId, className);
		}
		return cardIdToClassName;
	}

	@JSONExported
	@Transacted
	public void modifyRelation(@Parameter(PARAMETER_RELATION_ID) final Long relationId,
			@Parameter(PARAMETER_DOMAIN_NAME) final String domainName,
			@Parameter(PARAMETER_MASTER) final String master,
			@Parameter(PARAMETER_ATTRIBUTES) final JSONObject attributes) throws JSONException {
		final DataAccessLogic dataAccessLogic = TemporaryObjectsBeforeSpringDI.getDataAccessLogic();

		final RelationDTO relationDTO = new RelationDTO();
		relationDTO.relationId = relationId;
		relationDTO.domainName = domainName;
		relationDTO.master = master;
		relationDTO.relationAttributeToValue = extractOnlyRelationAttributes(attributes);
		final JSONArray srcCards = attributes.getJSONArray("_1");
		final Map<Long, String> srcCardIdToClassName = extractCardsFromJsonArray(srcCards);
		relationDTO.srcCardIdToClassName = srcCardIdToClassName;
		final JSONArray dstCards = attributes.getJSONArray("_2");
		final Map<Long, String> dstCardIdToClassName = extractCardsFromJsonArray(dstCards);
		relationDTO.dstCardIdToClassName = dstCardIdToClassName;

		dataAccessLogic.updateRelation(relationDTO);
	}

	@JSONExported
	@Transacted
	public void deleteRelation(@Parameter(PARAMETER_RELATION_ID) final Long relationId,
			@Parameter(PARAMETER_DOMAIN_NAME) final String domainName,
			@Parameter(PARAMETER_ATTRIBUTES) final JSONObject attributes) throws JSONException {
		final DataAccessLogic dataAccessLogic = TemporaryObjectsBeforeSpringDI.getDataAccessLogic();

		final RelationDTO relationDTO = new RelationDTO();
		relationDTO.domainName = domainName;
		relationDTO.relationId = relationId;
		final JSONArray srcCards = attributes.getJSONArray("_1");
		final Map<Long, String> srcCardIdToClassName = extractCardsFromJsonArray(srcCards);
		relationDTO.srcCardIdToClassName = srcCardIdToClassName;
		final JSONArray dstCards = attributes.getJSONArray("_2");
		final Map<Long, String> dstCardIdToClassName = extractCardsFromJsonArray(dstCards);
		relationDTO.dstCardIdToClassName = dstCardIdToClassName;

		dataAccessLogic.deleteRelation(relationDTO);
	}
}
