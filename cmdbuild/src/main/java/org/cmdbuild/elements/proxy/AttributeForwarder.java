package org.cmdbuild.elements.proxy;

import java.util.Map;

import org.cmdbuild.elements.DirectedDomain;
import org.cmdbuild.elements.LookupType;
import org.cmdbuild.elements.interfaces.BaseSchema;
import org.cmdbuild.elements.interfaces.IAttribute;
import org.cmdbuild.elements.interfaces.IDomain;
import org.cmdbuild.elements.interfaces.ITable;
import org.cmdbuild.exception.NotFoundException;
import org.cmdbuild.exception.ORMException;
import org.cmdbuild.services.meta.MetadataMap;

public class AttributeForwarder implements IAttribute {

	protected IAttribute a;

	protected AttributeForwarder(IAttribute a) { this.a = a; }

	public void delete() { a.delete(); }
	public String getDefaultValue() { return a.getDefaultValue(); }
	public String getDescription() { return a.getDescription(); }
	public FieldMode getFieldMode() { return a.getFieldMode(); }
	public int getIndex() { return a.getIndex(); }
	public int getLength() { return a.getLength(); }
	public LookupType getLookupType() { return a.getLookupType(); }
	public int getPrecision() { return a.getPrecision(); }
	public IDomain getReferenceDomain() { return a.getReferenceDomain(); }
	public ITable getReferenceTarget() { return a.getReferenceTarget(); }
	public String getReferenceType() { return a.getReferenceType(); }
	public String getFilter() {	return a.getFilter(); }
	public int getScale() { return a.getScale(); }
	public BaseSchema getSchema() { return a.getSchema(); }
	public SchemaStatus getStatus() { return a.getStatus(); }
	public AttributeType getType() { return a.getType(); }
	public boolean isBaseDSP() { return a.isBaseDSP(); }
	public boolean isDisplayable() { return a.isDisplayable(); }
	
	public boolean isReserved() { return a.isReserved(); }
	
	public boolean isLocal() { return a.isLocal(); }
	public boolean isNotNull() { return a.isNotNull(); }
	public boolean isReferenceDirect() { return a.isReferenceDirect(); }
	public boolean isUnique() { return a.isUnique(); }
	public void save() throws ORMException { a.save(); }
	public void setBaseDSP(boolean isBaseDSP) { a.setBaseDSP(isBaseDSP); }
	public void setDefaultValue(String defaultValue) { a.setDefaultValue(defaultValue); }
	public void setDescription(String description) { a.setDescription(description); }
	public void setFieldMode(String modeName) { a.setFieldMode(modeName); }
	public void setIndex(int index) { a.setIndex(index); }
	public void setIsReferenceDirect(boolean isReferenceDirect) { a.setIsReferenceDirect(isReferenceDirect); }
	public void setLength(int length) { a.setLength(length); }
	public void setLookupType(String lookupName) { a.setLookupType(lookupName); }
	public void setName(String name) { a.setName(name); }
	public void setNotNull(boolean isNotNull) { a.setNotNull(isNotNull); }
	public void setPrecision(int precision) { a.setPrecision(precision); }
	public void setReferenceDomain(IDomain domain) { a.setReferenceDomain(domain); }
	public void setReferenceDomain(String domainName) throws NotFoundException { a.setReferenceDomain(domainName); }
	public void setReferenceDomain(int idDomain) throws NotFoundException { a.setReferenceDomain(idDomain); }
	public void setReferenceType(String referenceType) { a.setReferenceType(referenceType); }
	public void setFilter(String referenceQuery) { a.setFilter(referenceQuery); }
	public void setFilterSafe(String referenceQuery) { a.setFilterSafe(referenceQuery); }
	public void setScale(int scale) { a.setScale(scale); }
	public void setSchema(BaseSchema schema) { a.setSchema(schema); }
	public void setStatus(SchemaStatus status) { a.setStatus(status); }
	public void setUnique(boolean isUnique) { a.setUnique(isUnique); }

	public void addAttribute(IAttribute attribute) { a.addAttribute(attribute); }
	public IAttribute getAttribute(String name) throws NotFoundException { return a.getAttribute(name); }
	public Map<String, IAttribute> getAttributes() { return a.getAttributes(); }
	public String getDBName() { return a.getDBName(); }
	public Mode getMode() { return a.getMode(); }
	public String getName() { return a.getName(); }
	public int getId() { return a.getId(); }
	public void setMode(String mode) { a.setMode(mode); }

	public MetadataMap getMetadata() { return a.getMetadata(); }

	public int getClassOrder() { return a.getClassOrder(); }
	public void setClassOrder(int classOrder) { a.setClassOrder(classOrder); }

	public DirectedDomain getReferenceDirectedDomain() {return a.getReferenceDirectedDomain(); }

	public Map<String, String> genDataDefinitionMeta() { return a.genDataDefinitionMeta(); }
	public void readDataDefinitionMeta(Map<String, String> dataDefinitionMeta) { a.readDataDefinitionMeta(dataDefinitionMeta); }

	public Object readValue(Object maybeValue) { return a.readValue(maybeValue); }
	public String valueToDBFormat(Object value) { return a.valueToDBFormat(value); }
	public String valueToString(Object value) { return a.valueToString(value); }

	public CMTableType getTableType() { return a.getTableType(); }
	public void setTableType(CMTableType type) { a.setTableType(type); }

	public ITable getFKTargetClass() { return a.getFKTargetClass(); }
	public void setFKTargetClass(String value) { a.setFKTargetClass(value); }

	public String getGroup() { return a.getGroup(); }
	public void setGroup(String value) { a.setGroup(value); }

	public String getEditorType() {return a.getEditorType();}
	public void setEditorType(String editorType) { a.setEditorType(editorType);}
}
