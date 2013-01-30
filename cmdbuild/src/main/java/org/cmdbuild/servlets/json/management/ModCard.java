package org.cmdbuild.servlets.json.management;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import org.cmdbuild.common.annotations.CheckIntegration;
import org.cmdbuild.common.annotations.OldDao;
import org.cmdbuild.dao.entry.CMCard;
import org.cmdbuild.dao.query.clause.QueryDomain.Source;
import org.cmdbuild.elements.DirectedDomain;
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
import org.cmdbuild.elements.interfaces.IDomain;
import org.cmdbuild.elements.interfaces.IRelation;
import org.cmdbuild.elements.interfaces.ITableFactory;
import org.cmdbuild.elements.interfaces.Process.ProcessAttributes;
import org.cmdbuild.elements.interfaces.RelationFactory;
import org.cmdbuild.exception.CMDBException;
import org.cmdbuild.exception.NotFoundException;
import org.cmdbuild.logic.GISLogic;
import org.cmdbuild.logic.LogicDTO;
import org.cmdbuild.logic.LogicDTO.Card;
import org.cmdbuild.logic.LogicDTO.DomainWithSource;
import org.cmdbuild.logic.TemporaryObjectsBeforeSpringDI;
import org.cmdbuild.logic.commands.AbstractGetRelation.RelationInfo;
import org.cmdbuild.logic.commands.GetRelationHistory.GetRelationHistoryResponse;
import org.cmdbuild.logic.commands.GetRelationList.DomainInfo;
import org.cmdbuild.logic.commands.GetRelationList.GetRelationListResponse;
import org.cmdbuild.logic.data.DataAccessLogic;
import org.cmdbuild.logic.data.DataAccessLogic.CardDTO;
import org.cmdbuild.logic.data.DataAccessLogic.FetchCardListResponse;
import org.cmdbuild.logic.data.QueryOptions;
import org.cmdbuild.services.auth.UserContext;
import org.cmdbuild.services.auth.UserOperations;
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

