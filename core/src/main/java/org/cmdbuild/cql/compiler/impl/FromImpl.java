package org.cmdbuild.cql.compiler.impl;

import java.util.ArrayList;
import java.util.List;

import org.cmdbuild.cql.compiler.From;
import org.cmdbuild.cql.compiler.from.ClassDeclaration;

@SuppressWarnings("unchecked")
public class FromImpl extends CQLElementImpl implements From  {

	boolean history = false;
	List<ClassDeclarationImpl> declarations = new ArrayList<ClassDeclarationImpl>();
	
	public void setHistory(boolean history) {
		this.history = history;
	}
	public boolean isHistory() {
		return history;
	}

	public void add(ClassDeclaration classDecl) {
		declarations.add((ClassDeclarationImpl)classDecl);
	}


	public ClassDeclarationImpl mainClass() {
		return declarations.get(0);
	}

	public ClassDeclarationImpl searchClass(String nameOrRef) {
		for(ClassDeclarationImpl c : declarations) {
			if(c.isClass(nameOrRef)) {
				return c;
			}
		}
		return null;
	}

	public DomainDeclarationImpl searchDomain(String nameOrRef) {
		for(ClassDeclarationImpl c : declarations) {
			DomainDeclarationImpl out = c.searchDomain(nameOrRef);
			if(out != null) return out;
		}
		return null;
	}
	
	public void check() {
		for(ClassDeclarationImpl c : declarations) {
			c.check();
		}
	}
}
