package org.cmdbuild.logic.data.access;

import static com.google.common.collect.FluentIterable.from;
import static com.google.common.collect.Iterables.isEmpty;
import static java.util.Arrays.asList;
import static org.cmdbuild.dao.driver.postgres.Const.DESCRIPTION_ATTRIBUTE;
import static org.cmdbuild.dao.driver.postgres.Const.ID_ATTRIBUTE;
import static org.cmdbuild.dao.entrytype.Deactivable.IsActivePredicate.filterActive;
import static org.cmdbuild.dao.query.clause.AnyAttribute.anyAttribute;
import static org.cmdbuild.dao.query.clause.QueryAliasAttribute.attribute;
import static org.cmdbuild.dao.query.clause.alias.EntryTypeAlias.canonicalAlias;
import static org.cmdbuild.dao.query.clause.join.Over.over;
import static org.cmdbuild.dao.query.clause.where.AndWhereClause.and;
import static org.cmdbuild.dao.query.clause.where.EqualsOperatorAndValue.eq;
import static org.cmdbuild.dao.query.clause.where.InOperatorAndValue.in;
import static org.cmdbuild.dao.query.clause.where.SimpleWhereClause.condition;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.Set;

import org.apache.commons.fileupload.FileItem;
import org.cmdbuild.common.collect.Mapper;
import org.cmdbuild.dao.entry.CMCard;
import org.cmdbuild.dao.entry.CMRelation;
import org.cmdbuild.dao.entry.CMRelation.CMRelationDefinition;
import org.cmdbuild.dao.entrytype.CMAttribute;
import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.dao.entrytype.CMDomain;
import org.cmdbuild.dao.entrytype.CMEntryType;
import org.cmdbuild.dao.entrytype.attributetype.ForeignKeyAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.LookupAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.NullAttributeTypeVisitor;
import org.cmdbuild.dao.entrytype.attributetype.ReferenceAttributeType;
import org.cmdbuild.dao.function.CMFunction;
import org.cmdbuild.dao.query.CMQueryResult;
import org.cmdbuild.dao.query.CMQueryRow;
import org.cmdbuild.dao.query.QuerySpecsBuilder;
import org.cmdbuild.dao.query.clause.FunctionCall;
import org.cmdbuild.dao.query.clause.OrderByClause;
import org.cmdbuild.dao.query.clause.OrderByClause.Direction;
import org.cmdbuild.dao.query.clause.QueryAliasAttribute;
import org.cmdbuild.dao.query.clause.alias.Alias;
import org.cmdbuild.dao.query.clause.alias.NameAlias;
import org.cmdbuild.dao.query.clause.where.WhereClause;
import org.cmdbuild.dao.view.CMDataView;
import org.cmdbuild.exception.NotFoundException;
import org.cmdbuild.exception.NotFoundException.NotFoundExceptionType;
import org.cmdbuild.logic.Logic;
import org.cmdbuild.logic.LogicDTO.DomainWithSource;
import org.cmdbuild.logic.TemporaryObjectsBeforeSpringDI;
import org.cmdbuild.logic.commands.AbstractGetRelation.RelationInfo;
import org.cmdbuild.logic.commands.GetCardHistory;
import org.cmdbuild.logic.commands.GetCardHistory.GetCardHistoryResponse;
import org.cmdbuild.logic.commands.GetRelationHistory;
import org.cmdbuild.logic.commands.GetRelationHistory.GetRelationHistoryResponse;
import org.cmdbuild.logic.commands.GetRelationList;
import org.cmdbuild.logic.commands.GetRelationList.GetRelationListResponse;
import org.cmdbuild.logic.data.QueryOptions;
import org.cmdbuild.logic.data.lookup.LookupDto;
import org.cmdbuild.logic.data.lookup.LookupStorableConverter;
import org.cmdbuild.logic.mapping.FilterMapper;
import org.cmdbuild.logic.mapping.SorterMapper;
import org.cmdbuild.logic.mapping.json.JsonFilterMapper;
import org.cmdbuild.logic.mapping.json.JsonSorterMapper;
import org.cmdbuild.model.data.Card;
import org.cmdbuild.model.data.Card.CardBuilder;
import org.cmdbuild.services.store.DataViewStore;
import org.cmdbuild.services.store.Store;
import org.cmdbuild.services.store.Store.Storable;
import org.cmdbuild.servlets.json.management.dataimport.csv.CsvData;
import org.cmdbuild.servlets.json.management.dataimport.csv.CsvImporter;
import org.cmdbuild.servlets.json.management.export.CMDataSource;
import org.cmdbuild.servlets.json.management.export.DBDataSource;
import org.cmdbuild.servlets.json.management.export.DataExporter;
import org.cmdbuild.servlets.json.management.export.csv.CsvExporter;
import org.json.JSONArray;
import org.supercsv.prefs.CsvPreference;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

