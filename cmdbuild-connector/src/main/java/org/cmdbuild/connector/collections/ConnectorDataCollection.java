package org.cmdbuild.connector.collections;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.commons.lang.Validate;
import org.cmdbuild.connector.data.ConnectorCard;
import org.cmdbuild.connector.data.ConnectorClass;
import org.cmdbuild.connector.data.ConnectorDomain;
import org.cmdbuild.connector.data.ConnectorRelation;

public class ConnectorDataCollection {

	private final Map<ConnectorClass, SortedSet<ConnectorCard>> cardsMap;
	private final Map<ConnectorDomain, SortedSet<ConnectorRelation>> relationsMap;

	public ConnectorDataCollection() {
		cardsMap = new HashMap<ConnectorClass, SortedSet<ConnectorCard>>();
		relationsMap = new HashMap<ConnectorDomain, SortedSet<ConnectorRelation>>();
	}

	public Set<ConnectorClass> getConnectorClasses() {
		return cardsMap.keySet();
	}

	public Set<ConnectorDomain> getConnectorDomains() {
		return relationsMap.keySet();
	}

	public void addConnectorCard(final ConnectorCard card) {
		Validate.notNull(card, "null card");
		final ConnectorClass connectorClass = card.getConnectorClass();
		final SortedSet<ConnectorCard> cards = getConnectorCards(connectorClass);
		cards.add(card);
	}

	public void addConnectorRelation(final ConnectorRelation relation) {
		Validate.notNull(relation, "null relation");
		final ConnectorDomain connectorDomain = relation.getConnectorDomain();
		final Set<ConnectorRelation> relations = getConnectorRelations(connectorDomain);
		relations.add(relation);
	}

	public SortedSet<ConnectorCard> getConnectorCards(final ConnectorClass connectorClass) {
		Validate.notNull(connectorClass, "null class");
		if (!cardsMap.containsKey(connectorClass)) {
			cardsMap.put(connectorClass, new TreeSet<ConnectorCard>());
		}
		return cardsMap.get(connectorClass);
	}

	public SortedSet<ConnectorRelation> getConnectorRelations(final ConnectorDomain connectorDomain) {
		Validate.notNull(connectorDomain, "null domain");
		if (!relationsMap.containsKey(connectorDomain)) {
			relationsMap.put(connectorDomain, new TreeSet<ConnectorRelation>());
		}
		return relationsMap.get(connectorDomain);
	}

}
