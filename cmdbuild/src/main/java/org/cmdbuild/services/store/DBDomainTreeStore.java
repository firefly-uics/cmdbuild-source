package org.cmdbuild.services.store;

import java.util.HashMap;
import java.util.Map;

import org.cmdbuild.elements.filters.AttributeFilter.AttributeFilterType;
import org.cmdbuild.elements.interfaces.CardQuery;
import org.cmdbuild.elements.interfaces.ICard;
import org.cmdbuild.elements.interfaces.ITable;
import org.cmdbuild.model.DomainTreeNode;
import org.cmdbuild.services.auth.UserContext;

public class DBDomainTreeStore {
	private enum Attributes {
		TARGET_CLASS_NAME("TargetClassName"),
		TARGET_CLASS_DESCRIPTION("TargetClassDescription"),
		DIRECT("Direct"),
		DOMAIN_NAME("DomainName"),
		TYPE("Type"),
		ID_GROUP("IdGroup"),
		ID_PARENT("IdParent");

		private String name;

		Attributes(String name) {
			this.name = name;
		}

		public String getName(){
			return name;
		}
	}

	private static final String TABLE_NAME = "_DomainTreeNavigation";
	private static final ITable table = UserContext.systemContext()
			.tables().get(TABLE_NAME);

	public void createOrReplaceTree(final String treeType, final DomainTreeNode root) {
		removeTree(treeType);
		saveNode(treeType, root);
	}

	public void removeTree(final String treeType) {
		for (ICard c : table.cards().list().filter(Attributes.TYPE.getName(), AttributeFilterType.EQUALS, treeType)) {
			c.delete();
		}
	}

	public DomainTreeNode getDomainTree(String treeType) {
		CardQuery nodes = table.cards().list().filter(Attributes.TYPE.getName(), AttributeFilterType.EQUALS, treeType);
		Map<Long, DomainTreeNode> treeNodes = new HashMap<Long, DomainTreeNode>();

		DomainTreeNode root = null;

		for (ICard card:nodes) {
			DomainTreeNode currentTreeNode = cardToDomainTreeNode(card);
			for (DomainTreeNode treeNode:treeNodes.values()) {
				// Link children to current node
				if (treeNode.getIdParent() != null
						&& treeNode.getIdParent().equals(currentTreeNode.getId())) {
					currentTreeNode.addChildNode(treeNode);
				}
			}

			// link the currentNode as child of a node 
			// if already created
			if (currentTreeNode.getIdParent() != null) {
				DomainTreeNode maybeParent = treeNodes.get(currentTreeNode.getIdParent());
				if (maybeParent != null) {
					maybeParent.addChildNode(currentTreeNode);
				}
			} else {
				root = currentTreeNode;
			}

			treeNodes.put(currentTreeNode.getId(), currentTreeNode);
		}

		return root;
	}

	private void saveNode(final String treeType, final DomainTreeNode root) {
		ICard c = table.cards().create();
		c.setValue(Attributes.DIRECT.getName(), root.isDirect());
		c.setValue(Attributes.DOMAIN_NAME.getName(), root.getDomainName());
		c.setValue(Attributes.TYPE.getName(), treeType);
		c.setValue(Attributes.ID_GROUP.getName(), root.getIdGroup());
		c.setValue(Attributes.ID_PARENT.getName(), root.getIdParent());
		c.setValue(Attributes.TARGET_CLASS_NAME.getName(), root.getTargetClassName());
		c.setValue(Attributes.TARGET_CLASS_DESCRIPTION.getName(), root.getTargetClassDescription());
		c.save();

		Long id = Long.valueOf(c.getId());
		for (DomainTreeNode child: root.getChildNodes()) {
			child.setIdParent(id);
			saveNode(treeType, child);
		}
	}

	private DomainTreeNode cardToDomainTreeNode(ICard card) {
		DomainTreeNode domainTreeNode = new DomainTreeNode();
		domainTreeNode.setId(safeLongCast(card.getId()));
		domainTreeNode.setDirect((Boolean)card.getValue(Attributes.DIRECT.getName()));
		domainTreeNode.setDomainName((String)card.getValue(Attributes.DOMAIN_NAME.getName()));
		domainTreeNode.setType((String)card.getValue(Attributes.TYPE.getName()));
		domainTreeNode.setIdGroup(safeLongCast(card.getValue(Attributes.ID_GROUP.getName())));
		domainTreeNode.setIdParent(safeLongCast(card.getValue(Attributes.ID_PARENT.getName())));
		domainTreeNode.setTargetClassDescription((String)card.getValue(Attributes.TARGET_CLASS_DESCRIPTION.getName()));
		domainTreeNode.setTargetClassName(((String)card.getValue(Attributes.TARGET_CLASS_NAME.getName())));

		return domainTreeNode;
	}

	// the getValue method of a ICard return
	// an Object. For the Ids return a Integer
	// but we want long. Cast them ignoring the null values
	private Long safeLongCast(Object o) {
		if (o == null) {
			return null;
		} else {
			return new Long((Integer) o);
		}
	}
}