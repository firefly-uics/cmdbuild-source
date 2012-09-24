package org.cmdbuild.elements;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.cmdbuild.elements.interfaces.ICard.CardAttributes;
import org.cmdbuild.elements.interfaces.ITable;
import org.cmdbuild.elements.interfaces.ObjectWithId;
import org.cmdbuild.exception.NotFoundException;
import org.cmdbuild.exception.ORMException;
import org.cmdbuild.exception.ORMException.ORMExceptionType;
import org.cmdbuild.services.auth.UserContext;
import org.cmdbuild.utils.StringUtils;

public class Lookup extends AbstractElementImpl implements ObjectWithId {

	private static final String LOOKUP_TABLE_NAME = "LookUp";

	static protected List<String> IGNOREDATTRS = Arrays.asList("User", "BeginDate", "ParentType");

	public Lookup() throws NotFoundException {
		this.schema = getLookupTable();
	}

	public Lookup(Lookup lookup) throws NotFoundException {
		this.schema = getLookupTable();
		setAttributeValueMap(lookup.getAttributeValueMap());
	}

	private ITable getLookupTable() {
		return UserContext.systemContext().tables().get(LOOKUP_TABLE_NAME);
	}

	public Integer getParentId() {
		return (Integer) getValue("ParentId");
	}

	public void setParentId(Integer id) throws ORMException {
		String parentType = this.getParentTypeName();
		Lookup parent = backend.getLookup(id);
		if ((parent != null)
				&& (parentType != null)
				&& parentType.equals(backend.getLookupType(this.getParentTypeName())
						.getType())) {
			setValue("ParentId", id);
		} else {
			throw ORMExceptionType.ORM_TYPE_ERROR.createException();
		}
	}

	public Lookup getParent() {
		return backend.getLookup(this.getParentId());
	}

	public String getParentTypeName() {
		try {
			return getLookupType().getParentTypeName();
		} catch (Exception e) {
			return null;
		}
	}

	public String getDescription() {
		return (String) getValue("Description");
	}

	public void setDescription(String description) {
		setValue("Description", description);
	}

	public String getNotes() {
		return (String) getValue("Notes");
	}

	public void setNotes(String notes) {
		setValue("Notes", notes);
	}

	public String getType() {
		return (String) getValue("Type");
	}

	public LookupType getLookupType() {
		return backend.getLookupType(getType());
	}

	public void setType(String type) throws ORMException {
		// can't change lookup type
		String actType = (String)getValue("Type");
		if (!this.isNew() && !(type.equals(actType))) {
			throw ORMExceptionType.ORM_CHANGE_LOOKUPTYPE_ERROR.createException();
		}
		// if the parent id is set, type cannot be changed
		if (this.getAttributeValueMap().containsKey("ParentId")
				&& !this.getType().equals(type)) {
			throw ORMExceptionType.ORM_TYPE_ERROR.createException();
		}
		// the type must exist
		if (null == backend.getLookupType(type))
			throw ORMExceptionType.ORM_TYPE_ERROR.createException();
		setValue("Type", type);
	}

	public int getNumber() {
		return (Integer) getValue("Number");
	}

	public void setNumber(Integer number) {
		setValue("Number", number);
	}

	public String getCode() {
		return (String) getValue("Code");
	}

	public void setCode(String code) {
		setValue("Code", code);
	}

	public boolean getIsDefault() {
		return (Boolean) getValue("IsDefault");
	}

	public void setIsDefault(Boolean isDefault) {
		setValue("IsDefault", isDefault);
	}

	public boolean equals(Object o) {
		if (o instanceof Lookup)
			return this.getId() == ((Lookup) o).getId();
		return false;
	}

	@Override
	public void setValue(String name, Object value) {
		if (!IGNOREDATTRS.contains(name)) {
			super.setValue(name, value);
		}
	}

	@Override
	public void setAttributeValueMap(Map<String, AttributeValue> values) {
		super.setAttributeValueMap(values);
		for (String attrName : IGNOREDATTRS) {
			if (this.values.containsKey(attrName))
				this.values.remove(attrName);
		}
	}

	@Override
	public Map<String, AttributeValue> getAttributeValueMap() {
		Map<String, AttributeValue> values = super.getAttributeValueMap();
		try {
			if (values.containsKey("ParentId")) {
				AttributeValue parentType = new AttributeValue(schema.getAttribute("ParentType"));
				if (!("".equals(getParentTypeName()))) {
					parentType.setValue(getParentTypeName());
					parentType.setChanged(values.get("ParentId").isChanged());
					values.put("ParentType", parentType);
				}
			}
		} catch (NotFoundException e) {
			// Should not happen, but in case values is not changed
		}
		return values;
	}

	@Override
	public void save() throws ORMException {
		setDefaultValueIfPresent(CardAttributes.ClassId.toString(), (Integer)schema.getId());
		setDefaultValueIfPresent("IsDefault", Boolean.FALSE);
		setDefaultValueIfPresent("Number", Integer.valueOf(0));
		if (values.containsKey("ParentType") && "".equals(getParentTypeName())) {
			values.remove("ParentType");
		}
		super.save();
	}

	@Override
	protected void modify() throws ORMException {
		backend.modifyLookup(this);
		resetAttributes();
	}

	@Override
	protected int create() throws ORMException {
		int id = backend.createLookup(this);
		return id;
	}

	/*
	 * Tree methods
	 */

	public String toString() {
		List<String> list = new LinkedList<String>();
		buildHierarchyList(list, this);
		return StringUtils.join(list, " - ");
	}

	private void buildHierarchyList(List<String> list, Lookup lookup) {
		String description = lookup.getDescription();
		if (description != null && description.length() > 0) {
			list.add(0, description);
		}
		Lookup parentLookup = lookup.getParent();
		if (parentLookup != null) {
			buildHierarchyList(list, parentLookup);
		}
	}
}
