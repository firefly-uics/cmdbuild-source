package org.cmdbuild.model.domainTree;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DomainTreeCardNode {
	private String className, text;
	private boolean leaf, checked, expanded, baseNode;
	private Long classId, cardId;
	private DomainTreeCardNode parent;
	private Map<Object, DomainTreeCardNode> children;

	public class DomainTreeCardNodeComparator implements Comparator<DomainTreeCardNode> {

		@Override
		public int compare(DomainTreeCardNode o1, DomainTreeCardNode o2) {
			return o1.getText().compareTo(o2.getText());
		}

	}

	public DomainTreeCardNode() {
		children = new HashMap<Object, DomainTreeCardNode>();
		classId = new Long(0);
		cardId = new Long(0);
	}

	public String getClassName() {
		return className;
	}

	public void setClassName(String className) {
		this.className = className;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public void setLeaf(boolean leaf) {
		this.leaf = leaf;
	}

	public boolean isLeaf() {
		return leaf;
	}

	public boolean isChecked() {
		return checked;
	}

	public void setChecked(boolean checked) {
		setChecked(checked, false);
	}

	public void setChecked(boolean checked, boolean deeply) {
		setChecked(checked, deeply, false);
	}

	public void setChecked(boolean checked, boolean deeply, boolean alsoAncestor) {
		this.checked = checked;

		if (deeply) {
			for (DomainTreeCardNode child:getChildren()) {
				child.setChecked(checked, true, false);
			}
		}

		if (alsoAncestor) {
			DomainTreeCardNode parent = parent();
			while(parent != null) {
				parent.setChecked(true, false, false);
				parent = parent.parent();
			}
		}
	}

	public boolean isExpanded() {
		return expanded;
	}

	public void setExpanded(boolean expanded) {
		this.expanded = expanded;
	}

	public boolean isBaseNode() {
		return baseNode;
	}

	public void setBaseNode(boolean baseNode) {
		this.baseNode = baseNode;
	}

	public Long getClassId() {
		return classId;
	}

	public void setClassId(Long classId) {
		this.classId = classId;
	}

	public Long getCardId() {
		return cardId;
	}

	public void setCardId(Long cardId) {
		this.cardId = cardId;
	}

	public List<DomainTreeCardNode> getChildren() {
		List<DomainTreeCardNode> childrenList = new ArrayList<DomainTreeCardNode>(children.values());
		Collections.sort(childrenList, new DomainTreeCardNodeComparator());

		return childrenList;
	}

	public void addChild(DomainTreeCardNode child) {
		children.put(child.getCardId(), child);
		child.setParent(this);
	}

	public DomainTreeCardNode getChildById(Object id) {
		return children.get(id);
	}

	public void setParent(DomainTreeCardNode parent) {
		this.parent = parent;
	}

	public DomainTreeCardNode parent() {
		return parent;
	}

	public void sortByText() {
		
	}
}