/**
 * Business Logic Layer for Data Access
 */
public class DataAccessLogic implements Logic {

	private static final String DEFAULT_SORTING_ATTRIBUTE_NAME = "Description";

	private static final List<Card> EMPTY_CARD_LIST = Collections.emptyList();

	private final CMDataView view;

	public DataAccessLogic(final CMDataView view) {
		this.view = view;
	}

	public CMDataView getView() {
		return view;
	}

	private DataViewStore<Card> storeOf(final Card card) {
		return new DataViewStore<Card>(view, CardStorableConverter.of(card));
	}

	public Map<Object, List<RelationInfo>> relationsBySource(final String sourceTypeName, final DomainWithSource dom) {
		return new GetRelationList(view).list(sourceTypeName, dom);
	}

	public GetRelationListResponse getRelationList(final Card srcCard, final DomainWithSource dom,
			final QueryOptions options) {
		return new GetRelationList(view).exec(srcCard, dom, options);
	}

	public GetRelationListResponse getRelationList(final Card srcCard, final DomainWithSource dom) {
		return new GetRelationList(view).exec(srcCard, dom, QueryOptions.newQueryOption().build());
	}

	public GetRelationHistoryResponse getRelationHistory(final Card srcCard) {
		return new GetRelationHistory(view).exec(srcCard);
	}

	public GetCardHistoryResponse getCardHistory(final Card srcCard) {
		return new GetCardHistory(view).exec(srcCard);
	}

	public CMClass findClass(final Long classId) {
		final CMClass fetchedClass = view.findClass(classId);
		if (fetchedClass == null) {
			throw NotFoundException.NotFoundExceptionType.CLASS_NOTFOUND.createException();
		}
		return fetchedClass;
	}

	public CMClass findClass(final String className) {
		return view.findClass(className);
	}

	/**
	 * 
	 * @return only active classes (all classes, included superclasses, simple
	 *         classes and process classes).
	 */
	public Iterable<? extends CMClass> findActiveClasses() {
		return filterActive(view.findClasses());
	}

	/**
	 * 
	 * @return active and non active domains
	 */
	public Iterable<? extends CMDomain> findAllDomains() {
		return view.findDomains();
	}

	/**
	 * 
	 * @return only active domains
	 */
	public Iterable<? extends CMDomain> findActiveDomains() {
		return filterActive(view.findDomains());
	}

	public Iterable<? extends CMDomain> findDomains(final Predicate<CMDomain> predicate) {
		return Iterables.filter(view.findDomains(), predicate);
	}

	public Iterable<? extends CMDomain> findReferenceableDomains(final String className) {
		final CMClass fetchedClass = view.findClass(className);
		return Iterables.filter(view.findDomainsFor(fetchedClass), referenceableDomains(fetchedClass));
	}

	private static Predicate<CMDomain> referenceableDomains(final CMClass clazz) {
		return new Predicate<CMDomain>() {
			@Override
			public boolean apply(final CMDomain input) {
				return (input.getCardinality().equalsIgnoreCase("1:N") && input.getClass2().getIdentifier()
						.getLocalName().equals(clazz.getIdentifier().getLocalName()))
						|| (input.getCardinality().equalsIgnoreCase("N:1") && input.getClass1().getIdentifier()
								.getLocalName().equals(clazz.getIdentifier().getLocalName()));
			}
		};
	}