import com.google.common.collect.Lists;

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
			@Parameter(value = "className") final String className, //
			@Parameter(value = "filter", required = false) final JSONObject filter, //
			@Parameter("limit") final int limit, //
			@Parameter("start") final int offset, //
			@Parameter(value = "sort", required = false) final JSONArray sorters //
	) throws JSONException {
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
			@Parameter(value = "className") final String className, //
			@Parameter("limit") final int limit, //
			@Parameter("start") final int offset, //
			@Parameter(value = "filter", required = false) final JSONObject filter, //
			@Parameter(value = "sort", required = false) final JSONArray sorters, //
			@Parameter(value = "attributes", required = false) final JSONArray attributes) throws JSONException,
			CMDBException {

		final DataAccessLogic dataLogic = TemporaryObjectsBeforeSpringDI.getDataAccessLogic();
		final QueryOptions queryOptions = QueryOptions.newQueryOption() //
				.limit(limit) //
				.offset(offset) //
				.orderBy(sorters) //
				.filter(filter) //
				.onlyAttributes(attributes) //
				.build();
		final FetchCardListResponse response = dataLogic.fetchCards(className, queryOptions);
		return CardSerializer.toClient(response.getPaginatedCards(), response.getTotalNumberOfCards());
	}

	/**
	 * Retrieves the list of all the cards in relation with the specified card
	 * that belongs to the specified class.
	 * 
	 * @param sourceClassId
	 *            the id of the class to which the card belongs
	 * @param sourceCardId
	 *            the id of the source card. We want to retrieve all cards
	 *            related to this card over the specified domain
	 * @param limit
	 *            is the page size used in pagination
	 * @param offset
	 *            is the offset from the first result
	 * @param sorters
	 *            is an array of sorters that operate on the cards related to
	 *            the source card
	 * @param fullTextQuery
	 *            is the search filter that operates on the cards related to the
	 *            source card
	 * @param directedDomainParameter
	 *            is the id of the domain between the source class and the
	 *            destination class. It looks like this: 'id_D' or 'id_I'
	 *            depending on the type of relation (direct or inverse
	 *            respectively)
	 */
	@CheckIntegration
	@JSONExported
	public JSONObject getDetailList(final JSONObject serializer, //
			@Parameter("IdClass") final int sourceClassId, //
			@Parameter("Id") final int sourceCardId, //
			@Parameter(value = "DirectedDomain", required = false) final String directedDomainParameter, //
			@Parameter("limit") final int limit, //
			@Parameter("start") final int offset, //
			@Parameter(value = "sort", required = false) final JSONArray sorters, //
			@Parameter(value = "query", required = false) final String fullTextQuery) throws JSONException,
			CMDBException {

		final DataAccessLogic dataLogic = TemporaryObjectsBeforeSpringDI.getDataAccessLogic();
		final DomainWithSource domWithSource = createDomainWithSource(directedDomainParameter);
		final LogicDTO.Card card = new Card(sourceClassId, sourceCardId);

		// TODO: improve it when QueryOptions object won't depend on JSON
		final QueryOptions queryOptions = QueryOptions.newQueryOption() //
				.limit(limit) //
				.offset(offset) //
				.orderBy(sorters) //
				.filter(new JSONObject().put("query", fullTextQuery)) //
				.build();

		final GetRelationListResponse relationListResponse = dataLogic.getRelationList(card, domWithSource,
				queryOptions);
		final List<CMCard> targetCards = Lists.newArrayList();
		if (relationListResponse.iterator().hasNext()) {
			final DomainInfo domainInfo = relationListResponse.iterator().next();
			for (final RelationInfo relationInfo : domainInfo) {
				final CMCard targetCard = relationInfo.getTargetCard();
				targetCards.add(targetCard);
			}
		}
		return CardSerializer.toClient(targetCards, relationListResponse.getTotalNumberOfRelations());
	}

	private static DomainWithSource createDomainWithSource(final String domainIdWithDirection) {
		final StringTokenizer st = new StringTokenizer(domainIdWithDirection, "_");
		final int domainId = Integer.parseInt(st.nextToken());
		final String domainDirection = st.nextToken();
		DomainWithSource domainWithSource = null;
		if (domainDirection.equals("D")) {
			domainWithSource = DomainWithSource.create(Long.valueOf(domainId), Source._1.toString());
		} else { // equals "I"
			domainWithSource = DomainWithSource.create(Long.valueOf(domainId), Source._2.toString());
		}
		return domainWithSource;
	}

	// TODO: replace card with cardId and "IdClass" with className
	@CheckIntegration
	@JSONExported
	public JSONObject getCard(final ICard card, @Parameter("IdClass") final int requestedIdClass,
			final JSONObject serializer) throws JSONException {
		final DataAccessLogic dataLogic = TemporaryObjectsBeforeSpringDI.getDataAccessLogic();
		final CMCard fetchedCard = dataLogic.fetchCard(card.getSchema().getName(), card.getId());
		// TODO: check serialization...
		return CardSerializer.toClient(fetchedCard);
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
		String className = card.getSchema().getName();
		CardDTO cardToBeCreatedOrUpdated = new CardDTO(card.getId(), className, attributes);
		boolean cardMustBeCreated = card.getId() == -1;
		if (cardMustBeCreated) {
			Long createdCardId = dataLogic.createCard(cardToBeCreatedOrUpdated);
			serializer.put("id", createdCardId);
		} else {
			dataLogic.updateCard(cardToBeCreatedOrUpdated);
		}
		updateGisFeatures(card, attributes);
		return serializer;
	}

	private void updateGisFeatures(ICard card, Map<String, Object> attributes) throws Exception {
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

	@OldDao
	@JSONExported
	public void deleteCard(final ICard card) throws JSONException, CMDBException {
		card.delete();
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
		final Card src = new Card(card.getSchema().getId(), card.getId());
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
	public JSONObject getRelationList(final ICard card,
			@Parameter(value = "domainlimit", required = false) final int domainlimit,
			@Parameter(value = "domainId", required = false) final Long domainId,
			@Parameter(value = "src", required = false) final String querySource) throws JSONException {
		final DataAccessLogic dataAccesslogic = TemporaryObjectsBeforeSpringDI.getDataAccessLogic();
		final Card src = new Card(card.getSchema().getId(), card.getId());
		final DomainWithSource dom = DomainWithSource.create(domainId, querySource);
		final GetRelationListResponse out = dataAccesslogic.getRelationList(src, dom);
		return new JsonGetRelationListResponse(out, domainlimit).toJson();
	}

	@OldDao
	@JSONExported
	@Transacted
	public void createRelations(@Parameter("JSON") final JSONObject JSON, final UserContext userCtx)
			throws JSONException {
		saveRelation(JSON, userCtx, true);
	}

	@JSONExported
	public void modifyRelation(@Parameter(required = false, value = "JSON") final JSONObject JSON,
			final UserContext userCtx) throws JSONException {
		saveRelation(JSON, userCtx, false);
	}

	private void saveRelation(final JSONObject JSON, final UserContext userCtx, final boolean createRelation)
			throws JSONException {
		final int relId = JSON.optInt("id");
		final int domainId = JSON.getInt("did");
		final JSONObject attributes = JSON.getJSONObject("attrs");

		final IDomain domain = UserOperations.from(userCtx).domains().get(domainId);
		int side1element = countArrayOrZero(attributes, "_1");
		int side2element = countArrayOrZero(attributes, "_2");
		if (relId > 0 && side1element > 0 && side2element > 0) {
			throw new IllegalArgumentException();
		}

		do {
			do {
				final IRelation relation;
				if (relId > 0) {
					relation = UserOperations.from(userCtx).relations().get(domain, relId);
				} else if (createRelation) {
					relation = UserOperations.from(userCtx).relations().create(domain);
				} else {
					// When a detail is added, we don't know the relation
					// id so we have to load it from the two card ids
					final ICard card1 = cardFromJson(attributes.getJSONObject("_1"), userCtx);
					final ICard card2 = cardFromJson(attributes.getJSONObject("_2"), userCtx);
					relation = UserOperations.from(userCtx).relations().get(domain, card1, card2);
				}
				fillAttributes(relation, attributes, userCtx, side1element, side2element);
				relation.save();
			} while (side2element-- > 0);
		} while (side1element-- > 0);
	}

	private int countArrayOrZero(final JSONObject attributes, final String key) {
		try {
			return attributes.getJSONArray(key).length() - 1;
		} catch (final Exception e) {
			return 0;
		}
	}

	private ICard cardFromJson(final Object value, final UserContext userCtx) throws JSONException, NotFoundException {
		if (value instanceof JSONObject) {
			final JSONObject jsonCard = (JSONObject) value;
			final int cardId = jsonCard.getInt("id");
			final int classId = jsonCard.getInt("cid");
			final ICard card1 = UserOperations.from(userCtx).tables().get(classId).cards().get(cardId);
			return card1;
		} else {
			return null;
		}
	}

	private void fillAttributes(final IRelation relation, final JSONObject attributes, final UserContext userCtx,
			final int side1element, final int side2element) throws JSONException {
		for (final String name : JSONObject.getNames(attributes)) {
			Object value;
			if (attributes.isNull(name)) {
				value = null;
			} else {
				value = attributes.get(name);
			}
			if ("_1".equals(name)) {
				if (value instanceof JSONArray) {
					value = ((JSONArray) value).get(side1element);
				}
				if (value instanceof JSONObject) {
					final ICard card1 = cardFromJson(value, userCtx);
					relation.setCard1(card1);
				}
			} else if ("_2".equals(name)) {
				if (value instanceof JSONArray) {
					value = ((JSONArray) value).get(side2element);
				}
				if (value instanceof JSONObject) {
					final ICard card2 = cardFromJson(value, userCtx);
					relation.setCard2(card2);
				}
			} else {
				relation.setValue(name, value);
			}
		}
	}

	@OldDao
	@JSONExported
	public void deleteRelation(final IRelation oldWayOfIdentifyingARelation,
			@Parameter(required = false, value = "JSON") final JSONObject JSON, final UserContext userCtx)
			throws JSONException {
		if (oldWayOfIdentifyingARelation == null) {
			final int relId = JSON.optInt("id");
			final int domainId = JSON.getInt("did");
			final IDomain domain = UserOperations.from(userCtx).domains().get(domainId);
			UserOperations.from(userCtx).relations().get(domain, relId).delete();
		} else {
			oldWayOfIdentifyingARelation.delete();
		}
	}

	private static ICard stringToCard(final ITableFactory tf, final String string) {
		final StringTokenizer st = new StringTokenizer(string, "_");
		final int classId = Integer.parseInt(st.nextToken());
		final int cardId = Integer.parseInt(st.nextToken());
		return tf.get(classId).cards().get(cardId);
	}

}
