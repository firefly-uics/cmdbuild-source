package org.cmdbuild.dao.view.user;

import org.cmdbuild.auth.user.OperationUser;
import org.cmdbuild.dao.entrytype.CMDomain;
import org.cmdbuild.dao.entrytype.DBDomain;

public class UserDomain extends UserEntryType implements CMDomain {

	private final DBDomain inner;

	static UserDomain newInstance(final UserDataView view, final DBDomain inner) {
		final OperationUser user = view.getOperationUser();
		if (inner != null && user.hasReadAccess(inner) && (inner.isActive() || user.hasDatabaseDesignerPrivileges())) {
			return new UserDomain(view, inner);
		} else {
			return null;
		}
	}

	private UserDomain(final UserDataView view, final DBDomain inner) {
		super(view);
		this.inner = inner;
	}

	@Override
	protected final DBDomain inner() {
		return inner;
	}

	@Override
	public UserClass getClass1() {
		return UserClass.newInstance(view, inner().getClass1());
	}

	@Override
	public UserClass getClass2() {
		return UserClass.newInstance(view, inner().getClass2());
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