	/**
	 * 
	 * @return active and non active classes
	 */
	public Iterable<? extends CMClass> findAllClasses() {
		return view.findClasses();
	}

	/**
	 * Fetches the card with the specified Id from the class with the specified
	 * name
	 * 
	 * @param className
	 * @param cardId
	 * @throws NoSuchElementException
	 *             if the card with the specified Id number does not exist or it
	 *             is not unique
	 * @return the card with the specified Id.
	 */
	public Card fetchCard(final String className, final Long cardId) {
		final CMClass entryType = view.findClass(className);
		try {
			final CMQueryRow row = view.select(anyAttribute(entryType)) //
					.from(entryType) //
					.where(condition(attribute(entryType, ID_ATTRIBUTE), eq(cardId))) //
					.run() //
					.getOnlyRow();
			final Iterable<Card> cards = transformToCardDto(entryType, asList(row.getCard(entryType)));
			return cards.iterator().next();
		} catch (final NoSuchElementException ex) {
			throw NotFoundException.NotFoundExceptionType.CARD_NOTFOUND.createException();
		}
	}

	/**
	 * Retrieve the cards of a given class that matches the given query options
	 * 
	 * @param className
	 * @param queryOptions
	 * @return a FetchCardListResponse
	 */
	public FetchCardListResponse fetchCards(final String className, final QueryOptions queryOptions) {
		final CMClass fetchedClass = view.findClass(className);
		if (fetchedClass == null) {
			return new FetchCardListResponse(EMPTY_CARD_LIST, EMPTY_CARD_LIST.size());
		}

		final QuerySpecsBuilder querySpecsBuilder = fetchCardQueryBuilder(queryOptions, fetchedClass);

		final CMQueryResult result = querySpecsBuilder.run();
		final List<CMCard> filteredCards = Lists.newArrayList();
		for (final CMQueryRow row : result) {
			final CMCard card = row.getCard(fetchedClass);
			filteredCards.add(card);
		}

		final Iterable<Card> cards = transformToCardDto(fetchedClass, filteredCards);

		return new FetchCardListResponse(cards, result.totalSize());
	}

	private Iterable<Card> transformToCardDto(final CMClass fetchedClass, final Iterable<CMCard> filteredCards) {
		final Map<CMClass, Set<Long>> idsByEntryType = extractIdsByEntryType(fetchedClass, filteredCards);
		final Map<Long, String> representationsById = calculateRepresentationsById(idsByEntryType);

		final Iterable<Card> cards = from(filteredCards) //
				.transform(new Function<CMCard, Card>() {

					final Store<LookupDto> lookupStore = new DataViewStore<LookupDto>(TemporaryObjectsBeforeSpringDI
							.getSystemView(), new LookupStorableConverter());

					@Override
					public Card apply(final CMCard input) {
						final Card card = CardStorableConverter.of(input).convert(input);
						final CardBuilder updatedCard = Card.newInstance().clone(card);

						for (final CMAttribute attribute : input.getType().getAllAttributes()) {
							final String attributeName = attribute.getName();
							final Object rawValue = input.get(attributeName);
							if (rawValue == null) {
								continue;
							}
							attribute.getType().accept(new NullAttributeTypeVisitor() {

								@Override
								public void visit(final ForeignKeyAttributeType attributeType) {
									final Long id = attributeType.convertValue(rawValue);
									updatedCard.withAttribute(attributeName, new HashMap<String, Object>() {
										{
											put("id", id);
											put("description", representationsById.get(id));
										}
									});
								}

								@Override
								public void visit(final LookupAttributeType attributeType) {
									final Long id = attributeType.convertValue(rawValue);
									final LookupDto lookup = lookupStore.read(LookupDto.newInstance() //
											.withId(id) //
											.build());
									updatedCard.withAttribute(attributeName, new HashMap<String, Object>() {
										{
											put("id", lookup.id);
											put("description", lookup.description);
										}
									});
								}

								@Override
								public void visit(final ReferenceAttributeType attributeType) {
									final Long id = attributeType.convertValue(rawValue);
									updatedCard.withAttribute(attributeName, new HashMap<String, Object>() {
										{
											put("id", id);
											put("description", representationsById.get(id));
										}
									});
								}

							});
						}

						return updatedCard.build();
					}

				});
		return cards;
	}

