package org.cmdbuild.api.fluent.ws;

import static java.util.Collections.unmodifiableList;
import static org.cmdbuild.api.utils.SoapUtils.attributesFor;
import static org.cmdbuild.api.utils.SoapUtils.cardFrom;
import static org.cmdbuild.api.utils.SoapUtils.equalsFilterFor;
import static org.cmdbuild.api.utils.SoapUtils.soapCardFor;
import static org.cmdbuild.api.utils.SoapUtils.soapRelationFor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.cmdbuild.api.fluent.Card;
import org.cmdbuild.api.fluent.CardDescriptor;
import org.cmdbuild.api.fluent.ExistingCard;
import org.cmdbuild.api.fluent.ExistingRelation;
import org.cmdbuild.api.fluent.FluentApiExecutor;
import org.cmdbuild.api.fluent.NewCard;
import org.cmdbuild.api.fluent.NewRelation;
import org.cmdbuild.api.fluent.QueryClass;
import org.cmdbuild.api.utils.SoapUtils;
import org.cmdbuild.services.soap.Attribute;
import org.cmdbuild.services.soap.CardList;
import org.cmdbuild.services.soap.CqlQuery;
import org.cmdbuild.services.soap.FilterOperator;
import org.cmdbuild.services.soap.Order;
import org.cmdbuild.services.soap.Private;
import org.cmdbuild.services.soap.Query;
import org.cmdbuild.services.soap.Relation;

public class WsFluentApiExecutor implements FluentApiExecutor {

	private static final List<Attribute> ALL_ATTRIBUTES = null;
	private static final List<Order> NO_ORDERING = null;
	private static final int NO_LIMIT = 0;
	private static final int OFFSET_BEGINNING = 0;
	private static final String NO_FULLTEXT = null;
	private static final CqlQuery NO_CQL = null;

	private static final String OPERATOR_AND = "AND";

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
				attributesFor(existingCard.getAttributes()));
		return cardFrom(soapCard);
	}

	public void create(final NewRelation newRelation) {
		final Relation relation = soapRelationFor(newRelation);
		proxy.createRelation(relation);
	}

	public void delete(final ExistingRelation existingRelation) {
		final Relation relation = soapRelationFor(existingRelation);
		proxy.deleteRelation(relation);
	}

	public List<CardDescriptor> fetch(final QueryClass classQuery) {
		final CardList cardList = proxy.getCardList( //
				classQuery.getClassName(), //
				ALL_ATTRIBUTES, //
				queryFor(classQuery.getAttributes()), //
				NO_ORDERING, //
				NO_LIMIT, //
				OFFSET_BEGINNING, //
				NO_FULLTEXT, //
				NO_CQL);
		return cardDescriptorsFor(cardList);
	}

	private Query queryFor(final Map<String, String> attributes) {
		final FilterOperator filterOperator = new FilterOperator();
		filterOperator.setOperator(OPERATOR_AND);
		filterOperator.getSubquery().addAll(queriesFor(attributes));
		return SoapUtils.queryFor(filterOperator);
	}

	private List<Query> queriesFor(final Map<String, String> attributes) {
		final List<Query> queries = new ArrayList<Query>();
		for (final Entry<String, String> attributeEntry : attributes.entrySet()) {
			final Query attributeQuery = queryFor(attributeEntry.getKey(), attributeEntry.getValue());
			queries.add(attributeQuery);
		}
		return queries;
	}

	private Query queryFor(final String name, final String value) {
		return SoapUtils.queryFor(equalsFilterFor(name, value));
	}

	private List<CardDescriptor> cardDescriptorsFor(final CardList cardList) {
		final List<CardDescriptor> descriptors = new ArrayList<CardDescriptor>();
		for (final org.cmdbuild.services.soap.Card wsCard : cardList.getCards()) {
			final CardDescriptor cardDescriptor = new CardDescriptor(wsCard.getClassName(), wsCard.getId());
			descriptors.add(cardDescriptor);
		}
		return unmodifiableList(descriptors);
	}

}
