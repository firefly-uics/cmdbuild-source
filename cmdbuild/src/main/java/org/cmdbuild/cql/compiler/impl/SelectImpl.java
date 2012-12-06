package org.cmdbuild.cql.compiler.impl;

import java.util.ArrayList;
import java.util.List;

import org.cmdbuild.cql.compiler.Select;
import org.cmdbuild.cql.compiler.from.ClassDeclaration;
import org.cmdbuild.cql.compiler.from.DomainDeclaration;
import org.cmdbuild.cql.compiler.select.ClassSelect;
import org.cmdbuild.cql.compiler.select.DomainMetaSelect;
import org.cmdbuild.cql.compiler.select.DomainObjectsSelect;
import org.cmdbuild.cql.compiler.select.FunctionSelect;
import org.cmdbuild.cql.compiler.select.SelectElement;

@SuppressWarnings("unchecked")
public class SelectImpl extends CQLElementImpl implements Select {
	boolean isDefault = false;
	public void setDefault(){
		isDefault = true;
	}
	public boolean isDefault() {
		return isDefault;
	}
	
	boolean isAll = false;
	List<SelectElement> elements = new ArrayList<SelectElement>();

	public void add(FunctionSelect fun) {
		elements.add(fun);
	}
	public void add(ClassSelect classSelect) {
		elements.add(classSelect);
	}
	public void add(DomainMetaSelect domMeta) {
		elements.add(domMeta);
	}
	public void add(DomainObjectsSelect domObjs) {
		elements.add(domObjs);
	}

	public ClassSelectImpl get(ClassDeclaration classDecl) {
		for(SelectElement el : elements) {
			if(el instanceof ClassSelectImpl) {
				if( ((ClassSelectImpl)el).declaration.equals(classDecl) )
					return (ClassSelectImpl)el;
			}
		}
		return null;
	}
	public DomainMetaSelectImpl getMeta(DomainDeclaration domainDecl) {
		for(SelectElement el : elements) {
			if(el instanceof DomainMetaSelectImpl) {
				if( ((DomainMetaSelectImpl)el).declaration.equals(domainDecl) )
					return (DomainMetaSelectImpl)el;
			}
		}
		return null;
	}
	public DomainObjectsSelectImpl getObjects(
			DomainDeclaration domainDecl) {
		for(SelectElement el : elements) {
			if(el instanceof DomainObjectsSelectImpl) {
				if( ((DomainObjectsSelectImpl)el).declaration.equals(domainDecl) )
					return (DomainObjectsSelectImpl)el;
			}
		}
		return null;
	}

	public DomainMetaSelectImpl getMetaOrCreate(
			DomainDeclaration domainDecl) {
		DomainMetaSelectImpl out = getMeta(domainDecl);
		if(out == null) {
			out = (DomainMetaSelectImpl)this.factory.createDomainMetaSelect(this,domainDecl);
		}
		return out;
	}
	public DomainObjectsSelectImpl getObjectsOrCreate(
			DomainDeclaration domainDecl) {
		DomainObjectsSelectImpl out = getObjects(domainDecl);
		if(out == null) {
			out = (DomainObjectsSelectImpl)this.factory.createDomainObjectsSelect(this,domainDecl);
		}
		return out;
	}
	public ClassSelectImpl getOrCreate(ClassDeclaration classDecl) {
		ClassSelectImpl out = get(classDecl);
		if(out == null) {
			out = (ClassSelectImpl)this.factory.createClassSelect(this,classDecl);
		}
		return out;
	}

	public boolean isSelectAll() {
		return isAll;
	}

	public void setSelectAll() {
		isAll = true;
	}

	public List<SelectElement> getElements() {
		return elements;
	}
	
}