	private Map<CMClass, Set<Long>> extractIdsByEntryType(final CMClass fetchedClass,
			final Iterable<CMCard> filteredCards) {
		final Map<CMClass, Set<Long>> idsByEntryType = Maps.newHashMap();
		for (final CMAttribute attribute : fetchedClass.getAttributes()) {
			attribute.getType().accept(new NullAttributeTypeVisitor() {

				@Override
				public void visit(final ForeignKeyAttributeType attributeType) {
					final String className = attributeType.getForeignKeyDestinationClassName();
					final CMClass target = view.findClass(className);
					extractIdsOfTarget(target);
				}

				@Override
				public void visit(final ReferenceAttributeType attributeType) {
					final ReferenceAttributeType type = ReferenceAttributeType.class.cast(attribute.getType());
					final CMDomain domain = view.findDomain(type.getDomainName());
					if (domain == null) {
						throw NotFoundExceptionType.DOMAIN_NOTFOUND.createException(type.getDomainName());
					}
					final CMClass target = domain.getClass1().isAncestorOf(fetchedClass) ? domain.getClass2() : domain
							.getClass1();
					extractIdsOfTarget(target);
				}

				private void extractIdsOfTarget(final CMClass target) {
					Set<Long> ids = idsByEntryType.get(target);
					if (ids == null) {
						ids = Sets.newHashSet();
						idsByEntryType.put(target, ids);
					}

					for (final CMCard card : filteredCards) {
						final Long id = card.get(attribute.getName(), Long.class);
						ids.add(id);
					}
				}

			});
		}
		return idsByEntryType;
	}

	private Map<Long, String> calculateRepresentationsById(final Map<CMClass, Set<Long>> idsByEntryType) {
		final Map<Long, String> representationsById = Maps.newHashMap();
		for (final CMClass entryType : idsByEntryType.keySet()) {
			final Set<Long> ids = idsByEntryType.get(entryType);
			if (ids.isEmpty()) {
				continue;
			}
			final Iterable<CMQueryRow> rows = view.select(DESCRIPTION_ATTRIBUTE) //
					.from(entryType) //
					.where(condition(attribute(entryType, ID_ATTRIBUTE), in(ids.toArray()))) //
					.run();
			for (final CMQueryRow row : rows) {
				final CMCard card = row.getCard(entryType);
				representationsById.put(card.getId(), card.get(DESCRIPTION_ATTRIBUTE, String.class));
			}
		}
		return representationsById;
	}

	/**
	 * Execute a given SQL function to select a set of rows Return these rows as
	 * fake cards
	 * 
	 * @param functionName
	 * @param queryOptions
	 * @return
	 */
	public FetchCardListResponse fetchSQLCards(final String functionName, final QueryOptions queryOptions) {
		final CMFunction fetchedFunction = view.findFunctionByName(functionName);
		final Alias functionAlias = NameAlias.as("f");

		if (fetchedFunction == null) {
			final List<Card> emptyCardList = Collections.emptyList();
			return new FetchCardListResponse(emptyCardList, 0);
		}

		final QuerySpecsBuilder querySpecsBuilder = fetchSQLCardQueryBuilder(queryOptions, fetchedFunction,
				functionAlias);
		final CMQueryResult queryResult = querySpecsBuilder.run();
		final List<Card> filteredCards = Lists.newArrayList();

		for (final CMQueryRow row : queryResult) {
			filteredCards.add( //
					Card.newInstance() //
							.withClassName(functionName) //
							.withAllAttributes(row.getValueSet(functionAlias).getValues()) //
							.build());
		}

		return new FetchCardListResponse(filteredCards, queryResult.totalSize());
	}

