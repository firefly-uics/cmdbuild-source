package org.cmdbuild.servlets.json.legacy;

import org.cmdbuild.elements.interfaces.ICard;
import org.cmdbuild.elements.interfaces.IRelation;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

public class GraphEdge extends GraphItem {

	IRelation relation;

	public GraphEdge(IRelation relation) {
			this.relation = relation;
	}

	private String getEdgeSourceId() {
		ICard card = this.relation.getCard1();
		return String.format("node_%d_%d", card.getIdClass(), card.getId());
	}

	private String getEdgeTargetId() {
		ICard card = this.relation.getCard2();
		if (card != null)
			return String.format("node_%d_%d", card.getIdClass(), card.getId());
		else
			return String.format("node_%d", this.relation.getDirectedDomain().getDestTable().getId());
	}

	private String getDomainDescription() {
		return this.relation.getDirectedDomain().getDescription();
	}

	public Element toXMLElement() {
		Element edge = DocumentHelper.createElement("edge");
		edge.addAttribute("source", this.getEdgeSourceId());
		edge.addAttribute("target", this.getEdgeTargetId());
		edge.add(serializeData("domaindescription", this.getDomainDescription()));
		return edge;
	}
}
