package org.cmdbuild.privileges.fetchers;

import static org.cmdbuild.auth.privileges.constants.GrantConstants.MODE_ATTRIBUTE;
import static org.cmdbuild.auth.privileges.constants.GrantConstants.PRIVILEGED_OBJECT_ID_ATTRIBUTE;

import org.cmdbuild.auth.acl.CMPrivilege;
import org.cmdbuild.auth.acl.DefaultPrivileges;
import org.cmdbuild.auth.acl.SerializablePrivelege;
import org.cmdbuild.auth.privileges.constants.PrivilegeMode;
import org.cmdbuild.auth.privileges.constants.PrivilegedObjectType;
import org.cmdbuild.auth.user.OperationUser;
import org.cmdbuild.dao.entry.CMCard;
import org.cmdbuild.dao.view.DBDataView;
import org.cmdbuild.logic.TemporaryObjectsBeforeSpringDI;
import org.cmdbuild.services.store.DataViewFilterStore;
import org.cmdbuild.services.store.FilterStore.Filter;

public class FilterPrivilegeFetcher extends AbstractPrivilegeFetcher {

	private final DBDataView view;

	public FilterPrivilegeFetcher(final DBDataView view, final Long groupId) {
		super(view, groupId);
		this.view = view;
	}

	@Override
	protected PrivilegedObjectType getPrivilegedObjectType() {
		return PrivilegedObjectType.FILTER;
	}

	@Override
	protected SerializablePrivelege extractPrivilegedObject(final CMCard privilegeCard) {
		final Integer filterId = (Integer) privilegeCard.get(PRIVILEGED_OBJECT_ID_ATTRIBUTE);
		final OperationUser operationUser = TemporaryObjectsBeforeSpringDI.getOperationUser();
		final DataViewFilterStore filterStore = new DataViewFilterStore(view, operationUser);
		final Filter privilegedFilter = filterStore.fetchFilter(filterId.longValue());
		return privilegedFilter;
	}

	@Override
	protected CMPrivilege extractPrivilegeType(final CMCard privilegeCard) {
		final Object type = privilegeCard.get(MODE_ATTRIBUTE);
		if (PrivilegeMode.READ.getValue().equals(type)) {
			return DefaultPrivileges.READ;
		} else if (PrivilegeMode.WRITE.getValue().equals(type)) {
			return DefaultPrivileges.WRITE;
		}
		return null;
	}
}
