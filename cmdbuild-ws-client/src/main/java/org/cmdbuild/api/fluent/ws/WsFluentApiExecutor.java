package org.cmdbuild.api.fluent.ws;

import static java.util.Collections.unmodifiableList;
import static java.util.Collections.unmodifiableMap;
import static org.apache.commons.lang.StringUtils.EMPTY;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.cmdbuild.api.fluent.CallFunction;
import org.cmdbuild.api.fluent.Card;
import org.cmdbuild.api.fluent.CardDescriptor;
import org.cmdbuild.api.fluent.ExistingCard;
import org.cmdbuild.api.fluent.ExistingRelation;
import org.cmdbuild.api.fluent.FluentApiExecutor;
import org.cmdbuild.api.fluent.NewCard;
import org.cmdbuild.api.fluent.NewRelation;
import org.cmdbuild.api.fluent.QueryClass;
import org.cmdbuild.api.fluent.Relation;
import org.cmdbuild.services.soap.Attribute;
import org.cmdbuild.services.soap.CardList;
import org.cmdbuild.services.soap.CqlQuery;
import org.cmdbuild.services.soap.Filter;
import org.cmdbuild.services.soap.FilterOperator;
import org.cmdbuild.services.soap.Order;
import org.cmdbuild.services.soap.Private;
import org.cmdbuild.services.soap.Query;

public class WsFluentApiExecutor implements FluentApiExecutor {

	private static final FluentApiExecutor NULL_NEVER_USED_EXECUTOR = null;

	private static final String OPERATOR_EQUALS = "EQUALS";
	private static final String OPERATOR_AND = "AND";

	private static final List<Attribute> ALL_ATTRIBUTES = null;
	private static final List<Order> NO_ORDERING = null;
	private static final int NO_LIMIT = 0;
	private static final int OFFSET_BEGINNING = 0;
	private static final String NO_FULLTEXT = null;
	private static final CqlQuery NO_CQL = null;

	private final Private proxy;

	public WsFluentApiExecutor(final Private proxy) {
		this.proxy = proxy;
	}

	public CardDescriptor create(final NewCard newCard) {
		final org.cmdbuild.services.soap.Card card = soapCardFor(newCard);
		final int id = proxy.createCard(card);
		return new CardDescriptor(newCard.getClassName(), id);
	}

	public void update(final ExistingCard existingCard) {
		final org.cmdbuild.services.soap.Card card = soapCardFor(existingCard);
		card.setId(existingCard.getId());
		proxy.updateCard(card);
	}

	public void delete(final ExistingCard existingCard) {
		proxy.deleteCard(existingCard.getClassName(), existingCard.getId());
	}

	public Card fetch(final ExistingCard existingCard) {
		final org.cmdbuild.services.soap.Card soapCard = proxy.getCard( //
				existingCard.getClassName(), //
				existingCard.getId(), //
				attributesFor(existingCard));
		return cardFrom(soapCard);
	}

	private Card cardFrom(final org.cmdbuild.services.soap.Card soapCard) {
		final ExistingCard card = new ExistingCard( //
				NULL_NEVER_USED_EXECUTOR, //
				soapCard.getClassName(), //
				soapCard.getId());
		for (final Attribute attribute : soapCard.getAttributeList()) {
			card.withAttribute(attribute.getName(), attribute.getValue());
		}
		return card;
	}

	public void create(final NewRelation newRelation) {
		proxy.createRelation(soapRelationFor(newRelation));
	}

	public void delete(final ExistingRelation existingRelation) {
		proxy.deleteRelation(soapRelationFor(existingRelation));
	}

	private org.cmdbuild.services.soap.Relation soapRelationFor(final Relation relation) {
		final org.cmdbuild.services.soap.Relation soapRelation = new org.cmdbuild.services.soap.Relation();
		soapRelation.setDomainName(relation.getDomainName());
		soapRelation.setClass1Name(relation.getClassName1());
		soapRelation.setCard1Id(relation.getClassId1());
		soapRelation.setClass2Name(relation.getClassName2());
		soapRelation.setCard2Id(relation.getClassId2());
		return soapRelation;
	}

	public List<CardDescriptor> fetch(final QueryClass queryClass) {
		final CardList cardList = proxy.getCardList( //
				queryClass.getClassName(), //
				ALL_ATTRIBUTES, //
				queriedAttributesFor(queryClass), //
				NO_ORDERING, //
				NO_LIMIT, //
				OFFSET_BEGINNING, //
				NO_FULLTEXT, //
				NO_CQL);
		return cardDescriptorsFor(cardList);
	}

	private Query queriedAttributesFor(final QueryClass queryClass) {
		final FilterOperator filterOperator = new FilterOperator();
		filterOperator.setOperator(OPERATOR_AND);
		filterOperator.getSubquery().addAll(queriesFor(queryClass));
		return queryFor(filterOperator);
	}

	private Query queryFor(final FilterOperator filterOperator) {
		final Query query = new Query();
		query.setFilterOperator(filterOperator);
		return query;
	}

	private List<Query> queriesFor(final QueryClass queryClass) {
		final List<Query> queries = new ArrayList<Query>();
		for (final Entry<String, Object> attributeEntry : queryClass.getAttributes().entrySet()) {
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

	public Filter equalsFilter(final String name, final Object value) {
		final Filter filter = new Filter();
		filter.setName(name);
		filter.setOperator(OPERATOR_EQUALS);
		filter.getValue().add(convert(value));
		return filter;
	}

	private List<CardDescriptor> cardDescriptorsFor(final CardList cardList) {
		final List<CardDescriptor> descriptors = new ArrayList<CardDescriptor>();
		for (final org.cmdbuild.services.soap.Card wsCard : cardList.getCards()) {
			final CardDescriptor cardDescriptor = new CardDescriptor(wsCard.getClassName(), wsCard.getId());
			descriptors.add(cardDescriptor);
		}
		return unmodifiableList(descriptors);
	}

	public Map<String, String> execute(final CallFunction callFunction) {
		final List<Attribute> outputs = proxy.callFunction( //
				callFunction.getFunctionName(), //
				attributesFor(callFunction));
		return unmodifiableMap(attributesAsMap(outputs));
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
		soapCard.getAttributeList().addAll(attributesFor(card));
		return soapCard;
	}

	public static List<Attribute> attributesFor(final Card card) {
		return attributesFor(card.getAttributes());
	}

	public static List<Attribute> attributesFor(final CallFunction callFunction) {
		return attributesFor(callFunction.getInputs());
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
		attribute.setValue(convert(value));
		return attribute;
	}

	private static String convert(final Object value) {
		final String stringValue;
		if (value == null) {
			stringValue = EMPTY;
		} else if (value instanceof Number) {
			stringValue = Number.class.cast(value).toString();
		} else if (value instanceof Date) {
			final SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
			final Date date = Date.class.cast(value);
			stringValue = formatter.format(date);
		} else {
			stringValue = value.toString();
		}
		return stringValue;
	}

}
