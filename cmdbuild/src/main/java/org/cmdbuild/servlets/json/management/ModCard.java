package org.cmdbuild.servlets.json.management;

import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.StringTokenizer;

import org.cmdbuild.common.annotations.CheckIntegration;
import org.cmdbuild.common.annotations.OldDao;
import org.cmdbuild.dao.backend.CMBackend;
import org.cmdbuild.dao.entry.CMCard;
import org.cmdbuild.dao.entry.CMLookup;
import org.cmdbuild.elements.DirectedDomain;
import org.cmdbuild.elements.DirectedDomain.DomainDirection;
import org.cmdbuild.elements.Lookup;
import org.cmdbuild.elements.TableImpl.OrderEntry;
import org.cmdbuild.elements.TableTree;
import org.cmdbuild.elements.filters.AbstractFilter;
import org.cmdbuild.elements.filters.AttributeFilter;
import org.cmdbuild.elements.filters.AttributeFilter.AttributeFilterType;
import org.cmdbuild.elements.filters.CompositeFilter;
import org.cmdbuild.elements.filters.CompositeFilter.CompositeFilterItem;
import org.cmdbuild.elements.filters.FilterOperator;
import org.cmdbuild.elements.filters.OrderFilter;
import org.cmdbuild.elements.filters.OrderFilter.OrderFilterType;
import org.cmdbuild.elements.interfaces.BaseSchema.Mode;
import org.cmdbuild.elements.interfaces.CardQuery;
import org.cmdbuild.elements.interfaces.DomainFactory;
import org.cmdbuild.elements.interfaces.IAttribute;
import org.cmdbuild.elements.interfaces.ICard;
import org.cmdbuild.elements.interfaces.IDomain;
import org.cmdbuild.elements.interfaces.IRelation;
import org.cmdbuild.elements.interfaces.ITable;
import org.cmdbuild.elements.interfaces.ITableFactory;
import org.cmdbuild.elements.interfaces.Process.ProcessAttributes;
import org.cmdbuild.elements.interfaces.RelationFactory;
import org.cmdbuild.elements.wrappers.PrivilegeCard.PrivilegeType;
import org.cmdbuild.exception.CMDBException;
import org.cmdbuild.exception.NotFoundException;
import org.cmdbuild.logic.LogicDTO.Card;
import org.cmdbuild.logic.LogicDTO.DomainWithSource;
import org.cmdbuild.logic.TemporaryObjectsBeforeSpringDI;
import org.cmdbuild.logic.commands.GetRelationHistory.GetRelationHistoryResponse;
import org.cmdbuild.logic.commands.GetRelationList;
import org.cmdbuild.logic.commands.GetRelationList.GetRelationListResponse;
import org.cmdbuild.logic.data.DataAccessLogic;
import org.cmdbuild.logic.data.DataAccessLogic.FetchCardListResponse;
import org.cmdbuild.logic.data.QueryOptions;
import org.cmdbuild.services.SessionVars;
import org.cmdbuild.services.auth.UserContext;
import org.cmdbuild.services.auth.UserOperations;
import org.cmdbuild.services.meta.MetadataService;
import org.cmdbuild.services.store.DBClassWidgetStore;
import org.cmdbuild.servlets.json.JSONBase;
import org.cmdbuild.servlets.json.serializers.CardSerializer;
import org.cmdbuild.servlets.json.serializers.JsonGetRelationHistoryResponse;
import org.cmdbuild.servlets.json.serializers.JsonGetRelationListResponse;
import org.cmdbuild.servlets.json.serializers.Serializer;
import org.cmdbuild.servlets.utils.OverrideKeys;
import org.cmdbuild.servlets.utils.Parameter;
import org.codehaus.jackson.map.ObjectMapper;
import org.joda.time.DateTime;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class ModCard extends JSONBase {

	/**
	 * Retrieves the cards for the specified class. If a filter is defined, only
	 * the cards that match the filter are retrieved. The fetched cards are
	 * sorted if a sorter is defined. Note that the max number of retrieved
	 * cards is the 'limit' parameter
	 * 
	 * @param serializer
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

	//TODO: check the input parameters and serialization
	@CheckIntegration
	@JSONExported
	public JSONObject getCardListShort( //
			final JSONObject serializer, //
			@Parameter(value = "className") final String className,
			@Parameter("limit") final int limit, //
			@Parameter("start") final int offset, //
			@Parameter(value = "filter", required = false) final JSONObject filter,
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
		FetchCardListResponse response = dataLogic.fetchCards(className, queryOptions);
		CardSerializer.toClient(response.getPaginatedCards(), response.getTotalNumberOfCards());
		return serializer;
	}

	private CardQuery applyAttributesConfiguration(final CardQuery cardQueryTemplate, //
			final JSONArray attributes) throws JSONException {

		String[] shortAttrList = { "Id", "Description" };

		// Use the passed attributes if present
		if (attributes != null) {
			shortAttrList = new String[attributes.length()];
			for (int i = 0; i < attributes.length(); ++i) {
				shortAttrList[i] = attributes.getString(i);
			}
		}

		return ((CardQuery) cardQueryTemplate.clone()).attributes(shortAttrList);
	}

	public static void applySortToCardQuery(final JSONArray sorters, final CardQuery cardQuery) throws JSONException {
		if (sorters != null && sorters.length() > 0) {
			final JSONObject s = sorters.getJSONObject(0);
			String sortField = s.getString("property");
			final String sortDirection = s.getString("direction");

			if (sortField != null || sortDirection != null) {
				if (sortField.endsWith("_value")) {
					sortField = sortField.substring(0, sortField.length() - 6);
				}

				cardQuery.clearOrder().order(sortField, OrderFilterType.valueOf(sortDirection));
			}
		}
	}

	private void temporaryPatchToFakePrivilegeCheckOnCQL(final CardQuery cardQuery, final UserContext userContext) {
		final ITable fromTable = cardQuery.getTable();
		if (fromTable.getMode() == Mode.RESERVED) {
			userContext.privileges().assureReadPrivilege(fromTable);
		}
	}

	@OldDao
	@JSONExported
	public JSONObject getDetailList(final JSONObject serializer, @Parameter("IdClass") final int masterIdClass,
			@Parameter("Id") final int masterIdCard, @Parameter("limit") final int limit,
			@Parameter("start") final int offset, @Parameter(value = "sort", required = false) final JSONArray sorters,
			@Parameter(value = "query", required = false) final String fullTextQuery,
			@Parameter(value = "DirectedDomain", required = false) final String directedDomainParameter,
			final ITableFactory tf, final RelationFactory rf, final DomainFactory df) throws JSONException,
			CMDBException {
		final JSONArray rows = new JSONArray();

		// define the inverse domain
		final DirectedDomain directedDomain = stringToDirectedDomain(df, directedDomainParameter);
		final DirectedDomain invertedDomain = DirectedDomain.create(directedDomain.getDomain(),
				!directedDomain.getDirectionValue());

		final CardQuery masterQuery = tf.get(masterIdClass).cards().list().id(masterIdCard);

		final ITable destinationTable = directedDomain.getDestTable();
		final CardQuery detailQuery = destinationTable.cards().list().cardInRelation(invertedDomain, masterQuery);

		if (fullTextQuery != null) {
			detailQuery.fullText(fullTextQuery.trim());
		}

		if (sorters != null) {
			applySortToCardQuery(sorters, detailQuery);
		} else {
			// if there is no sorters apply the default
			detailQuery.clearOrder();
			for (final OrderEntry sortEntry : destinationTable.getOrdering()) {
				detailQuery.order(sortEntry.getAttributeName(), sortEntry.getOrderDirection());
			}
		}

		for (final ICard card : detailQuery.subset(offset, limit).count()) {
			rows.put(Serializer.serializeCardWithPrivileges(card, false));
		}

		serializer.put("rows", rows);
		serializer.put("results", detailQuery.getTotalRows());
		return serializer;
	}

	/*
	 * TODO: Find a way to fix this somewhere else
	 */
	private void removeReadOnlySubclasses(final CardQuery cardQuery, final UserContext userContext) {
		final List<String> readOnlyTables = new LinkedList<String>();
		final TableTree wholeTree = UserOperations.from(userContext).tables().fullTree();
		for (final ITable table : wholeTree) {
			if (PrivilegeType.READ.equals(table.getMetadata().get(MetadataService.RUNTIME_PRIVILEGES_KEY))) {
				readOnlyTables.add(String.valueOf(table.getId()));
			}
		}
		if (!readOnlyTables.isEmpty()) {
			final String[] readOnlyTablesArray = readOnlyTables.toArray(new String[0]);
			cardQuery.filter(ICard.CardAttributes.ClassId.toString(), AttributeFilterType.DIFFERENT,
					(Object[]) readOnlyTablesArray);
		}
	}

	//TODO: replace card with cardId and "IdClass" with className
	@CheckIntegration
	@JSONExported
	public JSONObject getCard(ICard card, @Parameter("IdClass") final int requestedIdClass,
			final JSONObject serializer) throws JSONException {
		final DataAccessLogic dataLogic = TemporaryObjectsBeforeSpringDI.getDataAccessLogic();
		CMCard fetchedCard = dataLogic.fetchCard(card.getSchema().getName(), card.getId());
		return CardSerializer.toClient(fetchedCard); //TODO: check if the serialization is correct
	}

	/*
	 * Yes we can!
	 */
	private ICard fetchRealCardForSuperclasses(ICard card, final int requestedIdClass) {
		card.getCode(); // HACK to fetch the card
		if (card.getSchema().getId() != requestedIdClass) {
			card = card.getSchema().cards().get(card.getId());
		}
		return card;
	}

	/*
	 * FIXME This is an awful piece of code, but is only temporarily needed till
	 * it can be fixed by the new DAO in the next release
	 */
	private void addReferenceAttributes(final ICard card, final UserContext userCtx, final JSONObject serializer)
			throws JSONException {
		final DataAccessLogic dataAccesslogic = applicationContext.getBean(DataAccessLogic.class);
		final Card src = new Card(card.getSchema().getId(), card.getId());

		final JSONObject jsonRefAttr = new JSONObject();
		final Set<IAttribute> referenceWithAttributes = getReferenceWithAttributes(card.getSchema());
		for (final IAttribute reference : referenceWithAttributes) {
			if (card.getValue(reference.getName()) == null) {
				continue;
			}
			final IDomain domain = reference.getReferenceDomain();
			final Long domainId = Long.valueOf(domain.getId());
			final String querySource = reference.isReferenceDirect() ? "_1" : "_2";
			final DomainWithSource dom = DomainWithSource.create(domainId, querySource);
			final GetRelationListResponse rel = dataAccesslogic.getRelationList(src, dom);
			final JSONObject jsonRef = new JSONObject();
			if (!rel.iterator().hasNext()) {
				continue;
			}
			final GetRelationList.DomainInfo di = rel.iterator().next();
			if (!di.iterator().hasNext()) {
				continue;
			}
			for (final Entry<String, Object> entry : di.iterator().next().getRelationAttributes()) {
				final String name = entry.getKey();
				Object value = entry.getValue();

				if (value instanceof CMLookup) {
					// FIXME Temporary till the new DAO is finished
					final CMLookup lookup = (CMLookup) value;
					final Integer lookupId = lookup.getId().intValue();
					final String lookupDescription = CMBackend.INSTANCE.getLookup(lookupId).toString();

					jsonRef.put(name, lookupId);
					jsonRef.put(name + "_value", lookupDescription);
				} else {
					if (value instanceof DateTime) {
						value = new Date(((DateTime) value).getMillis());
					}
					final String stringValue = domain.getAttribute(name).valueToString(value);
					jsonRef.put(name, stringValue);
				}
			}
			jsonRefAttr.put(reference.getName(), jsonRef);
		}
		serializer.put("referenceAttributes", jsonRefAttr);
	}

	public Set<IAttribute> getReferenceWithAttributes(final ITable table) {
		final Set<IAttribute> reference = new HashSet<IAttribute>();
		for (final IAttribute ta : table.getAttributes().values()) {
			final DirectedDomain dd = ta.getReferenceDirectedDomain();
			if (dd != null) {
				final IDomain d = dd.getDomain();
				for (final IAttribute da : d.getAttributes().values()) {
					if (da.isDisplayable()) { // && da.isBaseDSP()
						reference.add(ta);
						break;
					}
				}
			}
		}
		return reference;
	}

	@OldDao
	@JSONExported
	public JSONObject getCardPosition(
			@Parameter(value = "retryWithoutFilter", required = false) final boolean retryWithoutFilter,
			final JSONObject serializer, final ICard card, final CardQuery currentCardQuery, final UserContext userCtx)
			throws JSONException {
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

	@OldDao
	@JSONExported
	public JSONObject updateCard(final ICard card, final Map<String, String> attributes, final UserContext userCtx,
			final JSONObject serializer) throws Exception {
		setCardAttributes(card, attributes, false);
		final boolean created = card.isNew();
		card.save();
		fillReferenceAttributes(card, attributes, UserOperations.from(userCtx).relations());

		// TODO: uncomment these lines after updating to new dao
		// final GISLogic gisLogic =
		// TemporaryObjectsBeforeSpringDI.getGISLogic();
		// if (gisLogic.isGisEnabled()) {
		// gisLogic.updateFeatures(card, attributes);
		// }

		if (created) {
			serializer.put("id", card.getId());
		}

		return serializer;
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

	public static void fillReferenceAttributes(final ICard card, final Map<String, String> attributes,
			final RelationFactory relationFactory) {
		for (final IAttribute attribute : card.getSchema().getAttributes().values()) {
			final DirectedDomain dd = attribute.getReferenceDirectedDomain();
			if (dd == null || !attribute.isDisplayable()) {
				continue;
			}
			final String referenceName = attribute.getName();
			final String attrNewValue = attributes.get(referenceName);
			if (attrNewValue == null || attrNewValue.isEmpty()) {
				continue;
			}

			final int refDstId = Integer.parseInt(attrNewValue);
			final ICard refDstCard = attribute.getReferenceTarget().cards().get(refDstId);
			final IRelation referenceRelation;
			if (dd.getDirectionValue()) {
				referenceRelation = relationFactory.get(dd.getDomain(), card, refDstCard);
			} else {
				referenceRelation = relationFactory.get(dd.getDomain(), refDstCard, card);
			}
			if (fillSingleReferenceAttributes(referenceName, referenceRelation, attributes)) {
				referenceRelation.save();
			}
		}
	}

	public static boolean fillSingleReferenceAttributes(final String referenceName, final IRelation referenceRelation,
			final Map<String, String> attributes) {
		boolean changed = false;
		for (final IAttribute domAttr : referenceRelation.getSchema().getAttributes().values()) {
			if (!domAttr.isDisplayable()) {
				continue;
			}
			final String domAttrName = domAttr.getName();
			final String reqAttrName = String.format("_%s_%s", referenceName, domAttrName);
			final String reqAttrValue = attributes.get(reqAttrName);
			if (reqAttrValue != null) {
				referenceRelation.setValue(domAttrName, reqAttrValue);
				changed = true;
			}
		}
		return changed;
	}

	@OldDao
	@JSONExported
	public void deleteCard(final ICard card) throws JSONException, CMDBException {
		card.delete();
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

	@OldDao
	@JSONExported
	public JSONObject getRelationList(final ICard card, final UserContext userCtx,
			@Parameter(value = "domainlimit", required = false) final int domainlimit,
			@Parameter(value = "domainId", required = false) final Long domainId,
			@Parameter(value = "src", required = false) final String querySource) throws JSONException {
		final DataAccessLogic dataAccesslogic = applicationContext.getBean(DataAccessLogic.class);
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

	private static DirectedDomain stringToDirectedDomain(final DomainFactory df, final String string) {
		final StringTokenizer st = new StringTokenizer(string, "_");
		final int domainId = Integer.parseInt(st.nextToken());
		final IDomain domain = df.get(domainId);
		final DomainDirection direction = DomainDirection.valueOf(st.nextToken());
		return DirectedDomain.create(domain, direction);
	}

	private static ICard stringToCard(final ITableFactory tf, final String string) {
		final StringTokenizer st = new StringTokenizer(string, "_");
		final int classId = Integer.parseInt(st.nextToken());
		final int cardId = Integer.parseInt(st.nextToken());
		return tf.get(classId).cards().get(cardId);
	}

}
