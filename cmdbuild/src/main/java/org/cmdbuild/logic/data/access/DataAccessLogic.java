package org.cmdbuild.logic.data.access;

import static com.google.common.collect.FluentIterable.from;
import static java.util.Arrays.asList;
import static org.cmdbuild.dao.driver.postgres.Const.ID_ATTRIBUTE;
import static org.cmdbuild.dao.entrytype.Deactivable.IsActivePredicate.filterActive;
import static org.cmdbuild.dao.query.clause.AnyAttribute.anyAttribute;
import static org.cmdbuild.dao.query.clause.QueryAliasAttribute.attribute;
import static org.cmdbuild.dao.query.clause.join.Over.over;
import static org.cmdbuild.dao.query.clause.where.AndWhereClause.and;
import static org.cmdbuild.dao.query.clause.where.EqualsOperatorAndValue.eq;
import static org.cmdbuild.dao.query.clause.where.SimpleWhereClause.condition;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;

import org.apache.commons.fileupload.FileItem;
import org.cmdbuild.common.utils.PagedElements;
import org.cmdbuild.dao.entry.CMCard;
import org.cmdbuild.dao.entry.CMRelation;
import org.cmdbuild.dao.entry.CMRelation.CMRelationDefinition;
import org.cmdbuild.dao.entry.ForwardingCard;
import org.cmdbuild.dao.entrytype.CMAttribute;
import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.dao.entrytype.CMDomain;
import org.cmdbuild.dao.function.CMFunction;
import org.cmdbuild.dao.query.CMQueryResult;
import org.cmdbuild.dao.query.CMQueryRow;
import org.cmdbuild.dao.query.QuerySpecsBuilder;
import org.cmdbuild.dao.query.clause.alias.Alias;
import org.cmdbuild.dao.query.clause.alias.NameAlias;
import org.cmdbuild.dao.query.clause.where.WhereClause;
import org.cmdbuild.dao.view.CMDataView;
import org.cmdbuild.data.store.DataViewStore;
import org.cmdbuild.data.store.Store;
import org.cmdbuild.data.store.Store.Storable;
import org.cmdbuild.exception.NotFoundException;
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
import org.cmdbuild.logic.data.access.ForeignReferenceResolver.EntryFiller;
import org.cmdbuild.logic.data.access.lock.LockCardManager;
import org.cmdbuild.logic.mapping.FilterMapper;
import org.cmdbuild.logic.mapping.json.JsonFilterMapper;
import org.cmdbuild.model.data.Card;
import org.cmdbuild.servlets.json.management.dataimport.csv.CsvData;
import org.cmdbuild.servlets.json.management.dataimport.csv.CsvImporter;
import org.cmdbuild.servlets.json.management.export.CMDataSource;
import org.cmdbuild.servlets.json.management.export.DBDataSource;
import org.cmdbuild.servlets.json.management.export.DataExporter;
import org.cmdbuild.servlets.json.management.export.csv.CsvExporter;
import org.supercsv.prefs.CsvPreference;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 * Business Logic Layer for Data Access
 */
public class DataAccessLogic implements Logic {

	private static final Function<CMCard, Card> CMCARD_TO_CARD = new Function<CMCard, Card>() {
		@Override
		public Card apply(final CMCard input) {
			return CardStorableConverter.of(input).convert(input);
		}
	};

	private final LockCardManager lockCardManager;
	private final CMDataView view;

