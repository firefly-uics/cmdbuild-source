package org.cmdbuild.api.fluent.ws;

import static java.util.Collections.unmodifiableList;
import static java.util.Collections.unmodifiableMap;
import static org.cmdbuild.api.fluent.ws.ReportHelper.DEFAULT_TYPE;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.activation.DataHandler;

import org.cmdbuild.api.fluent.Card;
import org.cmdbuild.api.fluent.CardDescriptor;
import org.cmdbuild.api.fluent.DownloadedReport;
import org.cmdbuild.api.fluent.ExistingCard;
import org.cmdbuild.api.fluent.FluentApi;
import org.cmdbuild.api.fluent.FluentApiExecutor;
import org.cmdbuild.api.fluent.Function;
import org.cmdbuild.api.fluent.Relation;
import org.cmdbuild.api.fluent.RelationsQuery;
import org.cmdbuild.api.fluent.Report;
import org.cmdbuild.common.Constants;
import org.cmdbuild.services.soap.Attribute;
import org.cmdbuild.services.soap.CardList;
import org.cmdbuild.services.soap.CqlQuery;
import org.cmdbuild.services.soap.Filter;
import org.cmdbuild.services.soap.FilterOperator;
import org.cmdbuild.services.soap.Order;
import org.cmdbuild.services.soap.Private;
import org.cmdbuild.services.soap.Query;
import org.cmdbuild.services.soap.ReportParams;

@SuppressWarnings("restriction")
public class WsFluentApiExecutor implements FluentApiExecutor {

	private static final FluentApiExecutor NULL_NEVER_USED_EXECUTOR = null;

	private static final String OPERATOR_EQUALS = "EQUALS";
	private static final String OPERATOR_AND = "AND";

	private static final List<Attribute> ALL_ATTRIBUTES = null;
	private static final Query NO_QUERY = null;
	private static final List<Order> NO_ORDERING = null;
	private static final int NO_LIMIT = 0;
	private static final int OFFSET_BEGINNING = 0;
	private static final String NO_FULLTEXT = null;
	private static final CqlQuery NO_CQL = null;

	private final Private proxy;

	public WsFluentApiExecutor(final Private proxy) {
		this.proxy = proxy;
	}

	public CardDescriptor create(final Card card) {
		final org.cmdbuild.services.soap.Card soapCard = soapCardFor(card);
		final int id = proxy.createCard(soapCard);
		return new CardDescriptor(card.getClassName(), id);
	}

	public void update(final Card card) {
		final org.cmdbuild.services.soap.Card soapCard = soapCardFor(card);
		soapCard.setId(card.getId());
		proxy.updateCard(soapCard);
	}

	public void delete(final Card card) {
		proxy.deleteCard(card.getClassName(), card.getId());
	}

	public Card fetch(final Card card) {
		final org.cmdbuild.services.soap.Card soapCard = proxy.getCard( //
				card.getClassName(), //
				card.getId(), //
				hasAttributes(card) ? attributesFor(card) : ALL_ATTRIBUTES);
		return cardFor(soapCard);
	}

	public List<Card> fetchCards(final Card card) {
		final CardList cardList = proxy.getCardList( //
				card.getClassName(), //
				ALL_ATTRIBUTES, //
				hasAttributes(card) ? queriedAttributesFor(card) : NO_QUERY, //
				NO_ORDERING, //
				NO_LIMIT, //
				OFFSET_BEGINNING, //
				NO_FULLTEXT, //
				NO_CQL);
		return cardsFor(cardList);
	}

	private Query queriedAttributesFor(final Card card) {
		final FilterOperator filterOperator = new FilterOperator();
		filterOperator.setOperator(OPERATOR_AND);
		filterOperator.getSubquery().addAll(queriesFor(card));
		return queryFor(filterOperator);
	}

	private Query queryFor(final FilterOperator filterOperator) {
		final Query query = new Query();
		query.setFilterOperator(filterOperator);
		return query;
	}

	private List<Query> queriesFor(final Card card) {
		final List<Query> queries = new ArrayList<Query>();
		for (final Entry<String, Object> attributeEntry : card.getAttributes().entrySet()) {
			final Query attributeQuery = queryFor(equalsFilter(attributeEntry.getKey(), attributeEntry.getValue()));
			queries.add(attributeQuery);
		}
		return queries;
	}

	private Query queryFor(final Filter filter) {
		final Query query = new Query();
		query.setFilter(filter);
		return query;
	}

	private Filter equalsFilter(final String name, final Object value) {
		final Filter filter = new Filter();
		filter.setName(name);
		filter.setOperator(OPERATOR_EQUALS);
		filter.getValue().add(WsHelper.convertToWsType(value));
		return filter;
	}

	private List<Card> cardsFor(final CardList cardList) {
		final List<Card> cards = new ArrayList<Card>();
		for (final org.cmdbuild.services.soap.Card soapCard : cardList.getCards()) {
			cards.add(cardFor(soapCard));
		}
		return unmodifiableList(cards);
	}

