package org.cmdbuild.dao.view;

import org.cmdbuild.dao.entrytype.CMDomain;
import org.cmdbuild.dao.entrytype.DBDomain;

public class UserDomain extends UserEntryType implements CMDomain {

	private final DBDomain inner;

	static UserDomain create(final UserDataView view, final DBDomain inner) {
		final CMAccessControlManager acm = view.getAccessControlManager();
		if (inner != null && acm.hasReadAccess(inner) && (inner.isActive() || acm.hasDatabaseDesignerPrivileges())) {
			return new UserDomain(view, inner);
		} else {
			return null;
		}
	}

	private UserDomain(final UserDataView view, final DBDomain inner) {
		super(view);
		this.inner = inner;
	}

	protected final DBDomain inner() {
		return inner;
	}

	@Override
	public UserClass getClass1() {
		return UserClass.create(view, inner().getClass1());
	}

	@Override
	public UserClass getClass2() {
		return UserClass.create(view, inner().getClass2());
	}

	@Override
	public String getDescription1() {
		return inner().getDescription1();
	}

	@Override
	public String getDescription2() {
		return inner().getDescription2();
	}

}
