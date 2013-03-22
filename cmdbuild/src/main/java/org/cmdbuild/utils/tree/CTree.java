package org.cmdbuild.utils.tree;

import java.util.ArrayList;
import java.util.List;

/**
 * 
 * COPIED from http://sujitpal.blogspot.com/2006/05/java-data-structure-generic-tree.html
 * 
 * Represents a Tree of Objects of generic type T. The Tree is represented as
 * a single rootElement which points to a List<Node<T>> of children. There is
 * no restriction on the number of children that a particular node may have.
 * This Tree provides a method to serialize the Tree into a List by doing a
 * pre-order traversal. It has several methods to allow easy updation of Nodes
 * in the Tree.
 */
public class CTree<T> implements Cloneable {
 
    private CNode<T> rootElement;
     
    /**
     * Default ctor.
     */
    public CTree() {
        super();
    }
 
    /**
     * Return the root Node of the tree.
     * @return the root element.
     */
    public CNode<T> getRootElement() {
        return this.rootElement;
    }
 
    /**
     * Set the root Element for the tree.
     * @param rootElement the root element to set.
     */
    public void setRootElement(CNode<T> rootElement) {
        this.rootElement = rootElement;
    }
     
    /**
     * Returns the Tree<T> as a List of Node<T> objects. The elements of the
     * List are generated from a pre-order traversal of the tree.
     * @return a List<Node<T>>.
     */
    public List<CNode<T>> toList() {
        List<CNode<T>> list = new ArrayList<CNode<T>>();
        walk(rootElement, list);
        return list;
    }
     
    /**
     * Returns a String representation of the Tree. The elements are generated
     * from a pre-order traversal of the Tree.
     * @return the String representation of the Tree.
     */
    public String toString() {
        return toList().toString();
    }
     
    /**
     * Walks the Tree in pre-order style. This is a recursive method, and is
     * called from the toList() method with the root element as the first
     * argument. It appends to the second argument, which is passed by reference     * as it recurses down the tree.
     * @param element the starting element.
     * @param list the output of the walk.
     */
    private void walk(CNode<T> element, List<CNode<T>> list) {
        list.add(element);
        for (CNode<T> data : element.getChildren()) {
            walk(data, list);
        }
    }

    /**
     * Return all the leaves of the tree
     * @param element the starting element. if null it starts from root node
     * @return the list of leaves 
     */
	public List<T> getLeaves(CNode<T> element) {
		List<T> list = new ArrayList<T>();
		if(element == null) element = getRootElement();
		if(element.getNumberOfChildren() > 0) {
	        for (CNode<T> data : element.getChildren()) {
	            list.addAll(getLeaves(data));
	        }
		}
		else {
			list.add(element.getData());
		}
		return list;
	}

	public List<T> getLeaves() {
		return getLeaves(null);
	}
	
    /**
     * Return a copy of the tree structure.
     * Note that data of nodes are not cloned 
     * @return the copy of this tree 
     */
	public CTree<T> clone() {
		CTree<T> copyTree = new CTree<T>();
		CNode<T> root = this.rootElement.clone();
		copyTree.setRootElement(root);
		return copyTree;
	}
}