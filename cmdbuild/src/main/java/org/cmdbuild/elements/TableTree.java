package org.cmdbuild.elements;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.cmdbuild.elements.interfaces.ITable;
import org.cmdbuild.exception.NotFoundException;
import org.cmdbuild.exception.ORMException.ORMExceptionType;
import org.cmdbuild.utils.tree.CNode;
import org.cmdbuild.utils.tree.CTree;

public class TableTree extends CTree<ITable> implements Iterable<ITable> {

	/**
	 * Clones the original tree
	 * 
	 * @param user
	 * @param role
	 * @param tree
	 */
	public TableTree(CTree<ITable> tree) {
		super();
		if (tree.getRootElement() != null) {
			this.setRootElement(tree.getRootElement().clone());
		} else {
			this.setRootElement(null);
		}
	}

	private TableTree() {
		super();
	}

	public TableTree branch(String className) {
		TableTree branchTree = new TableTree();
		CNode<ITable> node = findNode(this.getRootElement(), className);
		if (node != null)
			branchTree.setRootElement(node);
		return branchTree;
	}

	public TableTree exclude(String excluded) {
		removeExcluded(this.getRootElement(), excluded);
		return this;
	}

	public TableTree active() {
		removeInactive(this.getRootElement());
		return this;
	}

	public TableTree displayable() {
		this.setRootElement(keepDisplayable(this.getRootElement()));
		return this;
	}

	public TableTree superclasses() {
		leaveSuperclasses(this.getRootElement());
		return this;
	}

	public boolean contains(String classname) {
		return (findNode(this.getRootElement(), classname) != null);
	}

	public boolean contains(int classId) {
		return (findNode(this.getRootElement(), classId) != null);
	}

	public boolean contains(ITable table) {
		return (findNode(this.getRootElement(), table.getId()) != null);
	}

	public Collection<String> path(String className) throws NotFoundException {
		LinkedList<String> listWithAncestors = new LinkedList<String>();
        for(CNode<ITable> node = findNode(this.getRootElement(), className);
        node!=null && node.getParent()!=null; 
        node = node.getParent()) {
        	listWithAncestors.addFirst(node.getData().getName());
        }
		return listWithAncestors;
	}

	public Collection<Integer> idPath(String className) throws NotFoundException {
		LinkedList<Integer> listWithAncestors = new LinkedList<Integer>();
        for(CNode<ITable> node = findNode(this.getRootElement(), className); node.getParent()!=null; node=node.getParent()) {
        	listWithAncestors.addFirst(node.getData().getId());
        }
		return listWithAncestors;
	}

	// Can be heavily optimized
	public Iterator<ITable> iterator() {
		List<ITable> list = new LinkedList<ITable>();
		buildList(this.getRootElement(), list);
		return list.iterator();
	}

    private void buildList(CNode<ITable> element, List<ITable> list) {
        list.add(element.getData());
        for (CNode<ITable> data : element.getChildren()) {
        	buildList(data, list);
        }
    }

	private CNode<ITable> findNode(CNode<ITable> node, String className) {
		CNode<ITable> rv = null;
		if(node.getData().getName().equals(className))
			rv = node;
		else if(node.getNumberOfChildren() > 0){
			for(CNode<ITable> child : node.children){
				CNode<ITable> childNode = findNode(child, className);
				if(childNode != null) {
					rv = childNode;
					break;
				}
			}
		}
		return rv;
	}

	private CNode<ITable> findNode(CNode<ITable> node, int classId) {
		CNode<ITable> rv = null;
		if(node.getData().getId() == classId)
			rv = node;
		else if(node.getNumberOfChildren() > 0){
			for(CNode<ITable> child : node.children){
				CNode<ITable> childNode = findNode(child, classId);
				if(childNode != null) {
					rv = childNode;
					break;
				}
			}
		}
		return rv;
	}

	private void removeExcluded(CNode<ITable> node, String excluded) {
		if (node == null)
			return;
		int size = node.getNumberOfChildren();
		List<CNode<ITable>> children = node.getChildren();
		for(int i = size -1; i >= 0; --i){
			CNode<ITable> child = children.get(i);
			if (excluded.equals(child.getData().getName()))
				node.removeChild(child);
			else
				removeExcluded(child, excluded);
		}
	}

	private void removeInactive(CNode<ITable> node) {
		if (node == null)
			return;
		int size = node.getNumberOfChildren();
		List<CNode<ITable>> children = node.getChildren();
		for(int i = size -1; i >= 0; --i){
			CNode<ITable> child = children.get(i);
			if (child.getData().getStatus().isActive())
				removeInactive(child);
			else
				node.removeChild(child);
		}
	}

	private CNode<ITable> keepDisplayable(CNode<ITable> node) {
		if (node == null)
			return null;
		ITable table = node.getData();
		if (table == null || !table.getMode().isDisplayable())
			return null;
		int size = node.getNumberOfChildren();
		List<CNode<ITable>> children = node.getChildren();
		for(int i = size -1; i >= 0; --i) {
			CNode<ITable> child = children.get(i);
			if (keepDisplayable(child) == null)
				node.removeChild(child);
		}
		return node;
	}

	private void leaveSuperclasses(CNode<ITable> node) {
		if (node == null)
			return;
		int size = node.getNumberOfChildren();
		List<CNode<ITable>> children = node.getChildren();
		for(int i = size -1; i >= 0; --i){
			CNode<ITable> child = children.get(i);
			if (!child.getData().isSuperClass())
				node.removeChild(child);
			else
				leaveSuperclasses(child);
		}
	}

	public static void checkIfChild(ITable subClass, ITable superClass) {
		if (!TableImpl.tree().branch(superClass.getName()).contains(subClass.getId()))
			throw ORMExceptionType.ORM_ERROR_INCOMPATIBLE_CLASS.createException(subClass.getName());
	}
}
