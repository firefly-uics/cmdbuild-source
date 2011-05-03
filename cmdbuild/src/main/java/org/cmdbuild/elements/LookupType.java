package org.cmdbuild.elements;

import org.cmdbuild.dao.backend.CMBackend;
import org.cmdbuild.elements.interfaces.IAbstractElement.ElementStatus;
import org.cmdbuild.exception.ORMException;
import org.cmdbuild.exception.ORMException.ORMExceptionType;
import org.springframework.beans.factory.annotation.Autowired;

public class LookupType {

	private static final long serialVersionUID = 1L;

	@Autowired
	private CMBackend backend = CMBackend.INSTANCE;

	private String type;
	private String parentType;

	private String savedType; // meaningful only if not new and if you don't want databases in 1NF

	public LookupType(String type, String parentType) {
		this.type = type;
		this.savedType = null;
		this.parentType = parentType;
	}

	// AWFUL... waiting refactoring
	public static LookupType createFromDB(String type, String parentType) {
		LookupType lookupType = new LookupType(type, parentType);
		lookupType.savedType = type;
		return lookupType;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) throws ORMException {
		this.type = type;
	}

	// AWFUL!
	public String getSavedType() {
		return savedType;
	}

	public LookupType getParentType() {
		return backend.getLookupType(parentType);
	}

	public String getParentTypeName() {
		return parentType;
	}

	public void setParentType(String parentType) throws ORMException { 
		if (!this.isNew())
			throw ORMExceptionType.ORM_GENERIC_ERROR.createException();
		this.parentType = parentType;
	}

	public boolean isNew() {
		return (savedType == null);
	}

	@Deprecated
	public void setStatus(ElementStatus status){
		if(!status.isActive()){
			delete();
		}
	}

	public void save() throws ORMException {
		if (isNew()) {
			create();
		} else {
			modify();
		}
	}

	protected void create() throws ORMException {
		backend.createLookupType(this);
		savedType = type;
	}

	protected void modify() throws ORMException {
		backend.modifyLookupType(this);
	}

	@Deprecated
	protected void delete() throws ORMException {
		backend.deleteLookupType(this);
	}

	@Override
	public boolean equals(Object o) {
		if (o instanceof LookupType) {
			LookupType l = ((LookupType) o);
			return (l.getType().equals(this.getType()));
		}
		return false;
	}

	@Override
	public int hashCode() {
		return this.getType().hashCode();
	}

	@Override
	public String toString() {
		return this.getType();
	}

	public Lookup getLookup(final String description) {
		return backend.getLookup(type, description);
	}
}
