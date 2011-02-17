package org.cmdbuild.servlets.json.legacy;

import org.cmdbuild.elements.interfaces.ICard;
import org.cmdbuild.elements.interfaces.IRelation;
import org.cmdbuild.elements.proxy.LazyCard;
import org.cmdbuild.elements.utils.CountedValue;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

public class GraphNode extends GraphItem {

	private int elements = 1;
	private ICard card;

	private GraphNode() {
	}

	public GraphNode(ICard card) {
		this.elements = 1;
		this.card = card;
	}

	public static GraphNode relationTarget(CountedValue<IRelation> countedRelation) {
		IRelation relation = countedRelation.getValue();
		ICard card = relation.getCard2();
		if (card != null) {
			return new GraphNode(card);
		} else {
			GraphNode gn = new GraphNode();
			gn.elements = countedRelation.getCount();
			int classId = relation.getDirectedDomain().getDestTable().getId();
			gn.card = new LazyCard(classId, 0);
			return gn;
		}
	}

	public int getIdClass() {
		return this.card.getIdClass();
	}

	private String getClassDescription() {
		return this.card.getSchema().getDescription();
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
			return String.format("node_%d", this.card.getIdClass());
		else
			return String.format("node_%d_%d", this.card.getIdClass(), this.card.getId());
	}

	public Element toXMLElement() {
		Element node = DocumentHelper.createElement("node");
		node.addAttribute("id", this.getNodeId());
		node.add(serializeData("classId", String.valueOf(this.card.getIdClass())));
		node.add(serializeData("classDesc", this.getClassDescription()));
		node.add(serializeData("type", this.getType()));
		if (this.isCluster()) {
			node.add(serializeData("elements", String.valueOf(this.elements)));
		} else {
			node.add(serializeData("objId", String.valueOf(this.card.getId())));
			node.add(serializeData("objDesc", this.card.getDescription()));
		}
		return node;
	}
}