	public QuerySpecsBuilder fetchSQLCardQueryBuilder( //
			final QueryOptions queryOptions, //
			final CMFunction fetchedFunction, //
			final Alias functionAlias //
	) {

		final FunctionCall functionCall = FunctionCall.call(fetchedFunction, new HashMap<String, Object>());
		final FilterMapper filterMapper = new JsonFilterMapper(functionCall, queryOptions.getFilter(), view,
				functionAlias);
		final WhereClause whereClause = filterMapper.whereClause();
		final Iterable<FilterMapper.JoinElement> joinElements = filterMapper.joinElements();
		final QuerySpecsBuilder querySpecsBuilder = view //
				.select(anyAttribute(fetchedFunction, functionAlias)) //
				.from(functionCall, functionAlias) //
				.where(whereClause) //
				.limit(queryOptions.getLimit()) //
				.offset(queryOptions.getOffset());

		addJoinOptions(querySpecsBuilder, queryOptions, joinElements);
		addSortingOptions(querySpecsBuilder, queryOptions, functionCall, functionAlias);
		return querySpecsBuilder;
	}

	public QuerySpecsBuilder fetchCardQueryBuilder( //
			final QueryOptions queryOptions, //
			final CMClass fetchedClass //
	) {

		final FilterMapper filterMapper = new JsonFilterMapper(fetchedClass, queryOptions.getFilter(), view);
		final WhereClause whereClause = filterMapper.whereClause();
		final Iterable<FilterMapper.JoinElement> joinElements = filterMapper.joinElements();
		final Mapper<JSONArray, List<QueryAliasAttribute>> attributeSubsetMapper = new JsonAttributeSubsetMapper(
				fetchedClass);
		final List<QueryAliasAttribute> attributeSubsetForSelect = attributeSubsetMapper.map(queryOptions
				.getAttributes());
		final QuerySpecsBuilder querySpecsBuilder = newQuerySpecsBuilder(attributeSubsetForSelect, fetchedClass);
		querySpecsBuilder.from(fetchedClass) //
				.where(whereClause) //
				.limit(queryOptions.getLimit()) //
				.offset(queryOptions.getOffset());

		addJoinOptions(querySpecsBuilder, queryOptions, joinElements);
		addSortingOptions(querySpecsBuilder, queryOptions, fetchedClass);
		return querySpecsBuilder;
	}

	/**
	 * 
	 * @param className
	 * @param cardId
	 * @param queryOptions
	 * @return a long (zero based) with the position of this card in relation of
	 *         current sorting and filter
	 */
	public Long getCardPosition(final String className, final Long cardId, final QueryOptions queryOptions) {
		final CMClass fetchedClass = view.findClass(className);

		final FilterMapper filterMapper = new JsonFilterMapper(fetchedClass, queryOptions.getFilter(), view);
		final WhereClause whereClause = filterMapper.whereClause();
		final QuerySpecsBuilder queryBuilder = view.select(anyAttribute(fetchedClass)) //
				.from(fetchedClass) //
				.where(whereClause) //
				.numbered(condition(attribute(fetchedClass, ID_ATTRIBUTE), eq(cardId)));

		addSortingOptions(queryBuilder, queryOptions, fetchedClass);

		final CMQueryRow row = queryBuilder.run().getOnlyRow();
		final Long n = row.getNumber() - 1;

		return n;
	}

	private QuerySpecsBuilder newQuerySpecsBuilder(final List<QueryAliasAttribute> attributeSubsetForSelect,
			final CMEntryType entryType) {
		if (attributeSubsetForSelect.isEmpty()) {
			return view.select(anyAttribute(entryType));
		}
		final Object[] attributesArray = new QueryAliasAttribute[attributeSubsetForSelect.size()];
		attributeSubsetForSelect.toArray(attributesArray);
		return view.select(attributesArray);
	}

	private void addJoinOptions(final QuerySpecsBuilder querySpecsBuilder, final QueryOptions options,
			final Iterable<FilterMapper.JoinElement> joinElements) {
		if (!isEmpty(joinElements)) {
			querySpecsBuilder.distinct();
		}
		for (final FilterMapper.JoinElement joinElement : joinElements) {
			final CMDomain domain = view.findDomain(joinElement.domain);
			final CMClass clazz = view.findClass(joinElement.destination);
			if (joinElement.left) {
				querySpecsBuilder.leftJoin(clazz, canonicalAlias(clazz), over(domain));
			} else {
				querySpecsBuilder.join(clazz, canonicalAlias(clazz), over(domain));
			}
		}
	}

