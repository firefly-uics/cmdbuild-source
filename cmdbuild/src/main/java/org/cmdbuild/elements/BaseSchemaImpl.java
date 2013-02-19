package org.cmdbuild.elements;

import java.util.Map;

import org.cmdbuild.dao.backend.CMBackend;
import org.cmdbuild.elements.interfaces.BaseSchema;
import org.cmdbuild.elements.interfaces.IAttribute;
import org.cmdbuild.exception.NotFoundException;
import org.cmdbuild.exception.NotFoundException.NotFoundExceptionType;
import org.cmdbuild.services.meta.MetadataMap;
import org.cmdbuild.services.meta.MetadataService;
import org.springframework.beans.factory.annotation.Autowired;

public abstract class BaseSchemaImpl implements BaseSchema {

	private static final long serialVersionUID = 1L;

	@Autowired
	protected CMBackend backend = CMBackend.INSTANCE;

	protected int oid;

	protected String name;
	private CMTableType type;

	protected Mode mode;
	protected SchemaStatus status;

	private Map<String, IAttribute> attributes;

	// should be set by constructor method only
	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	/******************** TODO: move this in the superclass of domain and class! *******************/
	// should be set by constructor method only
	public void setTableType(CMTableType type) {
		this.type = type;
	}

	public CMTableType getTableType() {
		return type;
	}

	public void setMode(String modeName) {
		Mode mode = Mode.getValueOf(modeName);
		this.mode = mode;
	}

	public Mode getMode() {
		return mode;
	}

	public void setStatus(SchemaStatus status) {
		this.status = status;
	}

	public SchemaStatus getStatus() {
		return status;
	}

	public int getId() {
		return oid;
	}

	public void addAttribute(IAttribute attribute) {
		attributes.put(attribute.getName(), attribute);
	}

	public Map<String, IAttribute> getAttributes() {
		if (attributes == null) {
			attributes = loadAttributes();
		}
		return attributes;
	}

	protected Map<String, IAttribute> loadAttributes() {
		return backend.findAttributes(this);
	}

	public IAttribute getAttribute(String name) throws NotFoundException {
		IAttribute attribute = getAttributes().get(name);
		if (attribute != null)
			return attribute;
		else
			throw NotFoundExceptionType.ATTRIBUTE_NOTFOUND.createException(this.toString(), name);
	}

	// TODO: Change this ugliness
	public final String getDBName() {
		return getDBNameNotQuoted().replace(".","\".\"");
	}

	protected String getDBNameNotQuoted() {
		return getName();
	}

	public boolean equals(Object o) {
		if (o instanceof BaseSchema) {
			BaseSchema b = ((BaseSchema) o);
			return ((this.oid > 0) && this.oid == b.getId());
		}
		return false;
	}

	public int hashCode() {
		return this.oid;
	}

	public String toString() {
		return name;
	}

	public boolean isDisplayable() {
		return true;
	}

	abstract protected boolean isNew();

	public final MetadataMap getMetadata() {
		return MetadataService.of(this).getMetadataMap();
	}

	abstract public void readDataDefinitionMeta(Map<String, String> dataDefinitionMeta);

	abstract public Map<String, String> genDataDefinitionMeta();
}
