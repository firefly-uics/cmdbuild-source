package org.cmdbuild.logic.data;

import static com.google.common.collect.Iterables.isEmpty;
import static org.cmdbuild.dao.query.clause.AnyAttribute.anyAttribute;
import static org.cmdbuild.dao.query.clause.QueryAliasAttribute.attribute;
import static org.cmdbuild.dao.query.clause.alias.Alias.canonicalAlias;
import static org.cmdbuild.dao.query.clause.join.Over.over;
import static org.cmdbuild.dao.query.clause.where.AndWhereClause.and;
import static org.cmdbuild.dao.query.clause.where.EqualsOperatorAndValue.eq;
import static org.cmdbuild.dao.query.clause.where.SimpleWhereClause.condition;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;

import org.cmdbuild.common.collect.Mapper;
import org.cmdbuild.dao.entry.CMCard;
import org.cmdbuild.dao.entry.CMCard.CMCardDefinition;
import org.cmdbuild.dao.entry.CMRelation;
import org.cmdbuild.dao.entry.CMRelation.CMRelationDefinition;
import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.dao.entrytype.CMDomain;
import org.cmdbuild.dao.entrytype.CMEntryType;
import org.cmdbuild.dao.query.CMQueryResult;
import org.cmdbuild.dao.query.CMQueryRow;
import org.cmdbuild.dao.query.QuerySpecsBuilder;
import org.cmdbuild.dao.query.clause.OrderByClause;
import org.cmdbuild.dao.query.clause.QueryAliasAttribute;
import org.cmdbuild.dao.query.clause.where.WhereClause;
import org.cmdbuild.dao.view.CMDataView;
import org.cmdbuild.exception.NotFoundException;
import org.cmdbuild.logic.Logic;
import org.cmdbuild.logic.LogicDTO.Card;
import org.cmdbuild.logic.LogicDTO.DomainWithSource;
import org.cmdbuild.logic.commands.AbstractGetRelation.RelationInfo;
import org.cmdbuild.logic.commands.GetRelationHistory;
import org.cmdbuild.logic.commands.GetRelationHistory.GetRelationHistoryResponse;
import org.cmdbuild.logic.commands.GetRelationList;
import org.cmdbuild.logic.commands.GetRelationList.GetRelationListResponse;
import org.cmdbuild.logic.mapping.FilterMapper;
import org.cmdbuild.logic.mapping.SorterMapper;
import org.cmdbuild.logic.mapping.json.JsonFilterMapper;
import org.cmdbuild.logic.mapping.json.JsonSorterMapper;
import org.json.JSONArray;
import org.json.JSONException;

import com.google.common.collect.Lists;

/**
 * Business Logic Layer for Data Access
 */
public class DataAccessLogic implements Logic {

	public static class FetchCardListResponse implements Iterable<CMCard> {

		private final Iterable<CMCard> fetchedCards;
		private final int totalSize; // for pagination

		private FetchCardListResponse(final Iterable<CMCard> cards, final int totalSize) {
			this.totalSize = totalSize;
			this.fetchedCards = cards;
		}

		@Override
		public Iterator<CMCard> iterator() {
			return fetchedCards.iterator();
		}

		public Iterable<CMCard> getPaginatedCards() {
			return fetchedCards;
		}

		public int getTotalNumberOfCards() {
			return totalSize;
		}

	}

	private static class JsonAttributeSubsetMapper implements Mapper<JSONArray, List<QueryAliasAttribute>> {

		private final CMEntryType entryType;

		private JsonAttributeSubsetMapper(final CMEntryType entryType) {
			this.entryType = entryType;
		}

		@Override
		public List<QueryAliasAttribute> map(final JSONArray jsonAttributes) {
			if (jsonAttributes.length() == 0) {
				return Lists.newArrayList();
			}
			final List<QueryAliasAttribute> attributeSubset = Lists.newArrayList();
			for (int i = 0; i < jsonAttributes.length(); i++) {
				try {
					final String attributeName = jsonAttributes.getString(i);
					if (entryType.getAttribute(attributeName) != null) {
						final QueryAliasAttribute attr = attribute(entryType, attributeName);
						attributeSubset.add(attr);
					}
				} catch (final JSONException ex) {
					logger.error("Cannot read attribute...");
				}
			}
			return attributeSubset;
		}
	}

	public static class CardDTO {

		private final Long id;
		private final String className;
		private final Map<String, Object> attributes;

		public CardDTO(final Long id, final String className, final Map<String, Object> attributes) {
			this.id = id;
			this.className = className;
			this.attributes = attributes;
		}

		public Long getId() {
			return id;
		}

		public String getClassName() {
			return className;
		}

		public Map<String, Object> getAttributes() {
			return attributes;
		}
	}

	public static class RelationDTO {

		public String domainName;
		public String master;
		public Map<Long, String> srcCardIdToClassName;
		public Map<Long, String> dstCardIdToClassName;
		public Map<String, Object> relationAttributeToValue;
		public Long relationId;

