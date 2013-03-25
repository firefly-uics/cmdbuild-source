package org.cmdbuild.privileges.fetchers;

import static org.cmdbuild.auth.privileges.constants.GrantConstants.MODE_ATTRIBUTE;
import static org.cmdbuild.auth.privileges.constants.GrantConstants.PRIVILEGED_CLASS_ID_ATTRIBUTE;

import org.cmdbuild.auth.acl.CMPrivilege;
import org.cmdbuild.auth.acl.DefaultPrivileges;
import org.cmdbuild.auth.acl.SerializablePrivilege;
import org.cmdbuild.auth.privileges.constants.PrivilegeMode;
import org.cmdbuild.auth.privileges.constants.PrivilegedObjectType;
import org.cmdbuild.dao.entry.CMCard;
import org.cmdbuild.dao.reference.EntryTypeReference;
import org.cmdbuild.dao.view.DBDataView;

public class CMClassPrivilegeFetcher extends AbstractPrivilegeFetcher {

	private final DBDataView view;

	public CMClassPrivilegeFetcher(final DBDataView view, final Long groupId) {
		super(view, groupId);
		this.view = view;
	}

	@Override
	protected PrivilegedObjectType getPrivilegedObjectType() {
		return PrivilegedObjectType.CLASS;
	}

	@Override
	protected SerializablePrivilege extractPrivilegedObject(final CMCard privilegeCard) {
		final EntryTypeReference etr = (EntryTypeReference) privilegeCard.get(PRIVILEGED_CLASS_ID_ATTRIBUTE);
		return view.findClass(etr.getId());
	}

	@Override
	protected CMPrivilege extractPrivilegeMode(final CMCard privilegeCard) {
		final Object type = privilegeCard.get(MODE_ATTRIBUTE);
		if (PrivilegeMode.READ.getValue().equals(type)) {
			return DefaultPrivileges.READ;
		} else if (PrivilegeMode.WRITE.getValue().equals(type)) {
			return DefaultPrivileges.WRITE;
		} else {
			return DefaultPrivileges.NONE;
		}
	}
}
