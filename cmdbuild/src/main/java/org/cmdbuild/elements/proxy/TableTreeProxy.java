package org.cmdbuild.elements.proxy;

import java.util.List;

import org.cmdbuild.elements.TableTree;
import org.cmdbuild.elements.interfaces.ITable;
import org.cmdbuild.services.auth.UserContext;
import org.cmdbuild.utils.tree.CNode;

public class TableTreeProxy extends TableTree {
	protected UserContext userCtx;

	public TableTreeProxy(TableTree tree, UserContext userCtx) {
		super(tree);
		this.userCtx = userCtx;
		removeUnprivilegedAndProxy();
	}

	private void removeUnprivilegedAndProxy() {
		if (this.getRootElement() != null) {
			removeUnprivilegedChildrenAndProxy(this.getRootElement());
			proxyNodeData(this.getRootElement());
		}
	}

	private void removeUnprivilegedChildrenAndProxy(CNode<ITable> node) {
		int size = node.getNumberOfChildren();
		List<CNode<ITable>> children = node.getChildren();
		for(int i = size -1; i >= 0; --i){
			CNode<ITable> child = children.get(i);
			if (userCtx.privileges().hasReadPrivilege(child.getData())) {
				removeUnprivilegedChildrenAndProxy(child);
				proxyNodeData(child);
			} else {
				node.removeChild(child);
			}
		}
	}

	private void proxyNodeData(CNode<ITable> node) {
		ITable unproxedChildData = node.getData();
		ITable proxedChildData = new TableProxy(unproxedChildData, userCtx);
		node.setData(proxedChildData);
	}
}