		public RelationDTO() {
		}

		public void addSourceCardToClass(final Long srcCardId, final String srcClassName) {
			srcCardIdToClassName.put(srcCardId, srcClassName);
		}

		public void addDestinationCardToClass(final Long dstCardId, final String dstClassName) {
			dstCardIdToClassName.put(dstCardId, dstClassName);
		}

		public Entry<Long, String> getUniqueEntryForSourceCard() {
			for (final Entry<Long, String> entry : srcCardIdToClassName.entrySet()) {
				return entry;
			}
			return null;
		}

		public Entry<Long, String> getUniqueEntryForDestinationCard() {
			for (final Entry<Long, String> entry : dstCardIdToClassName.entrySet()) {
				return entry;
			}
			return null;
		}

	}

	private final CMDataView view;

	public DataAccessLogic(final CMDataView view) {
		this.view = view;
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

	public CMClass findClassById(final Long classId) {
		final CMClass fetchedClass = view.findClassById(classId);
		if (fetchedClass == null) {
			throw NotFoundException.NotFoundExceptionType.CLASS_NOTFOUND.createException();
		}
		return fetchedClass;
	}

	public CMClass findClassByName(final String className) {
		return view.findClassByName(className);
	}

	public Iterable<? extends CMDomain> findAllDomains() {
		return this.view.findAllDomains();
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
	public CMCard fetchCard(final String className, final Long cardId) {
		final CMClass entryType = view.findClassByName(className);
		final CMQueryRow row;
		try {
			row = view.select(anyAttribute(entryType)) //
					.from(entryType) //
					.where(condition(attribute(entryType, "Id"), eq(cardId))) //
					.run() //
					.getOnlyRow();
		} catch (final NoSuchElementException ex) {
			throw NotFoundException.NotFoundExceptionType.CARD_NOTFOUND.createException();
		}
		return row.getCard(entryType);
	}

	public FetchCardListResponse fetchCards(final String className, final QueryOptions queryOptions) {
		final CMClass fetchedClass = view.findClassByName(className);
		if (fetchedClass == null) {
			final List<CMCard> emptyCardList = Lists.newArrayList();
			return new FetchCardListResponse(emptyCardList, 0);
		}

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

		final CMQueryResult result = querySpecsBuilder.run();
		final List<CMCard> filteredCards = Lists.newArrayList();
		for (final CMQueryRow row : result) {
			filteredCards.add(row.getCard(fetchedClass));
		}
		return new FetchCardListResponse(filteredCards, result.totalSize());
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
			final CMDomain domain = view.findDomainByName(joinElement.domain);
			final CMClass clazz = view.findClassByName(joinElement.destination);
			if (joinElement.left) {
				querySpecsBuilder.leftJoin(clazz, canonicalAlias(clazz), over(domain));
			} else {
				querySpecsBuilder.join(clazz, canonicalAlias(clazz), over(domain));
			}
		}
	}

	private void addSortingOptions(final QuerySpecsBuilder querySpecsBuilder, final QueryOptions options,
			final CMClass fetchedClass) {
		final SorterMapper sorterMapper = new JsonSorterMapper(fetchedClass, options.getSorters());
		for (final OrderByClause clause : sorterMapper.deserialize()) {
			querySpecsBuilder.orderBy(clause.getAttribute(), clause.getDirection());
		}
	}

	public Long createCard(final CardDTO cardToBeCreated) {
		final CMClass entryType = view.findClassByName(cardToBeCreated.getClassName());
		if (entryType == null) {
			throw NotFoundException.NotFoundExceptionType.CLASS_NOTFOUND.createException();
		}
		final Map<String, Object> attributes = cardToBeCreated.getAttributes();
		final CMCardDefinition mutableCard = view.createCardFor(entryType);
		for (final String attributeName : attributes.keySet()) {
			final Object attributeValue = attributes.get(attributeName);
			mutableCard.set(attributeName, attributeValue);
		}
		final CMCard savedCard = mutableCard.save();
		return savedCard.getId();
	}

	public void updateCard(final CardDTO cardToBeUpdated) {
		final CMClass entryType = view.findClassByName(cardToBeUpdated.getClassName());
		if (entryType == null) {
			throw NotFoundException.NotFoundExceptionType.CLASS_NOTFOUND.createException();
		}
		final Map<String, Object> attributes = cardToBeUpdated.getAttributes();
		final CMCard fetchedCard = fetchCard(cardToBeUpdated.getClassName(), Long.valueOf(cardToBeUpdated.getId()));
		final CMCardDefinition mutableCard = view.update(fetchedCard);
		for (final String attributeName : attributes.keySet()) {
			final Object attributeValue = attributes.get(attributeName);
			mutableCard.set(attributeName, attributeValue);
		}
		mutableCard.save();
	}

	public void updateFetchedCard(final CMCard fetchedCard, final Map<String, Object> attributes) {
		if (fetchedCard != null) {
			final CMCardDefinition mutableCard = view.update(fetchedCard);
			for (final String attributeName : attributes.keySet()) {
				final Object attributeValue = attributes.get(attributeName);
				mutableCard.set(attributeName, attributeValue);
			}
			mutableCard.save();
		}
	}

	public void deleteCard(final String className, final Integer cardId) {
		final CMClass entryType = view.findClassByName(className);
		if (entryType == null) {
			throw NotFoundException.NotFoundExceptionType.CLASS_NOTFOUND.createException();
		}
		final CMCard fetchedCard = fetchCard(className, Long.valueOf(cardId));
		view.delete(fetchedCard);
	}

	/**
	 * Retrieves all domains in which the class with id = classId is involved
	 * (both direct and inverse relation)
	 * 
	 * @param classId
	 *            the class involved in the relation
	 * @return a list of all domains defined for the class
	 */
	public List<CMDomain> findDomainsForClassWithId(final Long classId) {
		final CMClass fetchedClass = view.findClassById(classId);
		if (fetchedClass == null) {
			throw NotFoundException.NotFoundExceptionType.DOMAIN_NOTFOUND.createException();
		}

		return findDomainsForCMClass(fetchedClass);
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
		final CMClass fetchedClass = view.findClassByName(className);
		if (fetchedClass == null) {
			throw NotFoundException.NotFoundExceptionType.DOMAIN_NOTFOUND.createException();
		}

		return findDomainsForCMClass(fetchedClass);
	}

	private List<CMDomain> findDomainsForCMClass(final CMClass fetchedClass) {
		return Lists.newArrayList(view.findDomainsFor(fetchedClass));
	}

	/**
	 * Relations.... move the following code to another class
	 */

	public void createRelations(final RelationDTO relationDTO) {
		final CMDomain domain = view.findDomainByName(relationDTO.domainName);
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
			return fetchCard(className, cardId);
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
			final CMCard fetchedCard = fetchCard(className, cardId);
			childCards.add(fetchedCard);
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
		final CMDomain domain = view.findDomainByName(relationDTO.domainName);
		if (domain == null) {
			throw NotFoundException.NotFoundExceptionType.DOMAIN_NOTFOUND.createException();
		}
		final Entry<Long, String> srcCard = relationDTO.getUniqueEntryForSourceCard();
		final String srcClassName = srcCard.getValue();
		final Long srcCardId = srcCard.getKey();
		final CMCard fetchedSrcCard = fetchCard(srcClassName, srcCardId);
		final CMClass srcClass = view.findClassByName(srcClassName);

		final Entry<Long, String> dstCard = relationDTO.getUniqueEntryForDestinationCard();
		final String dstClassName = dstCard.getValue();
		final Long dstCardId = dstCard.getKey();
		final CMCard fetchedDstCard = fetchCard(dstClassName, dstCardId);
		final CMClass dstClass = view.findClassByName(dstClassName);
		CMQueryRow row;
		if (relationDTO.master.equals("_1")) {
			row = view.select(anyAttribute(srcClass), anyAttribute(domain))//
					.from(srcClass) //
					.join(dstClass, over(domain)) //
					.where(and(condition(attribute(srcClass, "Id"), eq(srcCardId)), //
							condition(attribute(domain, "Id"), eq(relationDTO.relationId)))) //
					.run().getOnlyRow();
		} else {
			row = view.select(anyAttribute(dstClass), anyAttribute(domain)) //
					.from(dstClass) //
					.join(srcClass, over(domain)) //
					.where(and(condition(attribute(dstClass, "Id"), eq(dstCardId)), //
							condition(attribute(domain, "Id"), eq(relationDTO.relationId)))) //
					.run().getOnlyRow();
		}
		final CMRelation relation = row.getRelation(domain).getRelation();
		final CMRelationDefinition mutableRelation = view.update(relation).setCard1(fetchedSrcCard)
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
		final CMDomain domain = view.findDomainByName(relationDTO.domainName);
		if (domain == null) {
			throw NotFoundException.NotFoundExceptionType.DOMAIN_NOTFOUND.createException();
		}
		final String srcClassName = relationDTO.getUniqueEntryForSourceCard().getValue();
		final String dstClassName = relationDTO.getUniqueEntryForDestinationCard().getValue();
		final CMClass srcClass = view.findClassByName(srcClassName);
		final CMClass dstClass = view.findClassByName(dstClassName);
		final Long srcCardId = relationDTO.getUniqueEntryForSourceCard().getKey();
		final CMQueryRow row = view.select(anyAttribute(srcClass), anyAttribute(domain))//
				.from(srcClass) //
				.join(dstClass, over(domain)) //
				.where(and(condition(attribute(srcClass, "Id"), eq(srcCardId)), //
						condition(attribute(domain, "Id"), eq(relationDTO.relationId)))) //
				.run().getOnlyRow();
		final CMRelation relation = row.getRelation(domain).getRelation();
		final CMRelationDefinition mutableRelation = view.update(relation);
		mutableRelation.delete();
	}
}
