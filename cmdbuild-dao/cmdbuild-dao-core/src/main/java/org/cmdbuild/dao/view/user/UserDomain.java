package org.cmdbuild.dao.view.user;

import org.cmdbuild.dao.entrytype.CMDomain;
import org.cmdbuild.dao.entrytype.DBDomain;
import org.cmdbuild.auth.CMAccessControlManager;

public class UserDomain extends UserEntryType implements CMDomain {

	private final DBDomain inner;

	static UserDomain create(final UserDataView view, final DBDomain inner) {
		if (isUserAccessible(view.getAccessControlManager(), inner)) {
			return new UserDomain(view, inner);
		} else {
			return null;
		}
	}

	public static boolean isUserAccessible(final CMAccessControlManager acm, final DBDomain inner) {
		return inner != null && acm.hasReadAccess(inner)
				&& (inner.isActive() || acm.hasDatabaseDesignerPrivileges())
				&& UserClass.isUserAccessible(acm, inner.getClass1())
				&& UserClass.isUserAccessible(acm, inner.getClass2());
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
