package org.cmdbuild.elements.proxy;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.cmdbuild.elements.TableTree;
import org.cmdbuild.elements.TableImpl.OrderEntry;
import org.cmdbuild.elements.interfaces.CardFactory;
import org.cmdbuild.elements.interfaces.IAttribute;
import org.cmdbuild.elements.interfaces.ITable;
import org.cmdbuild.exception.NotFoundException;
import org.cmdbuild.exception.ORMException;
import org.cmdbuild.services.meta.MetadataMap;

public class TableForwarder implements ITable {

	protected ITable t;

	protected TableForwarder(ITable t) {
		this.t = t;
	}

	public CardFactory cards() { return t.cards(); }
	public TableTree treeBranch() { return t.treeBranch(); }
	public boolean equals(Object o) { return t.equals(o); }

	public String toString() { return t.toString(); }

	public void delete() throws ORMException { t.delete(); }
	public String getDescription() { return t.getDescription(); }
	public ITable getParent() { return t.getParent(); }
	public boolean isNew() { return t.isNew(); }
	public boolean isSuperClass() { return t.isSuperClass(); }
	public void save() throws ORMException { t.save(); }
	public void setDescription(String description) { t.setDescription(description); }
	public void setParent(String parent) throws NotFoundException { t.setParent(parent); }
	public void setParent(Integer parent) throws NotFoundException { t.setParent(parent); }
	public void setParent(ITable parent) { t.setParent(parent); }
	public void setSuperClass(boolean isSuperClass) { t.setSuperClass(isSuperClass); }
	public void addAttribute(IAttribute attribute) { t.addAttribute(attribute); }
	public IAttribute getAttribute(String name) { return t.getAttribute(name); }
	public Map<String, IAttribute> getAttributes() { return t.getAttributes(); }
	public String getDBName() { return t.getDBName(); }
	public Mode getMode() { return t.getMode(); }
	public String getName() { return t.getName(); }
	public int getId() { return t.getId(); }
	public SchemaStatus getStatus() { return t.getStatus(); }
	public void setMode(String mode) { t.setMode(mode); }
	public void setName(String name) { t.setName(name);	}
	public void setStatus(SchemaStatus status) { t.setStatus(status); }
	public MetadataMap getMetadata() { return t.getMetadata(); }
	public List<OrderEntry> getOrdering() { return t.getOrdering(); }
	public boolean isActivity() { return t.isActivity(); }
	public ArrayList<ITable> getChildren() { return t.getChildren(); }
	public boolean hasChild() { return t.hasChild(); }
	public boolean isTheTableClass() { return t.isTheTableClass(); }
	public boolean isTheTableActivity() { return t.isTheTableActivity(); }
	public boolean isAllowedOnTrees() { return t.isAllowedOnTrees(); }
	public Map<String, String> genDataDefinitionMeta() { return t.genDataDefinitionMeta(); }
	public void readDataDefinitionMeta(Map<String, String> dataDefinitionMeta) { t.readDataDefinitionMeta(dataDefinitionMeta); }

	public CMTableType getTableType() { return t.getTableType(); }
	public void setTableType(CMTableType type) { t.setTableType(type); }

	public Iterable<IAttribute> fkDetails() { return t.fkDetails(); }
	public String getPrivilegeId() { return t.getPrivilegeId(); }

	public boolean isUserStoppable() { return t.isUserStoppable(); }
	public void setUserStoppable(boolean userStoppable) { t.setUserStoppable(userStoppable); };
}
