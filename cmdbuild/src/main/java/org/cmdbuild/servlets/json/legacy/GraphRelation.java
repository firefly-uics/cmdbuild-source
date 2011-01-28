package org.cmdbuild.servlets.json.legacy;

import org.cmdbuild.config.GraphProperties;
import org.cmdbuild.elements.DirectedDomain;
import org.cmdbuild.elements.interfaces.ICard;
import org.cmdbuild.elements.interfaces.IRelation;
import org.cmdbuild.elements.utils.CountedValue;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

public class GraphRelation {

	ICard sourceCard;
	DirectedDomain ddomain;
	int count;

	public GraphRelation(CountedValue<IRelation> countedRelation) {
		this.count = countedRelation.getCount();
		IRelation relation = countedRelation.getValue();
		this.sourceCard = relation.getCard1();
		this.ddomain = countedRelation.getValue().getDirectedDomain();
	}

	private String getDescription() {
		return String.format("%s - %s", this.sourceCard.getDescription(), this.ddomain.getDescription());
	}

	private boolean isClusterized() {
		int clusteringThreshold = GraphProperties.getInstance().getClusteringThreshold();
		return (clusteringThreshold <= count);
	}

	Element toXMLElement() {
		Element relationItem = DocumentHelper.createElement("item");
		relationItem.addAttribute("parentClassId", String.valueOf(sourceCard.getIdClass()));
		relationItem.addAttribute("parentObjId", String.valueOf(sourceCard.getId()));
		relationItem.addAttribute("domainId", String.valueOf(ddomain.getDomain().getId()));
		relationItem.addAttribute("childClassId", String.valueOf(ddomain.getDestTable().getId()));
		relationItem.addAttribute("elements", String.valueOf(count));
		relationItem.addAttribute("clusterize", String.valueOf(this.isClusterized()));
		relationItem.addAttribute("description", this.getDescription());
		return relationItem;
	}

	public boolean equals(Object o) {
		if (o instanceof GraphRelation) {
			GraphRelation g = ((GraphRelation) o);
			return (this.ddomain.equals(g.ddomain)
					&& this.sourceCard.equals(g.sourceCard));
		}
		return false;
	}


	public int hashCode() {
		return this.ddomain.hashCode()+this.sourceCard.hashCode();
	}

}
