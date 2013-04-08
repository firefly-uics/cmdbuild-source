package org.cmdbuild.servlets.json.legacy;

import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.cmdbuild.model.data.Card;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

public class XMLSerializer {

	public static Document serializeCard(Card card) {
		Document document = DocumentHelper.createDocument();
		Element xmlCard = document.addElement("data").addElement("card");
		for (Entry<String, Object> entry : card.getAttributes().entrySet()) {
			Element item = xmlCard.addElement("item");
			String value = entry.getValue() != null ? entry.getValue().toString() : StringUtils.EMPTY;
			String valueForGraph = value == null ? "" : value.replaceAll("&", "&amp;").replaceAll("\\<.*?\\>", "");
			item.addAttribute("realName", entry.getKey());
			item.addAttribute("name", entry.getKey());
			item.addAttribute("value", valueForGraph);
		}
		return document;
	}

	public static Document serializeGraph(Set<GraphNode> nodes, Set<GraphEdge> edges, Set<GraphRelation> relationGroups) {
		Document document = DocumentHelper.createDocument();
		Element xmlData = document.addElement("data");
		xmlData.add(serializeRelations(relationGroups));
		xmlData.add(serializeGraphMLNode(nodes, edges));
		return document;
	}

	private static Element serializeRelations(Set<GraphRelation> relationGroups) {
		Element relationList = DocumentHelper.createElement("relations");
		for (GraphRelation relationGroup : relationGroups) {
			relationList.add(relationGroup.toXMLElement());
		}
		return relationList;
	}

	private static Element serializeGraphMLNode(Set<GraphNode> nodes, Set<GraphEdge> edges) {
		Element graphML = DocumentHelper.createElement("graphml");
		graphML.add(serializeGraphNode(nodes, edges));
		return graphML;
	}

	private static Element serializeGraphNode(Set<GraphNode> nodes, Set<GraphEdge> edges) {
		Element graph = DocumentHelper.createElement("graph");
		graph.addAttribute("edgedefault", "undirected");
		graph.add(genGraphKey("objId", true, "int"));
		graph.add(genGraphKey("classId", true, "int"));
		graph.add(genGraphKey("classDesc", true, "string"));
		graph.add(genGraphKey("objDesc", true, "string"));
		graph.add(genGraphKey("type", true, "string"));
		graph.add(genGraphKey("elements", true, "int"));
		graph.add(genGraphKey("domaindescription", false, "string"));
		for (GraphNode n : nodes) {
			graph.add(n.toXMLElement());
		}
		for (GraphEdge e : edges) {
			graph.add(e.toXMLElement());
		}
		return graph;
	}

	private static Element genGraphKey(String id, boolean node, String type) {
		Element keyNode = DocumentHelper.createElement("key");
		keyNode.addAttribute("id", id);
		if (node)
			keyNode.addAttribute("for", "node");
		else
			keyNode.addAttribute("for", "edge");
		keyNode.addAttribute("attr.name", id);
		keyNode.addAttribute("attr.type", type);
		return keyNode;
	}
}