	private Card cardFor(final org.cmdbuild.services.soap.Card soapCard) {
		final ExistingCard card = existingCardFrom(soapCard);
		for (final Attribute attribute : soapCard.getAttributeList()) {
			final String attributeName = attribute.getName();
			if (Constants.CLASS_ID_ATTRIBUTE.equals(attributeName)) {
				final int classId = Integer.parseInt(attribute.getValue());
				card.withClassId(classId);
			} else {
				card.with(attributeName, attribute.getValue());
			}
		}
		return card;
	}

	private ExistingCard existingCardFrom(final org.cmdbuild.services.soap.Card soapCard) {
		return new FluentApi(NULL_NEVER_USED_EXECUTOR) //
				.existingCard(soapCard.getClassName(), soapCard.getId());
	}

	public void create(final Relation relation) {
		proxy.createRelation(soapRelationFor(relation));
	}

	public void delete(final Relation relation) {
		proxy.deleteRelation(soapRelationFor(relation));
	}

	private org.cmdbuild.services.soap.Relation soapRelationFor(final Relation relation) {
		final org.cmdbuild.services.soap.Relation soapRelation = new org.cmdbuild.services.soap.Relation();
		soapRelation.setDomainName(relation.getDomainName());
		soapRelation.setClass1Name(relation.getClassName1());
		soapRelation.setCard1Id(relation.getCardId1());
		soapRelation.setClass2Name(relation.getClassName2());
		soapRelation.setCard2Id(relation.getCardId2());
		return soapRelation;
	}

	public List<Relation> fetch(final RelationsQuery query) {
		final List<org.cmdbuild.services.soap.Relation> soapRelations = proxy.getRelationList( //
				query.getDomainName(), //
				query.getClassName(), //
				query.getCardId());
		final List<Relation> relations = new ArrayList<Relation>();
		for (final org.cmdbuild.services.soap.Relation soapRelation : soapRelations) {
			relations.add(relationFor(soapRelation));
		}
		return unmodifiableList(relations);
	}

	private Relation relationFor(final org.cmdbuild.services.soap.Relation soapRelation) {
		final Relation relation = new Relation(soapRelation.getDomainName());
		relation.setCard1(soapRelation.getClass1Name(), soapRelation.getCard1Id());
		relation.setCard2(soapRelation.getClass2Name(), soapRelation.getCard2Id());
		return relation;
	}

	public Map<String, String> execute(final Function function) {
		final List<Attribute> outputs = proxy.callFunction( //
				function.getFunctionName(), //
				attributesFor(function));
		return unmodifiableMap(attributesAsMap(outputs));
	}

	public DownloadedReport download(final Report report) {
		final ReportHelper helper = new ReportHelper(proxy);
		final org.cmdbuild.services.soap.Report soapReport = helper.getReport(DEFAULT_TYPE, report.getTitle());
		final List<ReportParams> reportParams = helper.compileParams(report.getParameters());
		final DataHandler dataHandler = helper.getDataHandler(soapReport, report.getFormat(), reportParams);
		final File file = helper.temporaryFile();
		helper.saveToFile(dataHandler, file);
		return new DownloadedReport(file);
	}

	private Map<String, String> attributesAsMap(final List<Attribute> attributes) {
		final Map<String, String> attributesMap = new HashMap<String, String>();
		for (final Attribute attribute : attributes) {
			attributesMap.put(attribute.getName(), attribute.getValue());
		}
		return attributesMap;
	}

	/*
	 * Utils
	 */

	public static org.cmdbuild.services.soap.Card soapCardFor(final Card card) {
		final org.cmdbuild.services.soap.Card soapCard = new org.cmdbuild.services.soap.Card();
		soapCard.setClassName(card.getClassName());
		if (card.getId() != null) {
			soapCard.setId(card.getId());
		}
		soapCard.getAttributeList().addAll(attributesFor(card));
		return soapCard;
	}

	private boolean hasAttributes(final Card card) {
		return !card.getAttributes().isEmpty();
	}

	public static List<Attribute> attributesFor(final Card card) {
		return attributesFor(card.getAttributes());
	}

	public static List<Attribute> attributesFor(final Function function) {
		return attributesFor(function.getInputs());
	}

	private static List<Attribute> attributesFor(final Map<String, Object> map) {
		final List<Attribute> attributeList = new ArrayList<Attribute>();
		for (final Entry<String, Object> entry : map.entrySet()) {
			final Attribute attribute = attributeFor(entry);
			attributeList.add(attribute);
		}
		return attributeList;
	}

	private static Attribute attributeFor(final Entry<String, Object> entry) {
		return attribute(entry.getKey(), entry.getValue());
	}

	public static Attribute attribute(final String name, final Object value) {
		final Attribute attribute = new Attribute();
		attribute.setName(name);
		attribute.setValue(WsHelper.convertToWsType(value));
		return attribute;
	}

}
