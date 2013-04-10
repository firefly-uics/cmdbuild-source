package org.cmdbuild.cql.compiler.impl;

import java.util.ArrayList;
import java.util.List;

import org.cmdbuild.cql.compiler.GroupBy;
import org.cmdbuild.cql.compiler.from.ClassDeclaration;
import org.cmdbuild.cql.compiler.from.DomainDeclaration;

public class GroupByImpl extends CQLElementImpl implements GroupBy {

	List<GroupByElement> elements = new ArrayList<GroupByElement>();
	
	public void add(ClassDeclaration classDecl, String attributeName) {
		elements.add(new GroupByElement(classDecl,attributeName));
	}

	public void add(DomainDeclaration domainDecl, String attributeName) {
		elements.add(new GroupByElement(domainDecl,attributeName));
	}

	public List<GroupByElement> getElements() {
		return elements;
	}

}
