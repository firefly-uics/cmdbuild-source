package org.cmdbuild.servlets.json.legacy;

import org.apache.commons.lang.StringUtils;
import org.cmdbuild.model.data.Card;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

public class GraphNode extends GraphItem {

	private int elements = 1;
	private Card card;

	public GraphNode(Card card) {
		this.elements = 1;
		this.card = card;
	}

	public Long getIdClass() {
		return card.getClassId();
	}

	private String getType() {
		if (this.isCluster())
			return "cluster";
		else
			return "node";
	}

	private boolean isCluster() {
		return (this.elements > 1);
	}

	private String getNodeId() {
		if (this.isCluster())
			return String.format("node_%d", card.getClassId());
		else
			return String.format("node_%d_%d", card.getClassId(), card.getId());
	}

	public Element toXMLElement() {
		Element node = DocumentHelper.createElement("node");
		node.addAttribute("id", getNodeId());
		node.add(serializeData("classId", String.valueOf(card.getClassId())));
		node.add(serializeData("classDesc", card.getClassDescription()));
		node.add(serializeData("type", getType()));
		if (this.isCluster()) {
			node.add(serializeData("elements", String.valueOf(elements)));
		} else {
			node.add(serializeData("objId", String.valueOf(card.getId())));
			final Object cardDescription = card.getAttribute("Description");
			node.add(serializeData("objDesc", cardDescription != null ? cardDescription.toString() : StringUtils.EMPTY));
		}
		return node;
	}
}
