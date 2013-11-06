package org.cmdbuild.logic.data;

import java.util.List;

import org.cmdbuild.dao.entrytype.CMAttribute;
import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.dao.entrytype.CMDomain;
import org.cmdbuild.dao.view.CMDataView;
import org.cmdbuild.logic.Logic;
import org.cmdbuild.model.data.Attribute;
import org.cmdbuild.model.data.ClassOrder;
import org.cmdbuild.model.data.Domain;
import org.cmdbuild.model.data.EntryType;

public interface DataDefinitionLogic extends Logic {

	public abstract CMDataView getView();

	/**
	 * if forceCreation is true, check if already exists a table with the same
	 * name of the given entryType
	 */
	public abstract CMClass createOrUpdate(EntryType entryType, boolean forceCreation);

	public abstract CMClass createOrUpdate(EntryType entryType);

	/**
	 * TODO: delete also privileges that refers to the deleted class
	 */
	public abstract void deleteOrDeactivate(String className);

	public abstract CMAttribute createOrUpdate(Attribute attribute);

	public abstract void deleteOrDeactivate(Attribute attribute);

	public abstract void reorder(Attribute attribute);

	public abstract void changeClassOrders(String className, List<ClassOrder> classOrders);

	/**
	 * @deprecated use the create method and update methods only
	 */
	public abstract CMDomain createOrUpdate(Domain domain);

	public abstract CMDomain create(Domain domain);

	public abstract CMDomain update(Domain domain);

	public abstract void deleteDomainByName(String name);

}