package org.cmdbuild.workflow.utils;

import org.cmdbuild.common.annotations.Legacy;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Really deadly simple xml node producer
 */
@Legacy("Remove when REST is finally gone")
public class SimpleXMLNode{
	
	private Document document;
	private Element element;
	private SimpleXMLNode parent;

//	private boolean toQuote = true;
//	public SimpleXMLNode setToQuote(boolean q){this.toQuote=q;return this;}
	

	
	public SimpleXMLNode(Document document, String name){
		this.document=document;
		this.element = document.createElement(name);
	}
	
	/**
	 * Put attribute, return this node
	 * @param key
	 * @param value
	 * @return
	 */
	public SimpleXMLNode put(String key,String value){ 
		element.setAttribute(key, value); 
		return this; 
	}
	
	public SimpleXMLNode put(String key,Object value){
		return put(key,value.toString());
	}
	/**
	 * Copy an attribute value, return this node
	 * @param from
	 * @param to
	 * @return
	 */
	public SimpleXMLNode copy(String from, String newkey) { 
		element.setAttribute(newkey, element.getAttribute(from)); 
		return this; 
	}
	/**
	 * Add a children, return this node
	 * @param child
	 * @return
	 */
	public SimpleXMLNode add(SimpleXMLNode child){ 
		element.appendChild(child.element());
		child.setParent(this); 
		return this; 
	}
	/**
	 * Set the text value, return this node
	 * @param value
	 * @return
	 */
	public SimpleXMLNode set(String value){ 
		if(value==null)
			return this;
		this.element.setTextContent(value); 
		return this; 
	}
	
	public SimpleXMLNode set(Object value){ 
		if(value==null)
			return this;
		this.element.setTextContent(value.toString()); 
		return this; 
	}
	

	public boolean hasChilds(){ return element.hasChildNodes(); }
	public boolean hasValue(){
		String value = element.getNodeValue();
		return value != null && value.length()>0;
	}
	public boolean isAutoClose(){ return( !hasChilds()&&!hasValue() ); }
	
	public boolean isCDataRequired() {
		if(element.getNodeValue() == null) {
			return false;
		}
		return (element.getNodeValue().indexOf('<')>=0 || (element.getNodeValue().indexOf('&')>=0));
	}
	
	/**
	 * Create an XML child node with the childname tagname. Return the child node
	 * @param childname
	 * @return
	 */
	public SimpleXMLNode createChild(String childname){
		SimpleXMLNode child = new SimpleXMLNode(this.document, childname);
		this.add(child);
		return child;
	}
	
	public void setParent(SimpleXMLNode parent){
		this.parent = parent;
	}

	public SimpleXMLNode parent(){ 
		return this.parent; 
	}
	
	public Element element(){ 
		return this.element; 
	}
}
