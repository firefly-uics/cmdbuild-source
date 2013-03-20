package org.cmdbuild.privileges.fetchers;

import static org.cmdbuild.auth.privileges.constants.GrantConstants.MODE_ATTRIBUTE;
import static org.cmdbuild.auth.privileges.constants.GrantConstants.PRIVILEGED_OBJECT_ID_ATTRIBUTE;

import org.cmdbuild.auth.acl.CMPrivilege;
import org.cmdbuild.auth.acl.DefaultPrivileges;
import org.cmdbuild.auth.acl.SerializablePrivilege;
import org.cmdbuild.auth.privileges.constants.PrivilegeMode;
import org.cmdbuild.auth.privileges.constants.PrivilegedObjectType;
import org.cmdbuild.dao.entry.CMCard;
import org.cmdbuild.dao.view.DBDataView;
import org.cmdbuild.data.converter.ViewConverter;
import org.cmdbuild.model.View;
import org.cmdbuild.services.store.DataViewStore;
import org.cmdbuild.services.store.DataViewStore.StorableConverter;
import org.cmdbuild.services.store.Store.Storable;

public class ViewPrivilegeFetcher extends AbstractPrivilegeFetcher {

	private final DBDataView view;

	public ViewPrivilegeFetcher(final DBDataView view, final Long groupId) {
		super(view, groupId);
		this.view = view;
	}

	@Override
	protected PrivilegedObjectType getPrivilegedObjectType() {
		return PrivilegedObjectType.VIEW;
	}

	@Override
	protected SerializablePrivilege extractPrivilegedObject(final CMCard privilegeCard) {
		final Integer viewId = (Integer) privilegeCard.get(PRIVILEGED_OBJECT_ID_ATTRIBUTE);
		final StorableConverter<View> converter = new ViewConverter();
		final DataViewStore<View> viewStore = new DataViewStore<View>(view, converter);
		return viewStore.read(getFakeViewWithId(viewId));
	}

	private Storable getFakeViewWithId(final Integer viewId) {
		return new Storable() {
			@Override
			public String getIdentifier() {
				return viewId.toString();
			}
		};
	}

	@Override
	protected CMPrivilege extractPrivilegeMode(final CMCard privilegeCard) {
		final Object type = privilegeCard.get(MODE_ATTRIBUTE);
		if (PrivilegeMode.READ.getValue().equals(type)) {
			return DefaultPrivileges.READ;
		} else if (PrivilegeMode.WRITE.getValue().equals(type)) {
			return DefaultPrivileges.WRITE;
		}
		return null;
	}

}
