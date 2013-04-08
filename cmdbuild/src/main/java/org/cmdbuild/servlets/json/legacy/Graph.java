package org.cmdbuild.servlets.json.legacy;

import java.util.List;
import java.util.Set;

import org.cmdbuild.config.GraphProperties;
import org.cmdbuild.dao.query.clause.QueryDomain.Source;
import org.cmdbuild.logic.LogicDTO.DomainWithSource;
import org.cmdbuild.logic.TemporaryObjectsBeforeSpringDI;
import org.cmdbuild.logic.commands.AbstractGetRelation.RelationInfo;
import org.cmdbuild.logic.commands.GetRelationList.DomainInfo;
import org.cmdbuild.logic.commands.GetRelationList.GetRelationListResponse;
import org.cmdbuild.logic.data.access.CardStorableConverter;
import org.cmdbuild.logic.data.access.DataAccessLogic;
import org.cmdbuild.model.data.Card;
import org.cmdbuild.servlets.json.JSONBase;
import org.cmdbuild.servlets.utils.Parameter;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.XPath;

import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;

@SuppressWarnings("unchecked")
public class Graph extends JSONBase {

	private final DataAccessLogic dataAccessLogic;
	private final int clusteringThreshold;

	public Graph() {
		dataAccessLogic = TemporaryObjectsBeforeSpringDI.getDataAccessLogic();
		clusteringThreshold = GraphProperties.getInstance().getClusteringThreshold();
	}

	@JSONExported(contentType = "text/xml")
	@Unauthorized
	public Document graphML(@Parameter("data") String xmlDataString) throws DocumentException {
		Document xmlData = DocumentHelper.parseText(xmlDataString);
		Set<Card> nodes = getNodes(xmlData);
		Set<Card> excludes = getExcludes(xmlData);

		Set<GraphNode> graphNodes = Sets.newHashSet();
		Set<GraphEdge> graphEdges = Sets.newHashSet();
		Set<GraphRelation> graphRelations = Sets.newHashSet();

		for (Card card : nodes) {
			if (excludes.contains(card)) {
				continue;
			}
			graphNodes.add(new GraphNode(card));
			GetRelationListResponse response = dataAccessLogic.getRelationList(card, null);
			for (DomainInfo domainInfo : response) {
				boolean serializeOnlyOneNode = Iterables.size(domainInfo) >= clusteringThreshold;
				int numberOfNodesToSerialize = serializeOnlyOneNode ? 1 : Iterables.size(domainInfo);
				int serializedNodes = 0;

				for (RelationInfo relationInfo : domainInfo) {
					GraphRelation graphRelation = new GraphRelation(card, domainInfo);
					if (!graphRelations.contains(graphRelation)) {
						graphRelations.add(graphRelation);
					}
					if (serializedNodes < numberOfNodesToSerialize) {
						GraphEdge edge = new GraphEdge(card, relationInfo, domainInfo);
						graphEdges.add(edge);
						CardStorableConverter cardConverter = new CardStorableConverter(relationInfo.getTargetCard()
								.getType().getIdentifier().getLocalName());
						graphNodes.add(new GraphNode(cardConverter.convert(relationInfo.getTargetCard()), //
								domainInfo));
						serializedNodes++;
					}
				}
			}
		}

		return XMLSerializer.serializeGraph(graphNodes, graphEdges, graphRelations);
	}

	@JSONExported(contentType = "text/xml")
	@Unauthorized
	public Document declusterize(@Parameter("data") String xmlDataString) throws DocumentException {
		Set<GraphNode> graphNodes = Sets.newHashSet();
		Set<GraphEdge> graphEdges = Sets.newHashSet();
		Set<GraphRelation> graphRelations = Sets.newHashSet();

		Document xmlData = DocumentHelper.parseText(xmlDataString);
		List<Element> xmlElements = DocumentHelper.createXPath("/data/relations/item").selectNodes(xmlData);
		Element cluster = xmlElements.get(0);
		Long srcClassId = Long.parseLong(cluster.attributeValue("parentClassId"));
		Long srcCardId = Long.parseLong(cluster.attributeValue("parentObjId"));
		Long domainId = Long.parseLong(cluster.attributeValue("domainId"));
		Long targetClassId = Long.parseLong(cluster.attributeValue("childClassId"));

		final Card srcCard = dataAccessLogic.fetchCard(srcClassId, srcCardId);
		graphNodes.add(new GraphNode(srcCard));
		Long class1Id = dataAccessLogic.findDomain(domainId).getClass1().getId();
		final DomainWithSource dom;
		if (class1Id.equals(srcClassId)) {
			dom = DomainWithSource.create(domainId, Source._1.toString());
		} else {
			dom = DomainWithSource.create(domainId, Source._2.toString());
		}
		GetRelationListResponse response = dataAccessLogic.getRelationList(srcCard, dom);
		for (DomainInfo domainInfo : response) {
			for (RelationInfo relationInfo : domainInfo) {
				CardStorableConverter cardConverter = new CardStorableConverter(relationInfo.getTargetCard().getType()
						.getIdentifier().getLocalName());
				GraphNode node = new GraphNode(cardConverter.convert(relationInfo.getTargetCard()));
				if (!node.getIdClass().equals(targetClassId)) {
					continue;
				}
				graphNodes.add(node);
				GraphEdge edge = new GraphEdge(srcCard, relationInfo, domainInfo);
				edge.setDeclusterize(true);
				graphEdges.add(edge);

			}
		}
		return XMLSerializer.serializeGraph(graphNodes, graphEdges, graphRelations);
	}

	@JSONExported(contentType = "text/xml")
	@Unauthorized
	public Document card(@Parameter("data") String xmlDataString) throws DocumentException {
		Document xmlData = DocumentHelper.parseText(xmlDataString);
		XPath xpathSelector = DocumentHelper.createXPath("/data/nodes/item");
		List<?> results = xpathSelector.selectNodes(xmlData);
		Element element = (Element) results.get(0);
		Long classId = Long.parseLong(element.attributeValue("classId"));
		Long objId = Long.parseLong(element.attributeValue("objId"));
		return XMLSerializer.serializeCard(dataAccessLogic.fetchCard(classId, objId));
	}

	private Set<Card> getNodes(Document xmlData) {
		Set<Card> nodes = Sets.newHashSet();
		List<Element> xmlElements = DocumentHelper.createXPath("/data/nodes/item").selectNodes(xmlData);
		for (Element element : xmlElements) {
			Long classId = Long.parseLong(element.attributeValue("classId"));
			Long cardId = Long.parseLong(element.attributeValue("objId"));
			Card nodeCard = dataAccessLogic.fetchCard(classId, cardId);
			nodes.add(nodeCard);
		}
		return nodes;
	}

	private Set<Card> getExcludes(Document xmlData) {
		Set<Card> excludes = Sets.newHashSet();
		List<Element> xmlElements = DocumentHelper.createXPath("/data/excludes/item").selectNodes(xmlData);
		for (Element element : xmlElements) {
			Long classId = Long.parseLong(element.attributeValue("classId"));
			Long cardId = Long.parseLong(element.attributeValue("objId"));
			Card excludedCard = dataAccessLogic.fetchCard(classId, cardId);
			excludes.add(excludedCard);
		}
		return excludes;
	}
}
