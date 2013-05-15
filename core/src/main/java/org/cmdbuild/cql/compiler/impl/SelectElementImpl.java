package org.cmdbuild.cql.compiler.impl;

import java.util.ArrayList;
import java.util.List;

import org.cmdbuild.cql.compiler.from.FromElement;
import org.cmdbuild.cql.compiler.select.SelectElement;
import org.cmdbuild.cql.compiler.select.SelectItem;

public class SelectElementImpl<T extends FromElement> extends CQLElementImpl implements
		SelectElement<T> {

	T declaration = null;
	
	List<SelectItem> elements = new ArrayList<SelectItem>();
	public void add(SelectItem item) {
		elements.add(item);
	}
	public List<SelectItem> getElements() {
		return elements;
	}

	public T getDeclaration() {
		return declaration;
	}

	public void setDeclaration(T declaration) {
		this.declaration = declaration;
	}

}