	private void addSortingOptions( //
			final QuerySpecsBuilder querySpecsBuilder, //
			final QueryOptions options, //
			final FunctionCall functionCall, //
			final Alias alias) { //

		final SorterMapper sorterMapper = new JsonSorterMapper(functionCall, options.getSorters(), alias);
		final List<OrderByClause> clauses = sorterMapper.deserialize();

		addSortingOptions(querySpecsBuilder, clauses);
	}

	private void addSortingOptions( //
			final QuerySpecsBuilder querySpecsBuilder, //
			final QueryOptions options, //
			final CMClass clazz //
	) {

		final SorterMapper sorterMapper = new JsonSorterMapper(clazz, options.getSorters());
		final List<OrderByClause> clauses = sorterMapper.deserialize();

		// If no sorting rules are defined
		// sort by description (if the class has a description)
		if (clauses.isEmpty()) {
			if (clazz.getAttribute(DEFAULT_SORTING_ATTRIBUTE_NAME) != null) {
				querySpecsBuilder.orderBy(attribute(clazz, DEFAULT_SORTING_ATTRIBUTE_NAME), Direction.ASC);
			}
		} else {
			addSortingOptions(querySpecsBuilder, clauses);
		}
	}

	private void addSortingOptions(final QuerySpecsBuilder querySpecsBuilder, final List<OrderByClause> clauses) {
		for (final OrderByClause clause : clauses) {
			querySpecsBuilder.orderBy(clause.getAttribute(), clause.getDirection());
		}
	}

	public Long createCard(final Card card) {
		final CMClass entryType = view.findClass(card.getClassName());
		if (entryType == null) {
			throw NotFoundException.NotFoundExceptionType.CLASS_NOTFOUND.createException();
		}
		final Store<Card> store = storeOf(card);
		final Storable created = store.create(card);
		return store.read(created).getId();
	}

	public void updateCard(final Card card) {
		final CMClass entryType = view.findClass(card.getClassName());
		if (entryType == null) {
			throw NotFoundException.NotFoundExceptionType.CLASS_NOTFOUND.createException();
		}
		final Store<Card> store = storeOf(card);
		final Card currentCard = store.read(card);
		final Card updatedCard = Card.newInstance() //
				.clone(currentCard) //
				.withAllAttributes(card.getAttributes()) //
				.build();
		store.update(updatedCard);
	}

	public void updateFetchedCard(final Card card, final Map<String, Object> attributes) {
		if (card != null) {
			final Card updatedCard = Card.newInstance() //
					.clone(card) //
					.withAllAttributes(attributes) //
					.build();
			storeOf(updatedCard).update(updatedCard);
		}
	}

	public void deleteCard(final String className, final Long cardId) {
		final Card card = Card.newInstance() //
				.withClassName(className) //
				.withId(cardId) //
				.build();
		storeOf(card).delete(card);
	}

	/**
	 * Retrieves all domains in which the class with id = classId is involved
	 * (both direct and inverse relation)
	 * 
	 * @param className
	 *            the class name involved in the relation
	 * @return a list of all domains defined for the class
	 */
	public List<CMDomain> findDomainsForClassWithName(final String className) {
		final CMClass fetchedClass = view.findClass(className);
		if (fetchedClass == null) {
			throw NotFoundException.NotFoundExceptionType.DOMAIN_NOTFOUND.createException();
		}

		return findDomainsForCMClass(fetchedClass);
	}

	private List<CMDomain> findDomainsForCMClass(final CMClass fetchedClass) {
		return Lists.newArrayList(view.findDomainsFor(fetchedClass));
	}

	/**
	 * Tells if the given class is a subclass of Activity
	 * 
	 * @return {@code true} if if the given class is a subclass of Activity,
	 *         {@code false} otherwise
	 */
	public boolean isProcess(final CMClass target) {
		final CMClass activity = view.getActivityClass();
		return activity.isAncestorOf(target);
	}

