package org.cmdbuild.api.utils;

import static org.apache.commons.lang.StringUtils.EMPTY;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.cmdbuild.api.fluent.Card;
import org.cmdbuild.api.fluent.ExistingCard;
import org.cmdbuild.api.fluent.FluentApiExecutor;
import org.cmdbuild.api.fluent.Relation;
import org.cmdbuild.services.soap.Attribute;
import org.cmdbuild.services.soap.Filter;
import org.cmdbuild.services.soap.FilterOperator;
import org.cmdbuild.services.soap.Query;

public class SoapUtils {

	private static final FluentApiExecutor NULL_NEVER_USED_EXECUTOR = null;

	public static final String OPERATOR_EQUALS = "EQUALS";

	private SoapUtils() {
		// prevents instantiation
	}

	public static List<Attribute> attributesFor(final Map<String, String> attributeMap) {
		final List<Attribute> attributeList = new ArrayList<Attribute>();
		for (final Entry<String, String> attributeEntry : attributeMap.entrySet()) {
			final Attribute attribute = attributeFor(attributeEntry);
			attributeList.add(attribute);
		}
		return attributeList;
	}

	private static Attribute attributeFor(final Entry<String, String> entry) {
		return attribute(entry.getKey(), entry.getValue());
	}

	public static Attribute attribute(final String name, final String value) {
		final Attribute attribute = new Attribute();
		attribute.setName(name);
		attribute.setValue(safeValue(value));
		return attribute;
	}

	private static String safeValue(final String value) {
		return (value == null) ? EMPTY : value;
	}

	public static org.cmdbuild.services.soap.Card soapCardFor(final Card card) {
		final org.cmdbuild.services.soap.Card soapCard = new org.cmdbuild.services.soap.Card();
		soapCard.setClassName(card.getClassName());
		soapCard.getAttributeList().addAll(attributesFor(card.getAttributes()));
		return soapCard;
	}

	public static Card cardFrom(final org.cmdbuild.services.soap.Card soapCard) {
		final ExistingCard card = new ExistingCard( //
				soapCard.getClassName(), //
				soapCard.getId(), //
				NULL_NEVER_USED_EXECUTOR);
		for (final Attribute attribute : soapCard.getAttributeList()) {
			card.withAttribute(attribute.getName(), attribute.getValue());
		}
		return card;
	}

	public static org.cmdbuild.services.soap.Relation soapRelationFor(final Relation relation) {
		final org.cmdbuild.services.soap.Relation soapRelation = new org.cmdbuild.services.soap.Relation();
		soapRelation.setDomainName(relation.getDomainName());
		soapRelation.setClass1Name(relation.getClassName1());
		soapRelation.setCard1Id(relation.getClassId1());
		soapRelation.setClass2Name(relation.getClassName2());
		soapRelation.setCard2Id(relation.getClassId2());
		return soapRelation;
	}

	public static Query queryFor(final Filter filter) {
		final Query query = new Query();
		query.setFilter(filter);
		return query;
	}

	public static Query queryFor(final FilterOperator filterOperator) {
		final Query query = new Query();
		query.setFilterOperator(filterOperator);
		return query;
	}

	public static Filter equalsFilterFor(final String name, final String value) {
		final Filter filter = new Filter();
		filter.setName(name);
		filter.setOperator(OPERATOR_EQUALS);
		filter.getValue().add(value);
		return filter;
	}

}
