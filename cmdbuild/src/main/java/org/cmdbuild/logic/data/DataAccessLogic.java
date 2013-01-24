package org.cmdbuild.logic.data;

import static com.google.common.collect.Iterables.isEmpty;
import static org.cmdbuild.dao.query.clause.AnyAttribute.anyAttribute;
import static org.cmdbuild.dao.query.clause.alias.Alias.canonicalAlias;
import static org.cmdbuild.dao.query.clause.join.Over.over;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.cmdbuild.common.annotations.Legacy;
import org.cmdbuild.dao.entry.CMCard;
import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.dao.entrytype.CMDomain;
import org.cmdbuild.dao.legacywrappers.CardWrapper;
import org.cmdbuild.dao.query.CMQueryResult;
import org.cmdbuild.dao.query.CMQueryRow;
import org.cmdbuild.dao.query.QuerySpecsBuilder;
import org.cmdbuild.dao.query.clause.OrderByClause;
import org.cmdbuild.dao.query.clause.where.WhereClause;
import org.cmdbuild.dao.view.CMDataView;
import org.cmdbuild.elements.interfaces.ICard;
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
import org.cmdbuild.services.auth.UserContext;
import org.cmdbuild.services.auth.UserOperations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import com.google.common.collect.Lists;

/**
 * Business Logic Layer for Data Access
 */
@Component
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

	private final CMDataView view;

	@Autowired
	public DataAccessLogic(@Qualifier("user") final CMDataView view) {
		this.view = view;
	}

	public Map<Object, List<RelationInfo>> relationsBySource(final String sourceTypeName, final DomainWithSource dom) {
		return new GetRelationList(view).list(sourceTypeName, dom);
	}

	public GetRelationListResponse getRelationList(final Card srcCard, final DomainWithSource dom) {
		return new GetRelationList(view).exec(srcCard, dom);
	}

	public GetRelationHistoryResponse getRelationHistory(final Card srcCard) {
		return new GetRelationHistory(view).exec(srcCard);
	}

	public CMClass findClassById(final Long classId) {
		return view.findClassById(classId);
	}

	public Iterable<? extends CMDomain> findAllDomains() {
		return this.view.findAllDomains();
	}

	@Legacy("IMPORTANT! FIX THE NEW DAO AND FIX BECAUSE IT USES THE SYSTEM USER!")
	public CMCard getCard(final String className, final Object cardId) {
		try {
			final int id = Integer.parseInt(cardId.toString()); // very
			// expensive but
			// almost never
			// called
			final ICard card = UserOperations.from(UserContext.systemContext()).tables().get(className).cards().get(id);
			return new CardWrapper(card);
		} catch (final Exception e) {
			return null;
		}
		/*
		 * The new DAO layer does not query subclasses! **************** final
		 * CMClass cardType = view.findClassByName(className); final
		 * CMQueryResult result = view.select( attribute(cardType,
		 * Constants.DESCRIPTION_ATTRIBUTE)) .from(cardType)
		 * .where(attribute(cardType, Constants.ID_ATTRIBUTE), Operator.EQUALS,
		 * cardId) .run(); if (result.isEmpty()) { return null; } else { return
		 * result.iterator().next().getCard(cardType); }
		 * ***************************************************************
		 */
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
			return Lists.newArrayList();
		}
		return Lists.newArrayList(view.findDomainsFor(fetchedClass));
	}

	public FetchCardListResponse fetchCards(final String className, final QueryOptions queryOptions) {
		final CMClass fetchedClass = view.findClassByName(className);
		if (fetchedClass == null) {
			final List<CMCard> emptyCards = Lists.newArrayList();
			return new FetchCardListResponse(emptyCards, 0);
		}

		final FilterMapper filterMapper = new JsonFilterMapper(fetchedClass, queryOptions.getFilter(), view);
		final WhereClause whereClause = filterMapper.whereClauses();
		final Iterable<FilterMapper.JoinElement> joinElements = filterMapper.joinElements();

		final QuerySpecsBuilder queryBuilder = view.select(anyAttribute(fetchedClass)) //
				.from(fetchedClass) //
				.where(whereClause) //
				.limit(queryOptions.getLimit()) //
				.offset(queryOptions.getOffset());

		if (!isEmpty(joinElements)) {
			queryBuilder.distinct();
		}
		for (final FilterMapper.JoinElement joinElement : joinElements) {
			final CMDomain domain = view.findDomainByName(joinElement.domain);
			final CMClass clazz = view.findClassByName(joinElement.destination);
			if (joinElement.left) {
				queryBuilder.leftJoin(clazz, canonicalAlias(clazz), over(domain));
			} else {
				queryBuilder.join(clazz, canonicalAlias(clazz), over(domain));
			}
		}

		final SorterMapper sorterMapper = new JsonSorterMapper(fetchedClass, queryOptions.getSorters());
		for (final OrderByClause clause : sorterMapper.deserialize()) {
			queryBuilder.orderBy(clause.getAttribute(), clause.getDirection());
		}
		final CMQueryResult result = queryBuilder.run();
		final List<CMCard> filteredCards = Lists.newArrayList();
		for (final CMQueryRow row : result) {
			filteredCards.add(row.getCard(fetchedClass));
		}
		return new FetchCardListResponse(filteredCards, result.totalSize());
	}

}