	/**
	 * Relations.... move the following code to another class
	 */

	public void createRelations(final RelationDTO relationDTO) {
		final CMDomain domain = view.findDomain(relationDTO.domainName);
		if (domain == null) {
			throw NotFoundException.NotFoundExceptionType.DOMAIN_NOTFOUND.createException();
		}
		final CMCard parentCard = retrieveParentCard(relationDTO);
		final List<CMCard> childCards = retrieveChildCards(relationDTO);

		if (relationDTO.master.equals("_1")) {
			for (final CMCard dstCard : childCards) {
				saveRelation(domain, parentCard, dstCard, relationDTO.relationAttributeToValue);
			}
		} else {
			for (final CMCard srcCard : childCards) {
				saveRelation(domain, srcCard, parentCard, relationDTO.relationAttributeToValue);
			}
		}
	}

	private CMCard retrieveParentCard(final RelationDTO relationDTO) {
		Map<Long, String> cardToClassName;
		if (relationDTO.master.equals("_1")) {
			cardToClassName = relationDTO.srcCardIdToClassName;
		} else {
			cardToClassName = relationDTO.dstCardIdToClassName;
		}
		for (final Long cardId : cardToClassName.keySet()) {
			final String className = cardToClassName.get(cardId);
			return fetchCardForClassAndId(className, cardId);
		}
		return null; // should be unreachable
	}

	private List<CMCard> retrieveChildCards(final RelationDTO relationDTO) {
		final List<CMCard> childCards = Lists.newArrayList();
		Map<Long, String> cardToClassName;
		if (relationDTO.master.equals("_1")) {
			cardToClassName = relationDTO.dstCardIdToClassName;
		} else {
			cardToClassName = relationDTO.srcCardIdToClassName;
		}
		for (final Long cardId : cardToClassName.keySet()) {
			final String className = cardToClassName.get(cardId);
			childCards.add(fetchCardForClassAndId(className, cardId));
		}
		return childCards;
	}

	private void saveRelation(final CMDomain domain, final CMCard srcCard, final CMCard dstCard,
			final Map<String, Object> attributeToValue) {
		final CMRelationDefinition mutableRelation = view.createRelationFor(domain);
		mutableRelation.setCard1(srcCard);
		mutableRelation.setCard2(dstCard);
		for (final String attributeName : attributeToValue.keySet()) {
			final Object value = attributeToValue.get(attributeName);
			mutableRelation.set(attributeName, value);
		}
		mutableRelation.create();
	}

	public void updateRelation(final RelationDTO relationDTO) {
		final CMDomain domain = view.findDomain(relationDTO.domainName);
		if (domain == null) {
			throw NotFoundException.NotFoundExceptionType.DOMAIN_NOTFOUND.createException();
		}
		final Entry<Long, String> srcCard = relationDTO.getUniqueEntryForSourceCard();
		final String srcClassName = srcCard.getValue();
		final Long srcCardId = srcCard.getKey();
		final CMClass srcClass = view.findClass(srcClassName);

		final Entry<Long, String> dstCard = relationDTO.getUniqueEntryForDestinationCard();
		final String dstClassName = dstCard.getValue();
		final Long dstCardId = dstCard.getKey();
		final CMCard fetchedDstCard = fetchCardForClassAndId(dstClassName, dstCardId);
		final CMClass dstClass = view.findClass(dstClassName);
		CMQueryRow row;
		if (relationDTO.master.equals("_1")) {
			row = view.select(anyAttribute(srcClass), anyAttribute(domain))//
					.from(srcClass) //
					.join(dstClass, over(domain)) //
					.where(and(condition(attribute(srcClass, DESCRIPTION_ATTRIBUTE), eq(srcCardId)), //
							condition(attribute(domain, DESCRIPTION_ATTRIBUTE), eq(relationDTO.relationId)))) //
					.run().getOnlyRow();
		} else {
			row = view.select(anyAttribute(dstClass), anyAttribute(domain)) //
					.from(dstClass) //
					.join(srcClass, over(domain)) //
					.where(and(condition(attribute(dstClass, DESCRIPTION_ATTRIBUTE), eq(dstCardId)), //
							condition(attribute(domain, DESCRIPTION_ATTRIBUTE), eq(relationDTO.relationId)))) //
					.run().getOnlyRow();
		}
		final CMRelation relation = row.getRelation(domain).getRelation();
		final CMRelationDefinition mutableRelation = view.update(relation) //
				.setCard1(fetchedDstCard) //
				.setCard2(fetchedDstCard);
		updateRelationAttributes(relationDTO.relationAttributeToValue, mutableRelation);
		mutableRelation.update();
	}

