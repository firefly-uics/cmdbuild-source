package org.cmdbuild.connector.ws;

import java.util.Iterator;
import java.util.Map;

import org.apache.commons.lang.Validate;
import org.apache.log4j.Logger;
import org.cmdbuild.connector.collections.ConnectorDataCollection;
import org.cmdbuild.connector.collections.IdDataCollection;
import org.cmdbuild.connector.data.ConnectorCard;
import org.cmdbuild.connector.data.ConnectorCardAttribute;
import org.cmdbuild.connector.data.ConnectorClass;
import org.cmdbuild.connector.data.ConnectorClassAttribute;
import org.cmdbuild.connector.data.ConnectorDomain;
import org.cmdbuild.connector.data.ConnectorRelation;
import org.cmdbuild.connector.data.Key;
import org.cmdbuild.connector.logger.Log;
import org.cmdbuild.services.soap.Attribute;
import org.cmdbuild.services.soap.Card;
import org.cmdbuild.services.soap.Relation;

public class CMDBuildSync {

	protected static final Logger logger = Log.SYNC;

	private final CMDBuildWebServiceClient client;
	private final IdDataCollection idsDataCollection;
	private final ConnectorDataCollection externalDataCollection;

	public CMDBuildSync(final String url, final String username, final String password,
			final IdDataCollection idsDataCollection, final ConnectorDataCollection externalDataCollection) {
		Validate.notNull(idsDataCollection, "null data collection");
		Validate.notNull(externalDataCollection, "null external data collection");
		this.client = new CMDBuildWebServiceClient(url, username, password);
		this.idsDataCollection = idsDataCollection;
		this.externalDataCollection = externalDataCollection;
	}

	public void createCard(final ConnectorCard connectorCard) {
		try {
			logger.debug("creating card");
			final Card card = buildCard(connectorCard, true);
			final int id = client.getProxy().createCard(card);
			idsDataCollection.addCardId(connectorCard.getConnectorClass(), id, connectorCard.getKey());
		} catch (final Exception e) {
			logger.error(e.getMessage());
		}
	}

	public void modifyCard(final ConnectorCard connectorCard) {
		try {
			logger.debug("modifying card");
			final Card card = buildCard(connectorCard, false);
			client.getProxy().updateCard(card);
		} catch (final Exception e) {
			logger.error(e.getMessage());
		}
	}

	public void deleteCard(final ConnectorCard connectorCard) {
		try {
			logger.debug("deleting card");
			client.getProxy().deleteCard(connectorCard.getConnectorClass().getName(), getId(connectorCard));
		} catch (final Exception e) {
			logger.error(e.getMessage());
		}
	}

	public void createRelation(final ConnectorRelation connectorRelation) {
		try {
			logger.debug("creating relation");
			final Relation relation = buildRelation(connectorRelation);
			client.getProxy().createRelation(relation);
		} catch (final Exception e) {
			logger.error(e.getMessage());
		}
	}

	public void deleteRelation(final ConnectorRelation connectorRelation) {
		try {
			logger.debug("deleting relation");
			final Relation relation = buildRelation(connectorRelation);
			client.getProxy().deleteRelation(relation);
		} catch (final Exception e) {
			logger.error(e.getMessage());
		}
	}

	private Card buildCard(final ConnectorCard connectorCard, final boolean isNew) {
		final Card card = buildNewCard(connectorCard);
		if (!isNew) {
			final int id = getId(connectorCard);
			card.setId(id);
		}
		return card;
	}

	private Card buildNewCard(final ConnectorCard connectorCard) {
		final Card card = new Card();
		final ConnectorClass connectorClass = connectorCard.getConnectorClass();
		card.setClassName(connectorClass.getName());
		for (final ConnectorCardAttribute connectorCardAttribute : connectorCard.getAttributes()) {
			final Attribute attribute = buildAttribute(connectorClass, connectorCardAttribute);
			card.getAttributeList().add(attribute);
		}
		return card;
	}

	private Attribute buildAttribute(final ConnectorClass connectorClass,
			final ConnectorCardAttribute connectorCardAttribute) {
		final String name = connectorCardAttribute.getName();
		String value = "";
		if (connectorCardAttribute.isReference()) {
			final ConnectorClassAttribute classAttribute = connectorClass.getAttribute(name);
			final ConnectorClass referencedClass = classAttribute.getReferencedClass();
			final Key referencedKey = connectorCardAttribute.getReference();
			for (final ConnectorCard connectorCard : externalDataCollection.getConnectorCards(referencedClass)) {
				if (connectorCard.getKey().equals(referencedKey)) {
					final int id = getId(connectorCard);
					value = Integer.toString(id);
					break;
				}
			}
		} else {
			value = connectorCardAttribute.getValue();
		}

		final Attribute attribute = new Attribute();
		attribute.setName(name);
		attribute.setValue(value);

		return attribute;
	}

	private int getId(final ConnectorCard connectorCard) {
		final Map<Integer, Key> ids = idsDataCollection.getClassKeys(connectorCard.getConnectorClass());
		if (ids.containsValue(connectorCard.getKey())) {
			for (final Integer id : ids.keySet()) {
				final Key key = ids.get(id);
				if (key.equals(connectorCard.getKey())) {
					return id;
				}
			}
		} else {
			logger.warn("id not found!");
		}
		return 0;
	}

	private int getId(final Key cardKey, final ConnectorClass connectorClass) {
		final Map<Integer, Key> ids = idsDataCollection.getClassKeys(connectorClass);
		if (ids.containsValue(cardKey)) {
			for (final Integer id : ids.keySet()) {
				final Key key = ids.get(id);
				if (key.equals(cardKey)) {
					return id;
				}
			}
		} else {
			logger.warn("id not found!");
		}
		return 0;
	}

	private Relation buildRelation(final ConnectorRelation connectorRelation) {
		final Relation relation = new Relation();
		final ConnectorDomain connectorDomain = connectorRelation.getConnectorDomain();
		relation.setDomainName(connectorDomain.getName());
		final Iterator<ConnectorClass> classItr = connectorDomain.getConnectorClasses().iterator();
		final ConnectorClass connectorClass1 = classItr.next();
		final ConnectorClass connectorClass2 = classItr.next();
		relation.setClass1Name(connectorClass1.getName());
		relation.setClass2Name(connectorClass2.getName());
		final Iterator<Key> keyItr = connectorRelation.getKeys().iterator();
		final int id1 = getId(keyItr.next(), connectorClass1);
		final int id2 = getId(keyItr.next(), connectorClass2);
		relation.setCard1Id(id1);
		relation.setCard2Id(id2);
		return relation;
	}

}
