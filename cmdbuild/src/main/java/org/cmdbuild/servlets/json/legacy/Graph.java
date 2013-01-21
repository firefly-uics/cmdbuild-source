package org.cmdbuild.servlets.json.legacy;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.cmdbuild.common.annotations.OldDao;
import org.cmdbuild.config.GraphProperties;
import org.cmdbuild.elements.interfaces.ICard;
import org.cmdbuild.elements.interfaces.DomainFactory;
import org.cmdbuild.elements.interfaces.IRelation;
import org.cmdbuild.elements.interfaces.RelationFactory;
import org.cmdbuild.elements.interfaces.RelationQuery;
import org.cmdbuild.elements.interfaces.ITableFactory;
import org.cmdbuild.elements.utils.CountedValue;
import org.cmdbuild.servlets.json.JSONBase;
import org.cmdbuild.servlets.utils.Parameter;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.XPath;

/******************************************************************
 * This code is an attempt to make the relation graph work with   *
 * the new persistence layer. Filter calls are not handled and it *
 * MUST be refactored ASAP... even more than the rest             *
 ******************************************************************/
@OldDao
@SuppressWarnings("unchecked")
public class Graph extends JSONBase {

	@JSONExported(contentType="text/xml")
	@Unauthorized
	public Document graphML(
			ITableFactory tf,
			RelationFactory rf,
			@Parameter("data") String xmlDataString ) throws DocumentException {
		Document xmlData = DocumentHelper.parseText(xmlDataString);
		Set<ICard> nodes = getNodes(xmlData, tf);
		Set<ICard> excludes = getExcludes(xmlData, tf);

		Set<GraphNode> gnodes = new HashSet<GraphNode>();
		Set<GraphEdge> gedges = new HashSet<GraphEdge>();
		Set<GraphRelation> grelations = new HashSet<GraphRelation>();

		RelationQuery relationQuery = rf.list().straightened()
			.domainLimit(GraphProperties.getInstance().getClusteringThreshold());
		for (ICard card : nodes) {
			relationQuery.card(card);
			gnodes.add(new GraphNode(card));
		}

		for(CountedValue<IRelation> countedRelation : relationQuery.getCountedIterable()) {
			IRelation relation = countedRelation.getValue();
			GraphNode node = GraphNode.relationTarget(countedRelation);
			if (excludes.contains(node))
				continue;
			gnodes.add(node);
			GraphEdge edge = new GraphEdge(relation);
			gedges.add(edge);
			GraphRelation gr = new GraphRelation(countedRelation);
			if (!grelations.contains(gr))
				grelations.add(gr);
		}

		return XMLSerializer.serializeGraph(gnodes, gedges, grelations);
	}

	@JSONExported(contentType="text/xml")
	@Unauthorized
	public Document declusterize(
			ITableFactory tf,
			DomainFactory df,
			RelationFactory rf,
			@Parameter("data") String xmlDataString) throws DocumentException {
		Set<GraphNode> gnodes = new HashSet<GraphNode>();
		Set<GraphEdge> gedges = new HashSet<GraphEdge>();
		Set<GraphRelation> grelations = new HashSet<GraphRelation>();

		Document xmlData = DocumentHelper.parseText(xmlDataString);
		List<Element> xmlElements = DocumentHelper.createXPath("/data/relations/item").selectNodes(xmlData);
		Element cluster = xmlElements.get(0);
		int parentClassId = Integer.parseInt(cluster.attributeValue("parentClassId"));
		int parentObjId = Integer.parseInt(cluster.attributeValue("parentObjId"));
		int domainId = Integer.parseInt(cluster.attributeValue("domainId"));
		int childClassId = Integer.parseInt(cluster.attributeValue("childClassId"));

		ICard card = tf.get(parentClassId).cards().get(parentObjId);
		gnodes.add(new GraphNode(card));
		RelationQuery relationQuery = rf.list().straightened()
			.card(card).domain(df.get(domainId));
		for (CountedValue<IRelation> countedRelation : relationQuery.getCountedIterable()) {
			IRelation relation = countedRelation.getValue();
			GraphNode node = GraphNode.relationTarget(countedRelation);
			if (node.getIdClass() != childClassId)
				continue;
			gnodes.add(node);
			GraphEdge edge = new GraphEdge(relation);
			gedges.add(edge);
		}

		return XMLSerializer.serializeGraph(gnodes, gedges, grelations);
	}

	@JSONExported(contentType="text/xml")
	@Unauthorized
	public Document card(
			@Parameter("data") String xmlDataString,
			ITableFactory tf) throws DocumentException {
		Document xmlData = DocumentHelper.parseText(xmlDataString);
		XPath xpathSelector = DocumentHelper.createXPath("/data/nodes/item");
	    List results = xpathSelector.selectNodes(xmlData);
		Element element = (Element) results.get(0);
		int classId = Integer.parseInt(element.attributeValue("classId"));
		int objId = Integer.parseInt(element.attributeValue("objId"));
		return XMLSerializer.serializeCard(tf.get(classId).cards().get(objId));
	}

	private Set<ICard> getNodes(Document xmlData, ITableFactory tf) {
		Set<ICard> nodes = new HashSet<ICard>();
		List<Element> xmlElements = DocumentHelper.createXPath("/data/nodes/item").selectNodes(xmlData);
		for (Element element : xmlElements) {
			int classId = Integer.parseInt(element.attributeValue("classId"));
			int cardId = Integer.parseInt(element.attributeValue("objId"));
			ICard card = tf.get(classId).cards().get(cardId);
			nodes.add(card);
		}
		return nodes;
	}

	private Set<ICard> getExcludes(Document xmlData, ITableFactory tf) {
		Set<ICard> excludes = new HashSet<ICard>();
		List<Element> xmlElements = DocumentHelper.createXPath("/data/excludes/item").selectNodes(xmlData);
		for (Element element : xmlElements) {
			int classId = Integer.parseInt(element.attributeValue("classId"));
			int cardId = Integer.parseInt(element.attributeValue("objId"));
			ICard card = tf.get(classId).cards().get(cardId);
			excludes.add(card);
		}
		return excludes;
	}
}
