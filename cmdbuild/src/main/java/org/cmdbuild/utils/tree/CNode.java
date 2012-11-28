package org.cmdbuild.utils.tree;

import java.util.ArrayList;
import java.util.List;

/**
 * 
 * COPIED from http://sujitpal.blogspot.com/2006/05/java-data-structure-generic-tree.html
 * 
 * Represents a node of the Tree<T> class. The Node<T> is also a container, and
 * can be thought of as instrumentation to determine the location of the type T
 * in the Tree<T>.
 */
public class CNode<T> implements Cloneable {
 
    public T data;
    public List<CNode<T>> children;
    
    public CNode<T> parent;
 
    /**
     * Default ctor.
     */
    public CNode() {
        super();
    }
 
    /**
     * Convenience ctor to create a Node<T> with an instance of T.
     * @param data an instance of T.
     */
    public CNode(T data) {
        this();
        setData(data);
    }
     
    /**
     * Return the children of Node<T>. The Tree<T> is represented by a single
     * root Node<T> whose children are represented by a List<Node<T>>. Each of
     * these Node<T> elements in the List can have children. The getChildren()
     * method will return the children of a Node<T>.
     * @return the children of Node<T>
     */
    public List<CNode<T>> getChildren() {
        if (this.children == null) {
            return new ArrayList<CNode<T>>();
        }
        return this.children;
    }
 
    /**
     * Sets the children of a Node<T> object. See docs for getChildren() for
     * more information.
     * @param children the List<Node<T>> to set.
     */
    public void setChildren(List<CNode<T>> children) {
        this.children = children;
    }
 
    /**
     * Return the parent of Node<T>.
     * @return the parent of Node<T>
     */
    public CNode<T> getParent() {
        return this.parent;
    }
 
    /**
     * Sets the parent of a Node<T> object.
     * @param parent the Node<T> to set.
     */
    public void setParent(CNode<T> parent) {
        this.parent = parent;
    }
 
    /**
     * Returns the number of immediate children of this Node<T>.
     * @return the number of immediate children.
     */
    public int getNumberOfChildren() {
        if (children == null) {
            return 0;
        }
        return children.size();
    }
     
    /**
     * Adds a child to the list of children for this Node<T>. The addition of
     * the first child will create a new List<Node<T>>.
     * @param child a Node<T> object to set.
     */
    public void addChild(CNode<T> child) {
        if (children == null) {
            children = new ArrayList<CNode<T>>();
        }
        children.add(child);
        child.setParent(this);
    }
     
    /**
     * Inserts a Node<T> at the specified position in the child list. Will     * throw an ArrayIndexOutOfBoundsException if the index does not exist.
     * @param index the position to insert at.
     * @param child the Node<T> object to insert.
     * @throws IndexOutOfBoundsException if thrown.
     */
    public void insertChildAt(int index, CNode<T> child) throws IndexOutOfBoundsException {
        if (index == getNumberOfChildren()) {
            // this is really an append
            addChild(child);
            return;
        } else {
            children.get(index); //just to throw the exception, and stop here
            children.add(index, child);
            child.setParent(this);
        }
    }
     
    /**
     * Remove the Node<T> element at index index of the List<Node<T>>.
     * @param index the index of the element to delete.
     * @throws IndexOutOfBoundsException if thrown.
     */
    public void removeChildAt(int index) throws IndexOutOfBoundsException {
        children.remove(index);
    }
 
    /**
     * Remove the Node<T> element of the List<Node<T>>.
     * @param obj the element to delete.
     * @throws ClassCastException if thrown.
     * @throws NullPointerException if thrown.
     */
    public void removeChild(CNode<T> obj) throws ClassCastException, NullPointerException{
    	children.remove(obj);
    }
 
    /**
     * Return a copy of the node.
     * Note that data of node are not cloned 
     * @return the copy of this tree 
     */
	public CNode<T> clone() {
		CNode<T> copyNode = new CNode<T>();
		copyNode.setData(this.data);
		if(this.getNumberOfChildren() > 0) {
			for(CNode<T> child : children) {
				CNode<T> copyChild = child.clone();
				copyNode.addChild(copyChild);
			}
		}
		return copyNode;
	}

	public T getData() {
        return this.data;
    }
 
    public void setData(T data) {
        this.data = data;
    }
     
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("{").append(getData().toString()).append(",[");
        int i = 0;
        for (CNode<T> e : getChildren()) {
            if (i > 0) {
                sb.append(",");
            }
            sb.append(e.getData().toString());
            i++;
        }
        sb.append("]").append("}");
        return sb.toString();
    }

}