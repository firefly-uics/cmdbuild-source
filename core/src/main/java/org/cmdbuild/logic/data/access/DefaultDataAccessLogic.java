package org.cmdbuild.logic.data.access;

import static com.google.common.collect.FluentIterable.from;
import static java.util.Arrays.asList;
import static org.cmdbuild.dao.constants.Cardinality.CARDINALITY_1N;
import static org.cmdbuild.dao.constants.Cardinality.CARDINALITY_N1;
import static org.cmdbuild.dao.driver.postgres.Const.ID_ATTRIBUTE;
import static org.cmdbuild.dao.entrytype.Deactivable.IsActivePredicate.filterActive;
import static org.cmdbuild.dao.query.clause.AnyAttribute.anyAttribute;
import static org.cmdbuild.dao.query.clause.AnyClass.anyClass;
import static org.cmdbuild.dao.query.clause.QueryAliasAttribute.attribute;
import static org.cmdbuild.dao.query.clause.alias.Utils.as;
import static org.cmdbuild.dao.query.clause.join.Over.over;
import static org.cmdbuild.dao.query.clause.where.AndWhereClause.and;
import static org.cmdbuild.dao.query.clause.where.EqualsOperatorAndValue.eq;
import static org.cmdbuild.dao.query.clause.where.SimpleWhereClause.condition;
import static org.cmdbuild.spring.SpringIntegrationUtils.applicationContext;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.RandomStringUtils;
import org.cmdbuild.auth.user.OperationUser;
import org.cmdbuild.common.utils.PagedElements;
import org.cmdbuild.dao.entry.CMCard;
import org.cmdbuild.dao.entry.CMRelation;
import org.cmdbuild.dao.entry.CMRelation.CMRelationDefinition;
import org.cmdbuild.dao.entrytype.CMAttribute;
import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.dao.entrytype.CMDomain;
import org.cmdbuild.dao.entrytype.attributetype.CMAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.ReferenceAttributeType;
import org.cmdbuild.dao.function.CMFunction;
import org.cmdbuild.dao.query.CMQueryResult;
import org.cmdbuild.dao.query.CMQueryRow;
import org.cmdbuild.dao.query.clause.QueryAliasAttribute;
import org.cmdbuild.dao.query.clause.alias.Alias;
import org.cmdbuild.dao.query.clause.alias.NameAlias;
import org.cmdbuild.dao.query.clause.where.WhereClause;
import org.cmdbuild.dao.view.CMDataView;
import org.cmdbuild.dao.view.DBDataView;
import org.cmdbuild.data.store.DataViewStore;
import org.cmdbuild.data.store.Store;
import org.cmdbuild.data.store.Store.Storable;
import org.cmdbuild.data.store.lookup.LookupStore;
import org.cmdbuild.exception.NotFoundException;
import org.cmdbuild.exception.ORMException.ORMExceptionType;
import org.cmdbuild.logger.Log;
import org.cmdbuild.logic.LogicDTO.DomainWithSource;
import org.cmdbuild.logic.commands.AbstractGetRelation.RelationInfo;
import org.cmdbuild.logic.commands.GetCardHistory;
import org.cmdbuild.logic.commands.GetCardHistory.GetCardHistoryResponse;
import org.cmdbuild.logic.commands.GetRelationHistory;
import org.cmdbuild.logic.commands.GetRelationHistory.GetRelationHistoryResponse;
import org.cmdbuild.logic.commands.GetRelationList;
import org.cmdbuild.logic.commands.GetRelationList.GetRelationListResponse;
import org.cmdbuild.logic.data.QueryOptions;
import org.cmdbuild.logic.data.access.lock.LockCardManager;
import org.cmdbuild.logic.data.access.resolver.CardSerializer;
import org.cmdbuild.logic.data.access.resolver.ForeignReferenceResolver;
import org.cmdbuild.model.data.Card;
import org.cmdbuild.model.data.IdentifiedRelation;
import org.cmdbuild.servlets.json.management.dataimport.csv.CSVData;
import org.cmdbuild.servlets.json.management.dataimport.csv.CSVImporter;
import org.cmdbuild.servlets.json.management.export.CMDataSource;
import org.cmdbuild.servlets.json.management.export.DBDataSource;
import org.cmdbuild.servlets.json.management.export.DataExporter;
import org.cmdbuild.servlets.json.management.export.csv.CsvExporter;
import org.json.JSONException;
import org.springframework.transaction.annotation.Transactional;
import org.supercsv.prefs.CsvPreference;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class DefaultDataAccessLogic implements DataAccessLogic {

	protected static final Alias DOM_ALIAS = NameAlias.as("DOM");
	protected static final Alias DST_ALIAS = NameAlias.as("DST");

	private static final Function<CMCard, Card> CMCARD_TO_CARD = new Function<CMCard, Card>() {
		@Override
		public Card apply(final CMCard input) {
			return CardStorableConverter.of(input).convert(input);
		}
	};

	private final CMDataView view;
	private final OperationUser operationUser;
	private final LockCardManager lockCardManager;

	public DefaultDataAccessLogic(final CMDataView view, final OperationUser operationUser,
			final LockCardManager lockCardManager) {
		this.view = view;
		this.operationUser = operationUser;
		this.lockCardManager = lockCardManager;
	}

	@Override
	public CMDataView getView() {
		return view;
	}

	private DataViewStore<Card> storeOf(final Card card) {
		return new DataViewStore<Card>(view, CardStorableConverter.of(card));
	}

	@Override
	public Map<Object, List<RelationInfo>> relationsBySource(final String sourceTypeName, final DomainWithSource dom) {
		return new GetRelationList(view).list(sourceTypeName, dom);
	}

	@Override
	public GetRelationListResponse getRelationList(final Card srcCard, final DomainWithSource dom,
			final QueryOptions options) {
		return new GetRelationList(view).exec(srcCard, dom, options);
	}

	@Override
	public GetRelationListResponse getRelationList(final Card srcCard, final DomainWithSource dom) {
		return new GetRelationList(view).exec(srcCard, dom, QueryOptions.newQueryOption().build());
	}

	@Override
	public GetRelationHistoryResponse getRelationHistory(final Card srcCard) {
		return new GetRelationHistory(view).exec(srcCard);
	}

	@Override
	public GetRelationHistoryResponse getRelationHistory(final Card srcCard, final CMDomain domain) {
		return new GetRelationHistory(view).exec(srcCard, domain);
	}

	@Override
	public GetCardHistoryResponse getCardHistory(final Card srcCard) {
		return new GetCardHistory(view).exec(srcCard);
	}

	@Override
	public CMClass findClass(final Long classId) {
		final CMClass fetchedClass = view.findClass(classId);
		if (fetchedClass == null) {
			throw NotFoundException.NotFoundExceptionType.CLASS_NOTFOUND.createException();
		}
		return fetchedClass;
	}

	@Override
	public CMClass findClass(final String className) {
		return view.findClass(className);
	}

	@Override
	public CMDomain findDomain(final Long domainId) {
		return view.findDomain(domainId);
	}

	@Override
	public CMDomain findDomain(final String domainName) {
		return view.findDomain(domainName);
	}

	/**
	 * 
	 * @return only active classes (all classes, included superclasses, simple
	 *         classes and process classes).
	 */
	@Override
	public Iterable<? extends CMClass> findActiveClasses() {
		return filterActive(view.findClasses());
	}

	/**
	 * 
	 * @return active and non active domains
	 */
	@Override
	public Iterable<? extends CMDomain> findAllDomains() {
		return view.findDomains();
	}

	/**
	 * 
	 * @return only active domains
	 */
	@Override
	public Iterable<? extends CMDomain> findActiveDomains() {
		final Iterable<? extends CMDomain> activeDomains = filterActive(view.findDomains());
		return activeDomains;
	}

	@Override
	public Iterable<? extends CMDomain> findReferenceableDomains(final String className) {
		final List<CMDomain> referenceableDomains = Lists.newArrayList();
		final CMClass fetchedClass = view.findClass(className);
		for (final CMDomain domain : view.findDomainsFor(fetchedClass)) {
			if (isReferenceableDomain(domain, fetchedClass)) {
				referenceableDomains.add(domain);
			}
		}
		return referenceableDomains;
	}

	private static boolean isReferenceableDomain(final CMDomain domain, final CMClass cmClass) {
		final String cardinality = domain.getCardinality();
		if (cardinality.equals(CARDINALITY_1N.value()) && domain.getClass2().isAncestorOf(cmClass)) {
			return true;
		} else if (cardinality.equals(CARDINALITY_N1.value()) && domain.getClass1().isAncestorOf(cmClass)) {
			return true;
		}
		return false;
	}

	/**
	 * 
	 * @return active and non active classes
	 */
	@Override
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
	@Override
	public Card fetchCard(final String className, final Long cardId) {
		final CMClass entryType = view.findClass(className);
		try {
			final CMQueryRow row = view.select(anyAttribute(entryType)) //
					.from(entryType) //
					.where(condition(attribute(entryType, ID_ATTRIBUTE), eq(cardId))) //
					.run() //
					.getOnlyRow();
			/**
			 * FIXME: delete it when ForeignReferenceResolver will be unused
			 */
//			final Iterable<CMCard> cards = ForeignReferenceResolver.<CMCard> newInstance() //
//					.withSystemDataView(applicationContext().getBean("dbDataView", CMDataView.class)) //
//					.withEntryType(entryType) //
//					.withEntries(asList(row.getCard(entryType))) //
//					.withEntryFiller(new CardEntryFiller()) //
//					.withLookupStore(applicationContext().getBean(LookupStore.class)) //
//					.withSerializer(new CardSerializer<CMCard>()) //
//					.build() //
//					.resolve();
			final Iterable<CMCard> cards = asList(row.getCard(entryType));
			return from(cards) //
					.transform(CMCARD_TO_CARD) //
					.iterator() //
					.next();
		} catch (final NoSuchElementException ex) {
			throw NotFoundException.NotFoundExceptionType.CARD_NOTFOUND.createException(className);
		}
	}

	@Override
	public Card fetchCardShort(final String className, final Long cardId, final QueryOptions queryOptions) {
		final CMClass entryType = view.findClass(className);
		final List<QueryAliasAttribute> attributesToDisplay = Lists.newArrayList();

		for (int i = 0; i < queryOptions.getAttributes().length(); i++) {
			try {
				final QueryAliasAttribute queryAttribute = attribute(entryType,
						queryOptions.getAttributes().getString(i));
				attributesToDisplay.add(queryAttribute);
			} catch (final JSONException e) {
				// do nothing for now
			}
		}

		try {
			final CMQueryRow row = view.select(attributesToDisplay.toArray()) //
					.from(entryType) //
					.where(condition(attribute(entryType, ID_ATTRIBUTE), eq(cardId))) //
					.run() //
					.getOnlyRow();

			final CMCard card = row.getCard(entryType);
			final CMCard cardWithResolvedReference = resolveCardReferences(entryType, card);

			return CMCARD_TO_CARD.apply(cardWithResolvedReference);

		} catch (final NoSuchElementException ex) {
			throw NotFoundException.NotFoundExceptionType.CARD_NOTFOUND.createException();
		}
	}

	/**
	 * @param entryType
	 * @param card
	 * @return
	 */
	@Override
	public CMCard resolveCardReferences( //
			final CMClass entryType,
			final CMCard card //
		) {

		final Iterable<CMCard> cardWithResolvedReference = ForeignReferenceResolver.<CMCard> newInstance() //
				.withSystemDataView(applicationContext().getBean("dbDataView", CMDataView.class)) //
				.withEntryType(entryType) //
				.withEntries(asList(card)) //
				.withEntryFiller(new CardEntryFiller()) //
				.withLookupStore(applicationContext().getBean(LookupStore.class)) //
				.withSerializer(new CardSerializer<CMCard>()) //
				.build() //
				.resolve();

		return cardWithResolvedReference.iterator().next();
	}

	@Override
	public Card fetchCard(final Long classId, final Long cardId) {
		final CMClass entryType = view.findClass(classId);
		return fetchCard(entryType.getIdentifier().getLocalName(), cardId);
	}

	/**
	 * Retrieve the cards of a given class that matches the given query options
	 * 
	 * @param className
	 * @param queryOptions
	 * @return a FetchCardListResponse
	 */
	@Override
	public FetchCardListResponse fetchCards(final String className, final QueryOptions queryOptions) {
		/*
		 * preferred solution to avoid pre-release errors
		 */
		if (className != null) {
			return fetchCardsWithClassName(className, queryOptions);
		} else {
			return fetchCardsWithoutClassName(queryOptions);
		}
	}

	private FetchCardListResponse fetchCardsWithClassName(final String className, final QueryOptions queryOptions) {
		final CMClass fetchedClass = view.findClass(className);
		final PagedElements<CMCard> fetchedCards;
		final Iterable<Card> cards;
		if (fetchedClass != null) {
			fetchedCards = DataViewCardFetcher.newInstance() //
					.withDataView(view) //
					.withClassName(className) //
					.withQueryOptions(queryOptions) //
					.build() //
					.fetch();

			cards = resolveCardForeignReferences(fetchedClass, fetchedCards);

		} else {
			cards = Collections.emptyList();
			fetchedCards = new PagedElements<CMCard>(Collections.<CMCard> emptyList(), 0);
		}
		return new FetchCardListResponse(cards, fetchedCards.totalSize());
	}

//	/**
//	 * @param fetchedClass CMClass
//	 * @param fetchedCards PagedElements<CMCard>
//	 * @return
//	 */
//	private Iterable<CMCard> resolveCMCardForeignReferences(
//			final CMClass fetchedClass, final PagedElements<CMCard> fetchedCards) {
//		final Iterable<CMCard> cardsWithForeingReferences = ForeignReferenceResolver.<CMCard> newInstance() //
//				.withSystemDataView(applicationContext().getBean(DBDataView.class)) //
//				.withEntryType(fetchedClass) //
//				.withEntries(fetchedCards) //
//				.withEntryFiller(new CardEntryFiller()) //
//				.withLookupStore(applicationContext().getBean(LookupStore.class)) //
//				.withSerializer(new CardSerializer<CMCard>()) //
//				.build() //
//				.resolve();
//		return cardsWithForeingReferences;
//	}

	public Iterable<Card> resolveCardForeignReferences(final CMClass fetchedClass, final PagedElements<CMCard> fetchedCards) {
//		Iterable<CMCard> cardsWithForeingReferences = resolveCMCardForeignReferences(fetchedClass, fetchedCards);
		
		Iterable<CMCard> cardsWithForeingReferences = fetchedCards.elements();
		return from(cardsWithForeingReferences) //
				.transform(CMCARD_TO_CARD);
	}

	private FetchCardListResponse fetchCardsWithoutClassName(final QueryOptions queryOptions) {
		final PagedElements<CMCard> fetchedCards = DataViewCardFetcher.newInstance() //
				.withDataView(view) //
				.withQueryOptions(queryOptions) //
				.build() //
				.fetch();

		final Iterable<CMCard> cardsWithForeingReferences = ForeignReferenceResolver.<CMCard> newInstance() //
				.withSystemDataView(applicationContext().getBean(DBDataView.class)) //
				.withEntries(fetchedCards) //
				.withEntryFiller(new CardEntryFiller()) //
				.withLookupStore(applicationContext().getBean(LookupStore.class)) //
				.withSerializer(new CardSerializer<CMCard>()) //
				.build() //
				.resolve();

		final Iterable<Card> cards = from(cardsWithForeingReferences) //
				.transform(CMCARD_TO_CARD);

		return new FetchCardListResponse(cards, fetchedCards.totalSize());
	}

	/**
	 * Execute a given SQL function to select a set of rows Return these rows as
	 * fake cards
	 * 
	 * @param functionName
	 * @param queryOptions
	 * @return
	 */
	@Override
	public FetchCardListResponse fetchSQLCards(final String functionName, final QueryOptions queryOptions) {
		final CMFunction fetchedFunction = view.findFunctionByName(functionName);
		final Alias functionAlias = NameAlias.as("f");

		if (fetchedFunction == null) {
			final List<Card> emptyCardList = Collections.emptyList();
			return new FetchCardListResponse(emptyCardList, 0);
		}

		final CMQueryResult queryResult = new DataViewCardFetcher.SqlQuerySpecsBuilderBuilder() //
				.withDataView(view) //
				.withQueryOptions(queryOptions) //
				.withFunction(fetchedFunction) //
				.withAlias(functionAlias) //
				.build() //
				.run();
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

	/**
	 * 
	 * @param className
	 * @param cardId
	 * @param queryOptions
	 * @return a long (zero based) with the position of this card in relation of
	 *         current sorting and filter
	 */
	@Override
	public Long getCardPosition(final String className, final Long cardId, final QueryOptions queryOptions) {
		final CMClass fetchedClass = view.findClass(className);
		Long position = 0L;
		try {
			final PagedElements<CMQueryRow> cards = DataViewCardFetcher.newInstance() //
					.withClassName(className) //
					.withQueryOptions(queryOptions) //
					.withDataView(view) //
					.build() //
					.fetchNumbered(condition(attribute(fetchedClass, ID_ATTRIBUTE), eq(cardId)));
			final CMQueryRow fetchedRowWithPosition = cards.iterator().next();
			position = fetchedRowWithPosition.getNumber() - 1;
		} catch (final Exception ex) {
			Log.CMDBUILD.error("Cannot calculate the position for card with id " + cardId + " from class " + className);
		}
		return position;
	}

	@Override
	@Transactional
	public Long createCard(final Card card) {
		final CMClass entryType = view.findClass(card.getClassName());
		if (entryType == null) {
			throw NotFoundException.NotFoundExceptionType.CLASS_NOTFOUND.createException();
		}

		final Store<Card> store = storeOf(card);
		final Storable created = store.create(card);

		updateRelationAttributesFromReference( //
				Long.valueOf(created.getIdentifier()), card, entryType);

		return Long.valueOf(created.getIdentifier());
	}

	@Override
	public void updateCard(final Card card) {
		final String currentlyLoggedUser = operationUser.getAuthenticatedUser().getUsername();
		lockCardManager.checkLockerUser(card.getId(), currentlyLoggedUser);

		final CMClass entryType = view.findClass(card.getClassName());
		if (entryType == null) {
			throw NotFoundException.NotFoundExceptionType.CLASS_NOTFOUND.createException();
		}

		final Store<Card> store = storeOf(card);
		final Card currentCard = store.read(card);
		final Card updatedCard = Card.newInstance() //
				.clone(currentCard) //
				.withAllAttributes(card.getAttributes()) //
				.withUser(card.getUser()) //
				.build();
		store.update(updatedCard);

		updateRelationAttributesFromReference(updatedCard.getId(), card, entryType);

		lockCardManager.unlock(card.getId());
	}

	private void updateRelationAttributesFromReference(final Long storedCardId, final Card card, final CMClass entryType) {
		final Map<String, Object> cardAttributes = card.getAttributes();

		for (final CMAttribute attribute : entryType.getActiveAttributes()) {
			if (attribute.getType() instanceof ReferenceAttributeType) {
				final String referenceAttributeName = attribute.getName();

				// retrieve the reference value
				final Object referencedCardIdObject = cardAttributes.get(referenceAttributeName);
				final Long referencedCardId;
				if (referencedCardIdObject == null || "".equals(referencedCardIdObject)) {
					continue;
				} else if (referencedCardIdObject instanceof String) {
					referencedCardId = Long.parseLong((String)referencedCardIdObject);
				} else if (referencedCardIdObject instanceof Long) {
					referencedCardId = (Long) referencedCardIdObject;
				} else {
					throw new UnsupportedOperationException("A reference could have a String or a Long value");
				}

				// retrieve the relation attributes
				final String domainName = ((ReferenceAttributeType) attribute.getType()).getDomainName();
				final CMDomain domain = view.findDomain(domainName);
				final Map<String, Object> relationAttributes = Maps.newHashMap();
				for (final CMAttribute domainAttribute : domain.getAttributes()) {
					final String domainAttributeName = String.format("_%s_%s", referenceAttributeName,
							domainAttribute.getName());
					final Object domainAttributeValue = cardAttributes.get(domainAttributeName);
					relationAttributes.put(domainAttribute.getName(), domainAttributeValue);
				}

				// update the attributes if needed
				final CMClass sourceClass = domain.getClass1();
				final CMClass destinationClass = domain.getClass2();
				final Long sourceCardId, destinationCardId;

				if (sourceClass.isAncestorOf(view.findClass(card.getClassName()))) {
					sourceCardId = storedCardId;
					destinationCardId = referencedCardId;
				} else {
					sourceCardId = referencedCardId;
					destinationCardId = storedCardId;
				}

				final CMCard fetchedSourceCard = fetchCardForClassAndId(sourceClass.getName(), sourceCardId);
				final CMCard fetchedDestinationCard = fetchCardForClassAndId(destinationClass.getName(),
						destinationCardId);

				final CMRelation relation = getRelation(sourceCardId, destinationCardId, domain, sourceClass,
						destinationClass);

				boolean updateRelationNeeded = areRelationAttributesModified(relation.getValues(), relationAttributes,
						domain);

				if (updateRelationNeeded) {
					final CMRelationDefinition mutableRelation = view.update(relation) //
							.setCard1(fetchedSourceCard) //
							.setCard2(fetchedDestinationCard); //
					updateRelationDefinitionAttributes(relationAttributes, mutableRelation);
					mutableRelation.update();
				}
			}
		}
	}

	private boolean areRelationAttributesModified(final Iterable<Entry<String, Object>> oldValues,
			final Map<String, Object> newValues, final CMDomain domain) {
		for (Entry<String, Object> oldEntry : oldValues) {
			final String attributeName = oldEntry.getKey();
			final Object oldAttributeValue = oldEntry.getValue();
			final CMAttributeType<?> attributeType = domain.getAttribute(attributeName).getType();
			final Object newValueConverted = attributeType.convertValue(newValues.get(attributeName));
			if (!ObjectUtils.equals(newValueConverted, oldAttributeValue)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public void updateFetchedCard(final Card card, final Map<String, Object> attributes) {
		if (card != null) {
			final Card updatedCard = Card.newInstance() //
					.clone(card) //
					.withAllAttributes(attributes) //
					.build();
			storeOf(updatedCard).update(updatedCard);
		}
	}

	@Override
	@Transactional
	public void deleteCard(final String className, final Long cardId) {
		lockCardManager.checkLocked(cardId);

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
	@Override
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
	@Override
	public boolean isProcess(final CMClass target) {
		final CMClass activity = view.getActivityClass();
		return activity.isAncestorOf(target);
	}

	/**
	 * Relations.... move the following code to another class
	 */

	@Override
	@Transactional
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
		try {
			mutableRelation.create();
		} catch (RuntimeException ex) {
			throw ORMExceptionType.ORM_ERROR_RELATION_CREATE.createException();
		}
	}

	@Override
	@Transactional
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
		final CMCard fetchedSrcCard = fetchCardForClassAndId(srcClassName, srcCardId);
		final CMClass dstClass = view.findClass(dstClassName);

		CMQueryRow row;
		WhereClause whereCondition;
		CMClass directedSource;

		final Alias destinationAlias = as(DST_ALIAS);
		final Alias domainAlias = as(DOM_ALIAS);

		if (relationDTO.master.equals("_1")) {
			directedSource = srcClass;
			whereCondition = and( //
					condition(attribute(srcClass, ID_ATTRIBUTE), eq(srcCardId)), //
					and( //
					condition(attribute(domainAlias, ID_ATTRIBUTE), eq(relationDTO.relationId)), //
							condition(attribute(domainAlias, "_Src"), eq("_1")) //
					));
		} else {
			directedSource = dstClass;
			whereCondition = and( //
					condition(attribute(dstClass, ID_ATTRIBUTE), eq(dstCardId)), //
					and( //
					condition(attribute(domainAlias, ID_ATTRIBUTE), eq(relationDTO.relationId)), //
							condition(attribute(domainAlias, "_Src"), eq("_2"))));
		}

		row = view.select(anyAttribute(directedSource)) //
				.from(directedSource) //
				.join(anyClass(), destinationAlias, over(domain, domainAlias)) //
				.where(whereCondition).run().getOnlyRow(); //

		final CMRelation relation = row.getRelation(domainAlias).getRelation();
		final CMRelationDefinition mutableRelation = view.update(relation) //
				.setCard1(fetchedSrcCard) //
				.setCard2(fetchedDstCard);

		updateRelationDefinitionAttributes(relationDTO.relationAttributeToValue, mutableRelation);

		mutableRelation.update();
	}

	private void updateRelationDefinitionAttributes(final Map<String, Object> attributeToValue,
			final CMRelationDefinition relDefinition) {

		for (final Entry<String, Object> entry : attributeToValue.entrySet()) {
			relDefinition.set(entry.getKey(), entry.getValue());
		}
	}

	@Override
	@Transactional
	public void deleteRelation(final String domainName, final Long relationId) {
		final CMDomain domain = view.findDomain(domainName);
		if (domain == null) {
			throw NotFoundException.NotFoundExceptionType.DOMAIN_NOTFOUND.createException();
		}

		view.delete(new IdentifiedRelation(domain, relationId));
	}

	@Override
	public void deleteRelation( //
			final String srcClassName, //
			final Long srcCardId, //
			final String dstClassName, //
			final Long dstCardId, //
			final CMDomain domain) {
		final CMClass sourceClass = view.findClass(srcClassName);
		final CMClass destinationClass = view.findClass(dstClassName);
		final CMRelation relation = getRelation(srcCardId, dstCardId, domain, sourceClass, destinationClass);
		view.delete(relation);
	}

	@Override
	public CMRelation getRelation(final Long srcCardId, final Long dstCardId, final CMDomain domain,
			final CMClass sourceClass, final CMClass destinationClass) {
		/**
		 * The destination alias is mandatory in order to support also
		 * reflective domains
		 */
		final Alias destinationAlias = NameAlias.as(String.format("DST-%s-%s", destinationClass.getName(),
				RandomStringUtils.randomAscii(10)));
		final CMQueryRow row = view.select(anyAttribute(sourceClass), anyAttribute(domain)) //
				.from(sourceClass) //
				.join(destinationClass, destinationAlias, over(domain)) //
				.where( //
				and( //
				condition( //
						attribute(sourceClass, ID_ATTRIBUTE), eq(srcCardId)), //
						condition(attribute(destinationAlias, ID_ATTRIBUTE), eq(dstCardId)) //
				)).run().getOnlyRow();

		final CMRelation relation = row.getRelation(domain).getRelation();
		return relation;
	}

	@Override
	@Transactional
	public void deleteDetail(final Card master, final Card detail, final String domainName) {
		final CMDomain domain = view.findDomain(domainName);
		if (domain == null) {
			throw NotFoundException.NotFoundExceptionType.DOMAIN_NOTFOUND.createException();
		}

		String sourceClassName, destinationClassName;
		Long sourceCardId, destinationCardId;

		if (CARDINALITY_1N.value().equals(domain.getCardinality())) {
			sourceClassName = master.getClassName();
			sourceCardId = master.getId();
			destinationClassName = detail.getClassName();
			destinationCardId = detail.getId();
		} else if (CARDINALITY_N1.value().equals(domain.getCardinality())) {
			sourceClassName = detail.getClassName();
			sourceCardId = detail.getId();
			destinationClassName = master.getClassName();
			destinationCardId = master.getId();
		} else {
			throw new UnsupportedOperationException("You are tring to delete a detail over a N to N domain");
		}

		deleteRelation(sourceClassName, sourceCardId, destinationClassName, destinationCardId, domain);
		deleteCard(detail.getClassName(), detail.getId());
	}

	@Override
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

	@Override
	public CSVData importCsvFileFor(final FileItem csvFile, final Long classId, final String separator)
			throws IOException {
		final CMClass destinationClassForImport = view.findClass(classId);
		final int separatorInt = separator.charAt(0);
		final CsvPreference importCsvPreferences = new CsvPreference('"', separatorInt, "\n");
		final CSVImporter csvImporter = new CSVImporter( //
				view, //
				applicationContext().getBean(LookupStore.class), //
				destinationClassForImport, //
				importCsvPreferences //
			);

		final CSVData csvData = csvImporter.getCsvDataFrom(csvFile);
		return csvData;
	}

	private CMCard fetchCardForClassAndId(final String className, final Long cardId) {
		final CMClass entryType = view.findClass(className);
		final CMQueryRow row;
		try {
			row = view.select(anyAttribute(entryType)) //
					.from(entryType) //
					.where(condition(attribute(entryType, ID_ATTRIBUTE), eq(cardId))) //
					.run() //
					.getOnlyRow();
		} catch (final NoSuchElementException ex) {
			throw NotFoundException.NotFoundExceptionType.CARD_NOTFOUND.createException();
		}
		final CMCard card = row.getCard(entryType);
		return card;
	}

	@Override
	public void lockCard(final Long cardId) {
		this.lockCardManager.lock(cardId);
	}

	@Override
	public void unlockCard(final Long cardId) {
		this.lockCardManager.unlock(cardId);
	}

	@Override
	public void unlockAllCards() {
		this.lockCardManager.unlockAll();
	}
}
