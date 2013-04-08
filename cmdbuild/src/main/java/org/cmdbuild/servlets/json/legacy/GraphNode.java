package org.cmdbuild.servlets.json.legacy;

import org.apache.commons.lang.StringUtils;
import org.cmdbuild.config.GraphProperties;
import org.cmdbuild.logic.commands.GetRelationList.DomainInfo;
import org.cmdbuild.model.data.Card;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

import com.google.common.collect.Iterables;

public class GraphNode extends GraphItem {

	private int elements = 1;
	private final Card card;

	public GraphNode(final Card card) {
		this.elements = 1;
		this.card = card;
	}

	public GraphNode(final Card card, final DomainInfo domainInfo) {
		this.elements = Iterables.size(domainInfo);
		this.card = card;
	}

	public Long getIdClass() {
		return card.getClassId();
	}

	private String getType() {
		if (this.isCluster()) {
			return "cluster";
		} else {
			return "node";
		}
	}

	private boolean isCluster() {
		return (this.elements >= GraphProperties.getInstance().getClusteringThreshold());
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