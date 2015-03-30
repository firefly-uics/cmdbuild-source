package org.cmdbuild.servlets.json.management;

import static com.google.common.base.Predicates.in;
import static com.google.common.base.Predicates.not;
import static com.google.common.collect.FluentIterable.from;
import static com.google.common.collect.Iterables.isEmpty;
import static org.cmdbuild.common.Constants.DESCRIPTION_ATTRIBUTE;
import static org.cmdbuild.common.Constants.ID_ATTRIBUTE;
import static org.cmdbuild.servlets.json.CommunicationConstants.ATTRIBUTES;
import static org.cmdbuild.servlets.json.CommunicationConstants.CARD;
import static org.cmdbuild.servlets.json.CommunicationConstants.CARDS;
import static org.cmdbuild.servlets.json.CommunicationConstants.CARD_ID;
import static org.cmdbuild.servlets.json.CommunicationConstants.CLASS_NAME;
import static org.cmdbuild.servlets.json.CommunicationConstants.CONFIRMED;
import static org.cmdbuild.servlets.json.CommunicationConstants.COUNT;
import static org.cmdbuild.servlets.json.CommunicationConstants.DESCRIPTION;
import static org.cmdbuild.servlets.json.CommunicationConstants.DETAIL_CARD_ID;
import static org.cmdbuild.servlets.json.CommunicationConstants.DETAIL_CLASS_NAME;
import static org.cmdbuild.servlets.json.CommunicationConstants.DOMAIN_DIRECTION;
import static org.cmdbuild.servlets.json.CommunicationConstants.DOMAIN_ID;
import static org.cmdbuild.servlets.json.CommunicationConstants.DOMAIN_LIMIT;
import static org.cmdbuild.servlets.json.CommunicationConstants.DOMAIN_NAME;
import static org.cmdbuild.servlets.json.CommunicationConstants.DOMAIN_SOURCE;
import static org.cmdbuild.servlets.json.CommunicationConstants.FILTER;
import static org.cmdbuild.servlets.json.CommunicationConstants.FUNCTION;
import static org.cmdbuild.servlets.json.CommunicationConstants.ID;
import static org.cmdbuild.servlets.json.CommunicationConstants.LIMIT;
import static org.cmdbuild.servlets.json.CommunicationConstants.MASTER;
import static org.cmdbuild.servlets.json.CommunicationConstants.MASTER_CARD_ID;
import static org.cmdbuild.servlets.json.CommunicationConstants.MASTER_CLASS_NAME;
import static org.cmdbuild.servlets.json.CommunicationConstants.OUT_OF_FILTER;
import static org.cmdbuild.servlets.json.CommunicationConstants.PARAMS;
import static org.cmdbuild.servlets.json.CommunicationConstants.POSITION;
import static org.cmdbuild.servlets.json.CommunicationConstants.RELATION_ID;
import static org.cmdbuild.servlets.json.CommunicationConstants.RETRY_WITHOUT_FILTER;
import static org.cmdbuild.servlets.json.CommunicationConstants.SORT;
import static org.cmdbuild.servlets.json.CommunicationConstants.START;
import static org.cmdbuild.servlets.json.CommunicationConstants.STATE;
import static org.cmdbuild.servlets.json.schema.Utils.toIterable;
import static org.cmdbuild.servlets.json.schema.Utils.toMap;
import static org.cmdbuild.workflow.ProcessAttributes.FlowStatus;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.cmdbuild.dao.entry.CMCard;
import org.cmdbuild.dao.entry.LookupValue;
import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.dao.entrytype.CMDomain;
import org.cmdbuild.data.store.lookup.Lookup;
import org.cmdbuild.exception.CMDBException;
import org.cmdbuild.exception.ConsistencyException;
import org.cmdbuild.exception.NotFoundException;
import org.cmdbuild.logic.GISLogic;
import org.cmdbuild.logic.commands.GetCardHistory.GetCardHistoryResponse;
import org.cmdbuild.logic.commands.GetRelationHistory.GetRelationHistoryResponse;
import org.cmdbuild.logic.commands.GetRelationList.DomainWithSource;
import org.cmdbuild.logic.commands.GetRelationList.GetRelationListResponse;
import org.cmdbuild.logic.data.QueryOptions;
import org.cmdbuild.logic.data.QueryOptions.QueryOptionsBuilder;
import org.cmdbuild.logic.data.access.CMCardWithPosition;
import org.cmdbuild.logic.data.access.DataAccessLogic;
import org.cmdbuild.logic.data.access.FetchCardListResponse;
import org.cmdbuild.logic.data.access.RelationDTO;
import org.cmdbuild.logic.mapping.json.JsonFilterHelper;
import org.cmdbuild.model.data.Card;
import org.cmdbuild.services.json.dto.JsonResponse;
import org.cmdbuild.servlets.json.JSONBaseWithSpringContext;
import org.cmdbuild.servlets.json.serializers.JsonGetRelationHistoryResponse;
import org.cmdbuild.servlets.json.serializers.JsonGetRelationListResponse;
import org.cmdbuild.servlets.json.serializers.Serializer;
import org.cmdbuild.servlets.json.util.FlowStatusFilterElementGetter;
import org.cmdbuild.servlets.utils.Parameter;
import org.codehaus.jackson.annotate.JsonProperty;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class ModCard extends JSONBaseWithSpringContext {

	private static class FilterAttribute implements Function<Card, Card> {

		private final Iterable<String> whitelist;

		public FilterAttribute(final Iterable<String> whitelist) {
			this.whitelist = whitelist;
		}

		@Override
		public Card apply(final Card input) {
			if (!isEmpty(whitelist)) {
				final Collection<String> collection = Lists.newArrayList(whitelist);
				final Map<String, Object> map = input.getAttributes();
				final Map<String, Object> filteredMap = Maps.filterKeys(map, not(in(collection)));
				final Collection<String> removed = Lists.newArrayList(filteredMap.keySet());
				for (final String remove : removed) {
					map.remove(remove);
				}
			}
			return input;
		}

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
	public JSONObject getCardList( //
			final JSONObject serializer, //
			@Parameter(value = CLASS_NAME, required = false) final String className, //
			@Parameter(value = FILTER, required = false) final JSONObject filter, //
			@Parameter(LIMIT) final int limit, //
			@Parameter(START) final int offset, //
			@Parameter(value = SORT, required = false) final JSONArray sorters, //
			final Map<String, Object> otherAttributes //
	) throws JSONException {
		return getCardList(className, filter, limit, offset, sorters, null, otherAttributes);
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
	@JSONExported
	// TODO: check the input parameters and serialization
	public JSONObject getCardListShort( //
			final JSONObject serializer, //
			@Parameter(value = CLASS_NAME) final String className, //
			@Parameter(LIMIT) final int limit, //
			@Parameter(START) final int offset, //
			@Parameter(value = FILTER, required = false) final JSONObject filter, //
			@Parameter(value = SORT, required = false) final JSONArray sorters, //
			@Parameter(value = ATTRIBUTES, required = false) final JSONArray attributes, //
			final Map<String, Object> otherAttributes //
	) throws JSONException, CMDBException {
		JSONArray attributesToSerialize = new JSONArray();
		if (attributes == null || attributes.length() == 0) {
			attributesToSerialize.put(DESCRIPTION_ATTRIBUTE);
			attributesToSerialize.put(ID_ATTRIBUTE);
		} else {
			attributesToSerialize = attributes;
		}

		return getCardList(className, filter, limit, offset, sorters, attributesToSerialize, otherAttributes);
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
	public JSONObject getDetailList( //
			@Parameter(value = CLASS_NAME) final String className, //
			@Parameter(value = FILTER, required = false) final JSONObject filter, //
			@Parameter(LIMIT) final int limit, //
			@Parameter(START) final int offset, //
			@Parameter(value = SORT, required = false) final JSONArray sorters, //
			final Map<String, Object> otherAttributes //
	) throws JSONException {

		final DataAccessLogic dataLogic = userDataAccessLogic();
		final QueryOptionsBuilder queryOptionsBuilder = QueryOptions.newQueryOption() //
				.limit(limit) //
				.offset(offset) //
				.orderBy(sorters) //
				.parameters(otherAttributes) //
				.filter(filter); //

		final QueryOptions queryOptions = queryOptionsBuilder.build();
		final FetchCardListResponse response = dataLogic.fetchCards(className, queryOptions);
		return cardSerializer().toClient(response.elements(), response.totalSize());
	}

	private JSONObject getCardList(final String className, final JSONObject filter, final int limit, final int offset,
			final JSONArray sorters, final JSONArray attributes, final Map<String, Object> otherAttributes)
			throws JSONException {

		final DataAccessLogic dataLogic = userDataAccessLogic();
		final QueryOptionsBuilder queryOptionsBuilder = QueryOptions.newQueryOption() //
				.limit(limit) //
				.offset(offset) //
				.orderBy(sorters) //
				.onlyAttributes(toIterable(attributes)) //
				.parameters(otherAttributes) //
				.filter(filter);

		final QueryOptions queryOptions = queryOptionsBuilder.build();
		final FetchCardListResponse response = dataLogic.fetchCards(className, queryOptions);
		return cardSerializer().toClient(removeUnwantedAttributes(response.elements(), attributes),
				response.totalSize());
	}

	private Iterable<Card> removeUnwantedAttributes(final Iterable<Card> elements, final JSONArray attributes) {
		return from(elements) //
				.transform(new FilterAttribute(toIterable(attributes)));
	}

	@JSONExported
	public JSONObject getSQLCardList( //
			final @Parameter(FUNCTION) String functionName, //
			final @Parameter(START) int offset, //
			final @Parameter(LIMIT) int limit, //
			final @Parameter(value = PARAMS, required = false) JSONObject jsonParameters, //
			final @Parameter(value = FILTER, required = false) JSONObject filter, //
			final @Parameter(value = SORT, required = false) JSONArray sorters //
	) throws JSONException { //
		final QueryOptions queryOptions = QueryOptions.newQueryOption() //
				.limit(limit) //
				.offset(offset) //
				.orderBy(sorters) //
				.filter(filter) //
				.parameters(toMap(jsonParameters)) //
				.build();

		final FetchCardListResponse response = systemDataAccessLogic().fetchSQLCards(functionName, queryOptions);
		return cardSerializer().toClient(response.elements(), response.totalSize(), CARDS);
	}

	@JSONExported
	public JSONObject getCard( //
			@Parameter(value = CLASS_NAME) final String className, //
			@Parameter(value = CARD_ID) final Long cardId //
	) throws JSONException {
		final DataAccessLogic dataLogic = userDataAccessLogic();
		final Card fetchedCard = dataLogic.fetchCard(className, cardId);

		return cardSerializer().toClient(fetchedCard, CARD);
	}

	@JSONExported
	public JSONObject getCardPosition( //
			@Parameter(value = RETRY_WITHOUT_FILTER, required = false) final boolean retryWithoutFilter, //
			@Parameter(value = CLASS_NAME) final String className, //
			@Parameter(value = CARD_ID) final Long cardId, //
			@Parameter(value = FILTER, required = false) final JSONObject filter, //
			@Parameter(value = SORT, required = false) final JSONArray sorters, //
			@Parameter(value = STATE, required = false) final String flowStatus //
	) throws JSONException {
		final JSONObject out = new JSONObject();
		final DataAccessLogic dataAccessLogic = userDataAccessLogic();

		QueryOptionsBuilder queryOptionsBuilder = QueryOptions.newQueryOption();
		addFilterToQueryOption(new JsonFilterHelper(filter) //
				.merge(new FlowStatusFilterElementGetter(lookupHelper(), flowStatus)), queryOptionsBuilder);
		addSortersToQueryOptions(sorters, queryOptionsBuilder);

		CMCardWithPosition card = dataAccessLogic.getCardPosition(className, cardId, queryOptionsBuilder.build());

		if (card.position < 0 && retryWithoutFilter) {
			out.put(OUT_OF_FILTER, true);
			queryOptionsBuilder = QueryOptions.newQueryOption();
			final CMCard expectedCard = dataAccessLogic.fetchCMCard(className, cardId);
			final String flowStatusForExpectedCard = flowStatus(expectedCard);
			if (flowStatusForExpectedCard != null) {
				addFilterToQueryOption(new JsonFilterHelper(new JSONObject()) //
						.merge(new FlowStatusFilterElementGetter(lookupHelper(), flowStatusForExpectedCard)),
						queryOptionsBuilder);
			}
			addSortersToQueryOptions(sorters, queryOptionsBuilder);
			card = dataAccessLogic.getCardPosition(className, expectedCard.getId(), queryOptionsBuilder.build());
		}

		out.put(POSITION, card.position);
		/*
		 * FIXME It's late. We need the flow status if ask for a process
		 * position. Do it in a better way!
		 */
		if (card.card != null) {
			final Object retrievedFlowStatus = card.card.get(FlowStatus.dbColumnName());
			if (retrievedFlowStatus != null) {
				final Lookup lookupFlowStatus = lookupLogic().getLookup(((LookupValue) retrievedFlowStatus).getId());
				out.put("FlowStatus", lookupFlowStatus.code);
			}
		}

		return out;
	}

	private String flowStatus(final CMCard card) {
		final Object retrievedFlowStatus = card.get(FlowStatus.dbColumnName());
		if (retrievedFlowStatus != null) {
			final Lookup lookupFlowStatus = lookupLogic().getLookup(((LookupValue) retrievedFlowStatus).getId());
			return lookupFlowStatus.code;
		} else {
			return null;
		}
	}

	private void addFilterToQueryOption(final JSONObject filter, final QueryOptionsBuilder queryOptionsBuilder) {
		if (filter != null) {
			queryOptionsBuilder.filter(filter);
		}
	}

	private void addSortersToQueryOptions(final JSONArray sorters, final QueryOptionsBuilder queryOptionsBuilder) {
		if (sorters != null) {
			queryOptionsBuilder.orderBy(sorters); //
		}
	}

	@JSONExported
	public JSONObject updateCard( //
			@Parameter(value = CLASS_NAME) final String className, //
			@Parameter(value = CARD_ID) Long cardId, //
			final Map<String, Object> attributes //
	) throws Exception {
		final JSONObject out = new JSONObject();
		final DataAccessLogic dataLogic = userDataAccessLogic();
		final Card cardToBeCreatedOrUpdated = Card.newInstance() //
				.withClassName(className) //
				.withId(cardId) //
				.withUser(operationUser().getAuthenticatedUser().getUsername()) //
				.withAllAttributes(attributes) //
				.build();

		final boolean cardMustBeCreated = cardId == -1;

		if (cardMustBeCreated) {
			cardId = dataLogic.createCard(cardToBeCreatedOrUpdated);
			out.put("id", cardId);
		} else {
			try {
				dataLogic.updateCard(cardToBeCreatedOrUpdated);
			} catch (final ConsistencyException e) {
				notifier().warn(e);
				out.put("success", false);
			}
		}

		try {
			final Card card = dataLogic.fetchCard(className, cardId);
			updateGisFeatures(card, attributes);
		} catch (final NotFoundException ex) {
			logger.warn("The card with id " + cardId
					+ " is not present in the database or the logged user can not see it");
		}

		return out;
	}

	private void updateGisFeatures(final Card card, final Map<String, Object> attributes) throws Exception {
		final GISLogic gisLogic = gisLogic();
		if (gisLogic.isGisEnabled()) {
			gisLogic.updateFeatures(card, attributes);
		}
	}

	@JSONExported
	public JSONObject bulkUpdate( //
			final Map<String, Object> attributes, //
			@Parameter(value = CARDS, required = false) final JSONArray cards, //
			@Parameter(value = CONFIRMED, required = false) final boolean confirmed //
	) throws JSONException {
		final JSONObject out = new JSONObject();
		if (!confirmed) { // needs confirmation from user
			return out.put(COUNT, cards.length());
		}
		final DataAccessLogic dataLogic = userDataAccessLogic();
		final Map<Long, String> cardIdToClassName = extractCardsFromJsonArray(cards);
		attributes.remove(CARDS);
		attributes.remove(CONFIRMED);
		for (final Entry<Long, String> entry : cardIdToClassName.entrySet()) {
			final Card cardToUpdate = Card.newInstance() //
					.withId(entry.getKey()) //
					.withClassName(entry.getValue()).withAllAttributes(attributes) //
					.withUser(operationUser().getAuthenticatedUser().getUsername()) //
					.build();
			dataLogic.updateCard(cardToUpdate);
		}
		return out;
	}

	@JSONExported
	public JSONObject bulkUpdateFromFilter( //
			final Map<String, Object> attributes, //
			@Parameter(value = CLASS_NAME, required = false) final String className, //
			@Parameter(value = CARDS, required = false) final JSONArray cards, //
			@Parameter(value = FILTER, required = false) final JSONObject filter, //
			@Parameter(value = CONFIRMED, required = false) final boolean confirmed //
	) throws JSONException {
		final JSONObject out = new JSONObject();
		final DataAccessLogic dataLogic = userDataAccessLogic();
		final QueryOptions queryOptions = QueryOptions.newQueryOption() //
				.filter(filter) //
				.build();
		final FetchCardListResponse response = dataLogic.fetchCards(className, queryOptions);
		if (!confirmed) {
			final int numberOfCardsToUpdate = response.totalSize() - cards.length();
			return out.put(COUNT, numberOfCardsToUpdate);
		}
		final Iterable<Card> fetchedCards = response.elements();
		attributes.remove(CLASS_NAME);
		attributes.remove(CARDS);
		attributes.remove(FILTER);
		attributes.remove(CONFIRMED);
		for (final Card cardToUpdate : fetchedCards) {
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

	@JSONExported
	public JSONObject deleteCard( //
			@Parameter(value = "Id") final Long cardId, @Parameter(value = "IdClass") final Long classId)
			throws JSONException, CMDBException {
		final JSONObject out = new JSONObject();
		final DataAccessLogic dataLogic = userDataAccessLogic();
		final CMClass found = dataLogic.findClass(classId);
		if (found == null) {
			throw NotFoundException.NotFoundExceptionType.CLASS_NOTFOUND.createException(classId.toString());
		}
		final String className = found.getIdentifier().getLocalName();
		dataLogic.deleteCard(className, cardId);

		return out;
	}

	@JSONExported
	public JSONObject getCardHistory(//
			@Parameter(value = CLASS_NAME) final String className, //
			@Parameter(value = CARD_ID) final Long cardId //
	) throws JSONException {

		final DataAccessLogic dataAccessLogic = userDataAccessLogic();
		final CMClass targetClass = dataAccessLogic.findClass(className);
		final Card activeCard = dataAccessLogic.fetchCard(className, Long.valueOf(cardId));
		final Card src = Card.newInstance() //
				.withClassName(className) //
				.withId(activeCard.getId()) //
				.build();
		final GetRelationHistoryResponse relationResponse = dataAccessLogic.getRelationHistory(src);
		final JSONObject jsonRelations = new JsonGetRelationHistoryResponse(relationResponse).toJson();

		final GetCardHistoryResponse responseContainingOnlyUpdatedCards = dataAccessLogic.getCardHistory(src);
		Serializer.serializeCardAttributeHistory( //
				targetClass, //
				activeCard, //
				responseContainingOnlyUpdatedCards, //
				jsonRelations);

		return jsonRelations;
	}

	/*
	 * Relations
	 */

	@JSONExported
	public JSONObject getRelationList( //
			@Parameter(value = CARD_ID) final Long cardId, //
			@Parameter(value = CLASS_NAME) final String className, //
			@Parameter(value = DOMAIN_LIMIT, required = false) final int domainlimit, //
			@Parameter(value = DOMAIN_ID, required = false) final Long domainId, //
			@Parameter(value = DOMAIN_SOURCE, required = false) final String querySource //
	) throws JSONException {
		final DataAccessLogic dataAccesslogic = userDataAccessLogic();
		final Card src = Card.newInstance() //
				.withClassName(className) //
				.withId(cardId) //
				.build();
		final DomainWithSource dom = DomainWithSource.create(domainId, querySource);
		final GetRelationListResponse out = dataAccesslogic.getRelationListEmptyForWrongId(src, dom);
		return new JsonGetRelationListResponse(out, domainlimit, relationAttributeSerializer()).toJson();
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
	public void createRelations( //
			@Parameter(DOMAIN_NAME) final String domainName, //
			@Parameter(MASTER) final String master, //
			@Parameter(ATTRIBUTES) final JSONObject attributes //
	) throws JSONException {
		final DataAccessLogic dataAccessLogic = userDataAccessLogic();
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
				final Object attributeValue;
				if (attributes.isNull(attributeName)) {
					attributeValue = null;
				} else {
					attributeValue = attributes.get(attributeName);
				}
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
	public void modifyRelation( //
			@Parameter(RELATION_ID) final Long relationId, //
			@Parameter(DOMAIN_NAME) final String domainName, //
			@Parameter(MASTER) final String master, //
			@Parameter(ATTRIBUTES) final JSONObject attributes //
	) throws JSONException {
		final DataAccessLogic dataAccessLogic = userDataAccessLogic();

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
	public void deleteRelation( //
			@Parameter(DOMAIN_NAME) final String domainName, //
			@Parameter(RELATION_ID) final Long relationId //
	) throws JSONException {
		final DataAccessLogic dataAccessLogic = userDataAccessLogic();
		dataAccessLogic.deleteRelation(domainName, relationId);
	}

	/*
	 * If a domain name is not send, the detail is given by a foreign key
	 * attribute, so delete directly the card
	 */
	@JSONExported
	public void deleteDetail( //
			@Parameter(value = DETAIL_CLASS_NAME) final String detailClassName, //
			@Parameter(value = DETAIL_CARD_ID) final Long detailCardId, //
			@Parameter(value = MASTER_CLASS_NAME, required = false) final String masterClassName, //
			@Parameter(value = MASTER_CARD_ID, required = false) final Long masterCardId, //
			@Parameter(value = DOMAIN_NAME, required = false) final String domainName //
	) {

		final DataAccessLogic dataLogic = userDataAccessLogic();
		if (domainName != null) {
			final Card detail = Card.newInstance().withClassName(detailClassName).withId(detailCardId).build();
			final Card master = Card.newInstance().withClassName(masterClassName).withId(masterCardId).build();
			dataLogic.deleteDetail(master, detail, domainName);
		} else {
			dataLogic.deleteCard(detailClassName, detailCardId);
		}
	}

	@JSONExported
	public JSONObject lockCard(@Parameter(value = ID) final Long cardId //
	) throws JSONException { //

		final JSONObject out = new JSONObject();
		final DataAccessLogic dataLogic = userDataAccessLogic();

		try {
			dataLogic.lockCard(cardId);
		} catch (final ConsistencyException e) {
			notifier().warn(e);
			out.put("success", false);
		}

		return out;
	}

	@JSONExported
	public void unlockCard(@Parameter(value = ID) final Long cardId //
	) { //
		final DataAccessLogic dataLogic = userDataAccessLogic();
		dataLogic.unlockCard(cardId);
	}

	@Admin
	@JSONExported
	public void unlockAllCards() {
		final DataAccessLogic dataLogic = userDataAccessLogic();
		dataLogic.unlockAllCards();
	}

	@JSONExported
	public JsonResponse getAlreadyRelatedCards( //
			@Parameter(value = DOMAIN_NAME) final String domainName, //
			@Parameter(value = DOMAIN_DIRECTION) final String domainDirection, //
			@Parameter(value = CLASS_NAME) final String className, //
			@Parameter(value = CARDS) final JSONArray cardsIdArray //
	) throws JSONException {
		final DataAccessLogic dataLogic = userDataAccessLogic();
		final CMDomain domain = dataLogic.findDomain(domainName);

		final DomainWithSource dom = DomainWithSource.create(domain.getId(), domainDirection);

		final Predicate<Card> isCardAlreadyRelated = new Predicate<Card>() {
			@Override
			public boolean apply(final Card input) {
				final GetRelationListResponse relationList = dataLogic.getRelationList(input, dom);
				return relationList.getTotalNumberOfRelations() > 0;
			}
		};

		final Collection<Card> cardsToCheck = Lists.newArrayList();
		for (int i = 0; i < cardsIdArray.length(); i++) {
			final Card card = dataLogic.fetchCard(className, Long.parseLong(String.valueOf(cardsIdArray.get(i))));
			cardsToCheck.add(card);
		}
		final Iterable<Card> alreadyRelatedCards = Iterables.filter(cardsToCheck, isCardAlreadyRelated);
		final Iterable<JsonCard> alreadyRelatedJsonCards = from(alreadyRelatedCards) //
				.transform(CARD_TO_JSONCARD).toList();
		return JsonResponse.success(alreadyRelatedJsonCards);
	}

	private static Function<Card, JsonCard> CARD_TO_JSONCARD = new Function<Card, JsonCard>() {

		@Override
		public JsonCard apply(final Card input) {
			return new JsonCard(input);
		}
	};

	private static class JsonCard {

		private Long id;
		private String className;
		private String description;

		public JsonCard(final Card card) {
			this.id = card.getId();
			this.className = card.getClassName();
			this.description = String.class.cast(card.getAttribute(DESCRIPTION_ATTRIBUTE));
		}

		@JsonProperty(ID)
		public Long getId() {
			return id;
		}

		@JsonProperty(DESCRIPTION)
		public String getDescription() {
			return description;
		}

		@JsonProperty(CLASS_NAME)
		public String getClassName() {
			return className;
		}

		@JsonProperty(ID)
		public void setId(final Long id) {
			this.id = id;
		}

		@JsonProperty(CLASS_NAME)
		public void setClassName(final String className) {
			this.className = className;
		}

		@JsonProperty(DESCRIPTION)
		public void setDescription(final String description) {
			this.description = description;
		}
	}

}
