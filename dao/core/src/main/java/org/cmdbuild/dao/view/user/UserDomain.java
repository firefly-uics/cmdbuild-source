package org.cmdbuild.dao.view.user;

import org.cmdbuild.auth.user.OperationUser;
import org.cmdbuild.dao.entrytype.CMDomain;
import org.cmdbuild.dao.entrytype.DBDomain;

public class UserDomain extends UserEntryType implements CMDomain {

	private final DBDomain inner;

	static UserDomain newInstance(final UserDataView view, final DBDomain inner) {
		final OperationUser user = view.getOperationUser();
		if (isUserAccessible(user, inner)) {
			return new UserDomain(view, inner);
		} else {
			return null;
		}
	}

	public static boolean isUserAccessible(final OperationUser user, final DBDomain inner) {
		return inner != null && user.hasReadAccess(inner) && user.hasDatabaseDesignerPrivileges()
				&& UserClass.isUserAccessible(user, inner.getClass1())
				&& UserClass.isUserAccessible(user, inner.getClass2());
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

	@Override
	public String getCardinality() {
		return inner().getCardinality();
	}

	@Override
	public boolean isMasterDetail() {
		return inner().isMasterDetail();
	}

	@Override
	public String getMasterDetailDescription() {
		return inner().getMasterDetailDescription();
	}

	@Override
	public boolean holdsHistory() {
		return inner().holdsHistory();
	}
}
