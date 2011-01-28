package org.cmdbuild.connector.ws;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang.Validate;
import org.cmdbuild.connector.collections.IdDataCollection;
import org.cmdbuild.connector.data.ConnectorClass;
import org.cmdbuild.connector.data.ConnectorClassAttribute;
import org.cmdbuild.connector.data.ConnectorDomain;
import org.cmdbuild.connector.data.Key;
import org.cmdbuild.connector.data.MutableConnectorCard;
import org.cmdbuild.connector.data.MutableConnectorRelation;
import org.cmdbuild.connector.parser.AbstractParser;
import org.cmdbuild.connector.parser.ParserException;
import org.cmdbuild.services.soap.Attribute;
import org.cmdbuild.services.soap.Card;
import org.cmdbuild.services.soap.CardList;
import org.cmdbuild.services.soap.CqlQuery;
import org.cmdbuild.services.soap.Relation;

public class CMDBuildParser extends AbstractParser {

	private String url = "";
	private String username = "";
	private String password = "";
	private CMDBuildWebServiceClient client;
	private final IdDataCollection idDataCollection;

	public CMDBuildParser(final String url, final String username, final String password,
			final IdDataCollection idDataCollection) {
		Validate.notNull(url, "null url");
		Validate.notEmpty(url, "empty url");
		Validate.notNull(username, "null username");
		Validate.notNull(password, "null password");
		Validate.notNull(idDataCollection, "null data collection");
		this.url = url;
		this.username = username;
		this.password = password;
		this.idDataCollection = idDataCollection;
	}

	@Override
	public void parseSchema() throws ParserException {
	}

	@Override
	public void parse() throws ParserException {
		client = new CMDBuildWebServiceClient(url, username, password);
		buildCards();
		buildRelations();
	}

	private void buildCards() {
		for (final ConnectorClass connectorClass : schema.getConnectorClasses()) {
			logger.debug("Getting data for class: " + connectorClass.getName());

			CqlQuery cqlQuery = null;
			final String query = connectorClass.getCqlQueryString();
			if (query != null) {
				cqlQuery = new CqlQuery();
				cqlQuery.setCqlQuery(query);
				logger.debug("Filtering card using: " + query);
			}
			final CardList list = client.getProxy().getCardList(connectorClass.getName(),
					buildCMDBuildClassAttributes(connectorClass), null, null, null, null, null, cqlQuery);
			final List<Card> cards = list.getCards();
			for (final Card card : cards) {
				buildCard(connectorClass, card);
			}
		}
	}

	private void buildCard(final ConnectorClass connectorClass, final Card card) {
		final MutableConnectorCard connectorCard = new MutableConnectorCard(connectorClass);
		try {
			connectorCard.setId(card.getId());
			for (final Attribute attribute : card.getAttributeList()) {
				final String attributeName = attribute.getName();
				final ConnectorClassAttribute classAttribute = connectorClass.getAttribute(attributeName);
				if (classAttribute == null) {
					continue;
				} else if (classAttribute.isReference()) {
					final String code = attribute.getCode();
					final int id = Integer.parseInt(code);
					final ConnectorClass referencedClass = classAttribute.getReferencedClass();
					final Key referencedKey = idDataCollection.getCardKey(referencedClass, id);
					Validate.notNull(referencedKey, "key not found for attribute '" + attributeName + "'");
					connectorCard.setAttribute(attributeName, referencedKey);
				} else {
					connectorCard.setAttribute(attributeName, attribute.getValue());
				}
			}
			// a simple validation for the card
			connectorCard.getKey();
			fireCardFound(connectorCard);
		} catch (final NullPointerException e) {
			logger.warn(e.getMessage());
		} catch (final IllegalArgumentException e) {
			logger.warn(e.getMessage());
		}
	}

	private List<Attribute> buildCMDBuildClassAttributes(final ConnectorClass connectorClass) {
		final List<Attribute> attributes = new LinkedList<Attribute>();
		for (final ConnectorClassAttribute a : connectorClass.getAttributes()) {
			final Attribute attribute = new Attribute();
			attribute.setName(a.getName());
			attributes.add(attribute);
		}
		return attributes;
	}

	private void buildRelations() {
		for (final ConnectorDomain domain : schema.getConnectorDomains()) {
			logger.debug("Getting data for domain: " + domain.getName());
			final List<Relation> relations = client.getProxy().getRelationList(domain.getName(), null, 0);
			for (final Relation relation : relations) {
				buildRelation(domain, relation);
			}
		}
	}

	private void buildRelation(final ConnectorDomain domain, final Relation relation) {
		final List<Integer> ids = new LinkedList<Integer>();
		ids.add(relation.getCard1Id());
		ids.add(relation.getCard2Id());
		final Iterator<Integer> idIterator = ids.iterator();

		final List<ConnectorClass> domainClasses = domain.getConnectorClasses();
		final Iterator<ConnectorClass> classesIterator = domainClasses.iterator();

		final List<Key> keys = new LinkedList<Key>();
		while (classesIterator.hasNext() && idIterator.hasNext()) {
			final ConnectorClass connectorClass = classesIterator.next();
			final int id = idIterator.next();
			final Key key = idDataCollection.getCardKey(connectorClass, id);
			if (key != null) {
				keys.add(key);
			} else {
				return;
			}
		}

		final MutableConnectorRelation connectorRelation = new MutableConnectorRelation(domain, keys);
		fireRelationFound(connectorRelation);
	}
}
