package org.cmdbuild.services.auth;

import java.util.HashMap;
import java.util.Map;

import org.cmdbuild.elements.interfaces.BaseSchema;
import org.cmdbuild.elements.interfaces.IAttribute;
import org.cmdbuild.elements.interfaces.IDomain;
import org.cmdbuild.elements.interfaces.ITable;
import org.cmdbuild.elements.wrappers.PrivilegeCard;
import org.cmdbuild.elements.wrappers.PrivilegeCard.PrivilegeType;
import org.cmdbuild.exception.NotFoundException;
import org.cmdbuild.exception.AuthException.AuthExceptionType;
import org.cmdbuild.exception.NotFoundException.NotFoundExceptionType;

public class PrivilegeManager {

	private final Map<Integer, PrivilegeType> classPrivileges = new HashMap<Integer, PrivilegeType>();
	private boolean isAdmin = false;

	PrivilegeManager() {
	}

	PrivilegeManager(Group g) {
		if (g.isAdmin()) {
			isAdmin = true;
		} else {
			addGroupPrivileges(g);
		}
	}

	void addGroupPrivileges(Group g) {
		if (g != null) {
			if (g.isAdmin()) {
				isAdmin = true;
			} else {
				safeAddGroupPrivileges(g);
			}
		}
	}

	private void safeAddGroupPrivileges(Group g) {
		for (PrivilegeCard p : PrivilegeCard.forGroup(g.getId())) {
			Integer classId = p.getGrantedClassId();
			PrivilegeType privilege = p.getMode();
			PrivilegeType oldPrivilege = classPrivileges.get(classId);
			if (oldPrivilege != null) {
				privilege = PrivilegeType.union(oldPrivilege, privilege);
			}
			classPrivileges.put(classId, privilege);
		}
	}

	public boolean isAdmin() {
		return isAdmin;
	}

	public void assureAdminPrivilege() {
		if (!isAdmin())
			throw AuthExceptionType.AUTH_NOT_AUTHORIZED.createException();
	}

	/*
	 * Table privilege check
	 */

	public PrivilegeType getPrivilege(ITable table) throws NotFoundException {
		PrivilegeType privilege;
		if (isAdmin()) {
			privilege = PrivilegeType.WRITE;
		} else if (table == null) { // Guess what? Map!
			privilege = PrivilegeType.NONE;
		} else {
			privilege = classPrivileges.get(table.getId());
			if (privilege == null) {
				if (table.getMode().alwaysReadPrivileges()) {
					privilege = PrivilegeType.READ;
				} else {
					privilege = PrivilegeType.NONE;
				}
			}
		}
		return privilege;
	}

	public void assureReadPrivilege(ITable table) {
		if (!hasReadPrivilege(table))
			throw AuthExceptionType.AUTH_CLASS_NOT_AUTHORIZED.createException(table.getName());
	}

	public boolean hasReadPrivilege(ITable table) {
		return isAdmin() || getPrivilege(table) != PrivilegeType.NONE;
	}

	public void assureWritePrivilege(ITable table) {
		if (!hasWritePrivilege(table))
			throw AuthExceptionType.AUTH_CLASS_NOT_AUTHORIZED.createException(table.getName());
	}

	public boolean hasWritePrivilege(ITable table) {
		return isAdmin() || getPrivilege(table) == PrivilegeType.WRITE;
	}

	public void assureCreatePrivilege(ITable table) {
		if (!hasCreatePrivilege(table))
			throw AuthExceptionType.AUTH_CLASS_NOT_AUTHORIZED.createException(table.getName());
	}

	public boolean hasCreatePrivilege(ITable table) {
		return table.getMode().cardsAllowed() && hasWritePrivilege(table);
	}

	/*
	 * Domain privilege check
	 */

	public PrivilegeType getPrivilege(IDomain domain) throws NotFoundException {
		return PrivilegeType.intersection(getPrivilege(domain.getClass1()), getPrivilege(domain.getClass2()));
	}

	public void assureReadPrivilege(IDomain domain) {
		if (!hasReadPrivilege(domain))
			throw AuthExceptionType.AUTH_DOMAIN_NOT_AUTHORIZED.createException(domain.getName());
	}

	public boolean hasReadPrivilege(IDomain domain) {
		return isAdmin() || (getPrivilege(domain) != PrivilegeType.NONE);
	}

	public void assureWritePrivilege(IDomain domain) {
		if (!hasWritePrivilege(domain))
			throw AuthExceptionType.AUTH_DOMAIN_NOT_AUTHORIZED.createException(domain.getName());
	}

	public boolean hasWritePrivilege(IDomain domain) {
		return isAdmin() || (getPrivilege(domain) == PrivilegeType.WRITE);
	}

	public void assureCreatePrivilege(IDomain domain) {
		if (!hasCreatePrivilege(domain))
			throw AuthExceptionType.AUTH_DOMAIN_NOT_AUTHORIZED.createException(domain.getName());
	}

	public boolean hasCreatePrivilege(IDomain domain) {
		return hasWritePrivilege(domain);
	}

	/*
	 * Base Schema privilege check
	 */

	public boolean hasReadPrivilege(BaseSchema schema) {
		return isAdmin() || getPrivilege(schema) != PrivilegeType.NONE;
	}

	public PrivilegeType getPrivilege(BaseSchema schema) throws NotFoundException {
		if (schema instanceof ITable) {
			return getPrivilege((ITable) schema);
		} else if (schema instanceof IDomain) {
			return getPrivilege((IDomain) schema);
		} else if (schema instanceof IAttribute) {
			IAttribute attribute = (IAttribute) schema;
			BaseSchema attributeSchema = attribute.getSchema();
			ITable fkTargetClass = attribute.getFKTargetClass();
			if (attributeSchema.getMode().isCustom() || fkTargetClass == null) {
				return getPrivilege(attributeSchema);
			} else {
				return getPrivilege(fkTargetClass);
			}
		} else {
			throw NotFoundExceptionType.PRIVILEGE_NOTFOUND.createException();
		}
	}
}
