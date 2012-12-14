package org.cmdbuild.model.domainTree;

import java.util.LinkedList;
import java.util.List;

public class DomainTreeNode {
	private String targetClassName, targetClassDescription, domainName, Type;
	private Long idParent, idGroup, id;
	private boolean direct, baseNode;
	private List<DomainTreeNode> childNodes;

	public DomainTreeNode() {
		childNodes = new LinkedList<DomainTreeNode>();
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getTargetClassName() {
		return targetClassName;
	}

	public void setTargetClassName(String targetClassName) {
		this.targetClassName = targetClassName;
	}

	public String getTargetClassDescription() {
		return targetClassDescription;
	}

	public void setTargetClassDescription(String targetClassDescription) {
		this.targetClassDescription = targetClassDescription;
	}

	public String getDomainName() {
		return domainName;
	}

	public void setDomainName(String domainName) {
		this.domainName = domainName;
	}

	public String getType() {
		return Type;
	}

	public void setType(String type) {
		Type = type;
	}

	public Long getIdParent() {
		return idParent;
	}

	public void setIdParent(Long idParent) {
		this.idParent = idParent;
	}

	public Long getIdGroup() {
		return idGroup;
	}

	public void setIdGroup(Long idGroup) {
		this.idGroup = idGroup;
	}

	public boolean isDirect() {
		return direct;
	}

	public void setDirect(boolean direct) {
		this.direct = direct;
	}

	public boolean isBaseNode() {
		return baseNode;
	}

	public void setBaseNode(boolean baseNode) {
		this.baseNode = baseNode;
	}

	public List<DomainTreeNode> getChildNodes() {
		return childNodes;
	}

	public void setChildNodes(List<DomainTreeNode> childNodes) {
		this.childNodes = childNodes;
	}

	public void addChildNode(DomainTreeNode childNode) {
		childNodes.add(childNode);
	}

}