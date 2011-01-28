package org.cmdbuild.cql.compiler.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.cmdbuild.cql.CQLBuilderListener.WhereType;
import org.cmdbuild.cql.compiler.from.FromElement;
import org.cmdbuild.cql.compiler.where.WhereElement;

public class WhereElementImpl extends CQLElementImpl implements WhereElement {

	boolean isNot = false;
	WhereType type;
	FromElement scope;
	List<WhereElement> elements = new ArrayList<WhereElement>();
	
	public void add(WhereElement element) {
		elements.add(element);
	}

	public Collection<WhereElement> getElements() {
		return elements;
	}

	public FromElement getScope() {
		return scope;
	}

	public WhereType getType() {
		return type;
	}

	public boolean isNot() {
		return isNot;
	}

	public void setIsNot(boolean isNot) {
		this.isNot = isNot;
	}

	public void setScope(FromElement classOrDomain) {
		this.scope = classOrDomain;
	}

	public void setType(WhereType type) {
		this.type = type;
	}

}