	public DataAccessLogic(final CMDataView view, final LockCardManager lockCardManager) {
		this.view = view;
		this.lockCardManager = lockCardManager;
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
			final Iterable<CMCard> cards = ForeignReferenceResolver.<CMCard> newInstance() //
					.withSystemDataView(TemporaryObjectsBeforeSpringDI.getSystemView()) //
					.withEntryType(entryType) //
					.withEntries(asList(row.getCard(entryType))) //
					.withEntryFiller(cardFiller()) //
					.build() //
					.resolve();
			return from(cards) //
					.transform(CMCARD_TO_CARD) //
					.iterator() //
					.next();
		} catch (final NoSuchElementException ex) {
			throw NotFoundException.NotFoundExceptionType.CARD_NOTFOUND.createException();
		}
	}

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
	public FetchCardListResponse fetchCards(final String className, final QueryOptions queryOptions) {
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
			final Iterable<CMCard> cardsWithForeingReferences = ForeignReferenceResolver.<CMCard> newInstance() //
					.withSystemDataView(TemporaryObjectsBeforeSpringDI.getSystemView()) //
					.withEntryType(view.findClass(className)) //
					.withEntries(fetchedCards) //
					.withEntryFiller(cardFiller()) //
					.build() //
					.resolve();
			cards = from(cardsWithForeingReferences) //
					.transform(CMCARD_TO_CARD);
		} else {
			cards = Collections.emptyList();
			fetchedCards = new PagedElements<CMCard>(Collections.<CMCard> emptyList(), 0);
		}
		return new FetchCardListResponse(cards, fetchedCards.totalSize());
	}

	private EntryFiller<CMCard> cardFiller() {
		return new EntryFiller<CMCard>() {

			private final Map<String, Object> values = Maps.newHashMap();
			private CMCard input;

			@Override
			public void setInput(final CMCard input) {
				this.input = input;
			}

			@Override
			public void setValue(final String name, final Object value) {
				values.put(name, value);
			}

			@Override
			public CMCard getOutput() {
				return new ForwardingCard(input) {

					@Override
					public Iterable<Entry<String, Object>> getAllValues() {
						return values.entrySet();
					}

					@Override
					public Iterable<Entry<String, Object>> getValues() {
						return from(getAllValues()) //
								.filter(new Predicate<Map.Entry<String, Object>>() {
									@Override
									public boolean apply(final Entry<String, Object> input) {
										final String name = input.getKey();
										final CMAttribute attribute = getType().getAttribute(name);
										return (attribute != null) && !attribute.isSystem();
									}
								});
					}

				};
			}

		};
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
	public Long getCardPosition(final String className, final Long cardId, final QueryOptions queryOptions) {
		final CMClass fetchedClass = view.findClass(className);

		final FilterMapper filterMapper = new JsonFilterMapper(fetchedClass, queryOptions.getFilter(), view);
		final WhereClause whereClause = filterMapper.whereClause();
		final QuerySpecsBuilder queryBuilder = view.select(anyAttribute(fetchedClass)) //
				.from(fetchedClass) //
				.where(whereClause) //
				.numbered(condition(attribute(fetchedClass, ID_ATTRIBUTE), eq(cardId)));

		// TODO make it better, maybe some utility class
		DataViewCardFetcher.QuerySpecsBuilderBuilder.addSortingOptions(queryBuilder, queryOptions, fetchedClass);

		final CMQueryRow row = queryBuilder.run().getOnlyRow();
		final Long n = row.getNumber() - 1;

		return n;
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
		final String currentlyLoggedUser = TemporaryObjectsBeforeSpringDI.getOperationUser().getAuthenticatedUser()
				.getUsername();
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
				.build();
		store.update(updatedCard);

		lockCardManager.unlock(card.getId());
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
		final CMCard fetchedSrcCard = fetchCardForClassAndId(srcClassName, srcCardId);
		final CMClass dstClass = view.findClass(dstClassName);
		CMQueryRow row;
		if (relationDTO.master.equals("_1")) {
			row = view.select(anyAttribute(srcClass), anyAttribute(domain))//
					.from(srcClass) //
					.join(dstClass, over(domain)) //
					.where(and(condition(attribute(srcClass, ID_ATTRIBUTE), eq(srcCardId)), //
							condition(attribute(domain, ID_ATTRIBUTE), eq(relationDTO.relationId)))) //
					.run().getOnlyRow();
		} else {
			row = view.select(anyAttribute(dstClass), anyAttribute(domain)) //
					.from(dstClass) //
					.join(srcClass, over(domain)) //
					.where(and(condition(attribute(dstClass, ID_ATTRIBUTE), eq(dstCardId)), //
							condition(attribute(domain, ID_ATTRIBUTE), eq(relationDTO.relationId)))) //
					.run().getOnlyRow();
		}
		final CMRelation relation = row.getRelation(domain).getRelation();
		final CMRelationDefinition mutableRelation = view.update(relation) //
				.setCard1(fetchedSrcCard) //
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
				.where(and(condition(attribute(srcClass, ID_ATTRIBUTE), eq(srcCardId)), //
						condition(attribute(domain, ID_ATTRIBUTE), eq(relationDTO.relationId)))) //
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
					.where(condition(attribute(entryType, ID_ATTRIBUTE), eq(cardId))) //
					.run() //
					.getOnlyRow();
		} catch (final NoSuchElementException ex) {
			throw NotFoundException.NotFoundExceptionType.CARD_NOTFOUND.createException();
		}
		final CMCard card = row.getCard(entryType);
		return card;
	}

	public void lockCard(final Long cardId) {
		this.lockCardManager.lock(cardId);
	}

	public void unlockCard(final Long cardId) {
		this.lockCardManager.unlock(cardId);
	}

	public void unlockAllCards() {
		this.lockCardManager.unlockAll();
	}
}