	private void updateRelationAttributes(final Map<String, Object> attributeToValue,
			final CMRelationDefinition relDefinition) {
		for (final Entry<String, Object> entry : attributeToValue.entrySet()) {
			relDefinition.set(entry.getKey(), entry.getValue());
		}
	}

	public void deleteRelation(final RelationDTO relationDTO) {
		final CMDomain domain = view.findDomain(relationDTO.domainName);
		if (domain == null) {
			throw NotFoundException.NotFoundExceptionType.DOMAIN_NOTFOUND.createException();
		}
		final String srcClassName = relationDTO.getUniqueEntryForSourceCard().getValue();
		final String dstClassName = relationDTO.getUniqueEntryForDestinationCard().getValue();
		final CMClass srcClass = view.findClass(srcClassName);
		final CMClass dstClass = view.findClass(dstClassName);
		final Long srcCardId = relationDTO.getUniqueEntryForSourceCard().getKey();
		final CMQueryRow row = view.select(anyAttribute(srcClass), anyAttribute(domain))//
				.from(srcClass) //
				.join(dstClass, over(domain)) //
				.where(and(condition(attribute(srcClass, DESCRIPTION_ATTRIBUTE), eq(srcCardId)), //
						condition(attribute(domain, DESCRIPTION_ATTRIBUTE), eq(relationDTO.relationId)))) //
				.run().getOnlyRow();
		final CMRelation relation = row.getRelation(domain).getRelation();
		final CMRelationDefinition mutableRelation = view.update(relation);
		mutableRelation.delete();
	}

	public File exportClassAsCsvFile(final String className, final String separator) {
		final CMClass fetchedClass = view.findClass(className);
		final int separatorInt = separator.charAt(0);
		final CsvPreference exportCsvPrefs = new CsvPreference('"', separatorInt, "\n");
		final String fileName = fetchedClass.getIdentifier().getLocalName() + ".csv";
		final String dirName = System.getProperty("java.io.tmpdir");
		final File targetFile = new File(dirName, fileName);
		final DataExporter dataExporter = new CsvExporter(targetFile, exportCsvPrefs);
		final CMDataSource dataSource = new DBDataSource(view, fetchedClass);
		return dataExporter.export(dataSource);
	}

	public CsvData importCsvFileFor(final FileItem csvFile, final Long classId, final String separator)
			throws IOException {
		final CMClass destinationClassForImport = view.findClass(classId);
		final int separatorInt = separator.charAt(0);
		final CsvPreference importCsvPreferences = new CsvPreference('"', separatorInt, "\n");
		final CsvImporter csvImporter = new CsvImporter(view, destinationClassForImport, importCsvPreferences);
		final CsvData csvData = csvImporter.getCsvDataFrom(csvFile);
		return csvData;
	}

	private CMCard fetchCardForClassAndId(final String className, final Long cardId) {
		final CMClass entryType = view.findClass(className);
		final CMQueryRow row;
		try {
			row = view.select(anyAttribute(entryType)) //
					.from(entryType) //
					.where(condition(attribute(entryType, DESCRIPTION_ATTRIBUTE), eq(cardId))) //
					.run() //
					.getOnlyRow();
		} catch (final NoSuchElementException ex) {
			throw NotFoundException.NotFoundExceptionType.CARD_NOTFOUND.createException();
		}
		final CMCard card = row.getCard(entryType);
		return card;
	}

}
