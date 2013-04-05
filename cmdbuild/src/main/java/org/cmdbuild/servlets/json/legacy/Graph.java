package org.cmdbuild.servlets.json.legacy;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.cmdbuild.common.annotations.OldDao;
import org.cmdbuild.config.GraphProperties;
import org.cmdbuild.dao.entry.CMCard;
import org.cmdbuild.elements.interfaces.ICard;
import org.cmdbuild.elements.interfaces.DomainFactory;
import org.cmdbuild.elements.interfaces.IRelation;
import org.cmdbuild.elements.interfaces.RelationFactory;
import org.cmdbuild.elements.interfaces.RelationQuery;
import org.cmdbuild.elements.interfaces.ITableFactory;
import org.cmdbuild.elements.utils.CountedValue;
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

/******************************************************************
 * This code is an attempt to make the relation graph work with * the new
 * persistence layer. Filter calls are not handled and it * MUST be refactored
 * ASAP... even more than the rest *
 ******************************************************************/
@OldDao
@SuppressWarnings("unchecked")
public class Graph extends JSONBase {

	private final DataAccessLogic dataAccessLogic;

	public Graph() {
		dataAccessLogic = TemporaryObjectsBeforeSpringDI.getDataAccessLogic();
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
				for (RelationInfo relationInfo : domainInfo) {
					GraphEdge edge = new GraphEdge(card, relationInfo, domainInfo);
					graphEdges.add(edge);
					CMCard targetCard = relationInfo.getTargetCard();
					CardStorableConverter cardConverter = new CardStorableConverter(targetCard.getType()
							.getIdentifier().getLocalName());
					graphNodes.add(new GraphNode(cardConverter.convert(targetCard)));
					GraphRelation graphRelation = new GraphRelation(card, relationInfo, domainInfo);
					if (!graphRelations.contains(graphRelation)) {
						graphRelations.add(graphRelation);
					}
				}
			}
		}

		return XMLSerializer.serializeGraph(graphNodes, graphEdges, graphRelations);
	}

	@JSONExported(contentType = "text/xml")
	@Unauthorized
	public Document declusterize(ITableFactory tf, DomainFactory df, RelationFactory rf,
			@Parameter("data") String xmlDataString) throws DocumentException {
		Set<GraphNode> graphNodes = Sets.newHashSet();
		Set<GraphEdge> graphEdges = Sets.newHashSet();
		Set<GraphRelation> graphRelations = Sets.newHashSet();

		Document xmlData = DocumentHelper.parseText(xmlDataString);
		List<Element> xmlElements = DocumentHelper.createXPath("/data/relations/item").selectNodes(xmlData);
		Element cluster = xmlElements.get(0);
		Long parentClassId = Long.parseLong(cluster.attributeValue("parentClassId"));
		Long parentObjId = Long.parseLong(cluster.attributeValue("parentObjId"));
		Long domainId = Long.parseLong(cluster.attributeValue("domainId"));
		Long childClassId = Long.parseLong(cluster.attributeValue("childClassId"));

		// OLD
		// ICard card = tf.get(parentClassId).cards().get(parentObjId);
		// graphNodes.add(new GraphNode(card));
		// RelationQuery relationQuery =
		// rf.list().straightened().card(card).domain(df.get(domainId));
		// for (CountedValue<IRelation> countedRelation :
		// relationQuery.getCountedIterable()) {
		// IRelation relation = countedRelation.getValue();
		// GraphNode node = GraphNode.relationTarget(countedRelation);
		// if (node.getIdClass() != childClassId)
		// continue;
		// graphNodes.add(node);
		// GraphEdge edge = new GraphEdge(relation);
		// graphEdges.add(edge);
		// }
		// END OLD

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
