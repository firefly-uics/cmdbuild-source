package org.cmdbuild.logic.data;

import static org.cmdbuild.dao.query.clause.AnyAttribute.anyAttribute;

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
import org.cmdbuild.dao.query.clause.where.EmptyWhereClause;
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
import org.cmdbuild.logic.mappers.FilterMapper;
import org.cmdbuild.logic.mappers.SorterMapper;
import org.cmdbuild.logic.mappers.json.JSONFilterMapper;
import org.cmdbuild.logic.mappers.json.JSONSorterMapper;
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

	public List<CMCard> fetchCards(final String className, final QueryOptions queryOptions) {
		final CMClass fetchedClass = view.findClassByName(className);
		if (fetchedClass == null) {
			return Lists.newArrayList();
		}
		final FilterMapper filterMapper = new JSONFilterMapper(fetchedClass, queryOptions.getFilter());
		final WhereClause whereClause = filterMapper.deserialize();

		final QuerySpecsBuilder queryBuilder = view.select(anyAttribute(fetchedClass)) //
				.from(fetchedClass);
		if (!(whereClause instanceof EmptyWhereClause)) {
			queryBuilder.where(whereClause);
		}
		queryBuilder.limit(queryOptions.getLimit()) //
				.offset(queryOptions.getOffset());
		final SorterMapper sorterMapper = new JSONSorterMapper(fetchedClass, queryOptions.getSorters());
		for (final OrderByClause clause : sorterMapper.deserialize()) {
			queryBuilder.orderBy(clause.getAttribute(), clause.getDirection());
		}
		final CMQueryResult result = queryBuilder.run();
		final List<CMCard> filteredCards = Lists.newArrayList();
		for (final CMQueryRow row : result) {
			filteredCards.add(row.getCard(fetchedClass));
		}
		return filteredCards;
	}

}
