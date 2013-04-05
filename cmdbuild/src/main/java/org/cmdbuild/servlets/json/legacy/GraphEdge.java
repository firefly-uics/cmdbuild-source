package org.cmdbuild.servlets.json.legacy;

import org.cmdbuild.dao.entry.CMCard;
import org.cmdbuild.logic.commands.AbstractGetRelation.RelationInfo;
import org.cmdbuild.logic.commands.GetRelationList.DomainInfo;
import org.cmdbuild.model.data.Card;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

public class GraphEdge extends GraphItem {

	private final Card srcCard;
	private final RelationInfo relation;
	private final DomainInfo domainInfo;

	public GraphEdge(final Card srcCard, final RelationInfo relation, final DomainInfo domainInfo) {
		this.relation = relation;
		this.domainInfo = domainInfo;
		this.srcCard = srcCard;
	}

	private String getEdgeSourceId() {
		return String.format("node_%d_%d", domainInfo.getQueryDomain().getSourceClass().getId(), srcCard.getId());
	}

	private String getEdgeTargetId() {
		CMCard targetCard = (CMCard) relation.getTargetCard();
		return String.format("node_%d_%d", domainInfo.getQueryDomain().getTargetClass().getId(), targetCard.getId());
	}

	private String getDomainDescription() {
		return domainInfo.getDescription();
	}

	public Element toXMLElement() {
		Element edge = DocumentHelper.createElement("edge");
		edge.addAttribute("source", getEdgeSourceId());
		edge.addAttribute("target", getEdgeTargetId());
		edge.add(serializeData("domaindescription", getDomainDescription()));
		return edge;
	}
}
