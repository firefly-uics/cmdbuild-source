package org.cmdbuild.operation.schema;

import org.cmdbuild.dao.backend.CMBackend;
import org.cmdbuild.elements.LookupType;
import org.cmdbuild.elements.interfaces.IAttribute;
import org.cmdbuild.elements.interfaces.IAttribute.AttributeType;
import org.cmdbuild.elements.interfaces.ITable;
import org.cmdbuild.exception.ORMException;
import org.cmdbuild.exception.ORMException.ORMExceptionType;
import org.cmdbuild.services.SessionVars;
import org.cmdbuild.services.auth.UserContext;
import org.cmdbuild.utils.tree.CTree;
import org.springframework.beans.factory.annotation.Autowired;

public class LookupTypeOperation {

	private final UserContext userCtx;

	@Autowired
	private CMBackend backend = CMBackend.INSTANCE;

	public LookupTypeOperation(UserContext userCtx) {
		this.userCtx = userCtx;
	}

	public LookupTypeOperation() {
		this.userCtx = new SessionVars().getCurrentUserContext();
	}

	public LookupType getLookupType(String type) {
		LookupType lookupType = backend.getLookupType(type);
		return lookupType;
	}

	public LookupType saveLookupType(String type, String originalType, String parentType) throws ORMException {
		userCtx.privileges().assureAdminPrivilege();
		LookupType lookupType = null;
		if(originalType!= null && !(originalType.equals(""))){
			lookupType = backend.getLookupType(originalType);
			if(!(originalType.equals(type)))
				lookupType.setType(type);
		}
		if(lookupType==null){
			originalType = null;
			// forbids changing the lookup type description to an existing one
			if (backend.getLookupType(type) != null) {
				throw ORMExceptionType.ORM_LOOKUPTYPE_ALREADY_EXISTS.createException();
			}
			lookupType = new LookupType(type, parentType);
		}
		lookupType.save();
		// iterate all the attributes of all the table to upgrade the lookuptype that are changed
		if (originalType != null) {
			for (ITable table : backend.getTableList()) {
				for (IAttribute attribute : table.getAttributes().values()) {
					if (AttributeType.LOOKUP.equals(attribute.getType())) {
						if(originalType.equals(attribute.getLookupType().getType()) || type.equals(attribute.getLookupType().getType())){
							attribute.setLookupType(type);
							attribute.save();
						}
					}
				}
			}
		}
		return lookupType;
	}

	public Iterable<LookupType> getLookupTypeList() {
		return backend.getLookupTypeList();
	}

	public CTree<LookupType> getLookupTypeTree() {
		return backend.getLookupTypeTree();
	}
}
