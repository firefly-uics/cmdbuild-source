package org.cmdbuild.elements.proxy;

import java.util.Map;

import org.cmdbuild.elements.interfaces.IAttribute;
import org.cmdbuild.elements.interfaces.IDomain;
import org.cmdbuild.elements.interfaces.ITable;
import org.cmdbuild.exception.NotFoundException;
import org.cmdbuild.exception.ORMException;
import org.cmdbuild.services.meta.MetadataMap;

public class DomainForwarder implements IDomain {

	protected IDomain d;

	protected DomainForwarder(IDomain d) {
		this.d = d;
	}

	public void delete() { d.delete(); }
	public void save() { d.save(); }

	public String getCardinality() { return d.getCardinality(); }
	public String getDBName() { return d.getDBName(); }
	public String getDescription() { return d.getDescription(); }
	public String getDescriptionDirect() { return d.getDescriptionDirect(); }
	public String getDescriptionInverse() { return d.getDescriptionInverse(); }
	public int getId() { return d.getId(); }
	public int getOpenedRows() { return d.getOpenedRows(); }
	@Deprecated public ITable[] getTables() { return d.getTables(); }
	public ITable getClass1() { return d.getClass1(); }
	public ITable getClass2() { return d.getClass2(); }
	public String getType() { return d.getType(); }
	public boolean isLocal(ITable table) { return d.isLocal(table); }
	public boolean isMasterDetail() { return d.isMasterDetail(); }
	public boolean isNew() { return d.isNew(); }
	public void setCardinality(String cardinality) { d.setCardinality(cardinality); }
	public void setDescription(String description) { d.setDescription(description); }
	public void setDescriptionDirect(String descriptionDirect) { d.setDescriptionDirect(descriptionDirect); }
	public void setDescriptionInverse(String descriptionInverse) { d.setDescriptionInverse(descriptionInverse); }
	public void setMasterDetail(boolean isMasterDetail) { d.setMasterDetail(isMasterDetail); }
	public String getMDLabel() { return d.getMDLabel(); }
	public void setMDLabel(String mdLabel) { d.setMDLabel(mdLabel); }
	public void setOpenedRows(int openedRows) { d.setOpenedRows(openedRows); }
	public void setClass1(ITable table) { d.setClass1(table); }
	public void setClass2(ITable table) { d.setClass2(table); }
	public void addAttribute(IAttribute attribute) { d.addAttribute(attribute); }
	public IAttribute getAttribute(String name) throws NotFoundException { return d.getAttribute(name); }
	public Map<String, IAttribute> getAttributes() { return d.getAttributes(); }
	public Mode getMode() { return d.getMode(); }
	public String getName() { return d.getName(); }
	public SchemaStatus getStatus() { return d.getStatus(); }
	public void setMode(String mode) { d.setMode(mode); }
	public void setName(String name) { d.setName(name); }
	public void setStatus(SchemaStatus status) { d.setStatus(status); }
	public boolean getDirectionFrom(ITable sourceTable) throws ORMException { return d.getDirectionFrom(sourceTable); }

	public boolean equals(Object o) { return d.equals(o); }

	public MetadataMap getMetadata() { return d.getMetadata(); }

	public Map<String, String> genDataDefinitionMeta() { return d.genDataDefinitionMeta(); }
	public void readDataDefinitionMeta(Map<String, String> dataDefinitionMeta) { d.readDataDefinitionMeta(dataDefinitionMeta); }

	public CMTableType getTableType() { return d.getTableType(); }
	public void setTableType(CMTableType type) { d.setTableType(type); }

	public String getPrivilegeId() { return d.getPrivilegeId(); }
}
