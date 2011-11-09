package org.cmdbuild.dao.view.user;

import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.dao.entrytype.DBClass;
import org.cmdbuild.auth.CMAccessControlManager;

public class UserClass extends UserEntryType implements CMClass {

	private final DBClass inner;

	static UserClass create(final UserDataView view, final DBClass inner) {
		final CMAccessControlManager acm = view.getAccessControlManager();
		if (inner != null && acm.hasReadAccess(inner) && (inner.isActive() || acm.hasDatabaseDesignerPrivileges())) {
			return new UserClass(view, inner);
		} else {
			return null;
		}
	}

	private UserClass(final UserDataView view, final DBClass inner) {
		super(view);
		this.inner = inner;
	}

	protected final DBClass inner() {
		return inner;
	}

	@Override
	public UserClass getParent() {
		return UserClass.create(view, inner().getParent());
	}

	@Override
	public Iterable<UserClass> getChildren() {
		return view.proxyClasses(inner().getChildren());
	}

	@Override
	public Iterable<UserClass> getLeaves() {
		return view.proxyClasses(inner().getLeaves());
	}

	@Override
	public boolean isAncestorOf(CMClass cmClass) {
		// Is there any way to protect this?
		return inner.isAncestorOf(cmClass);
	}

	@Override
	public boolean isSuperclass() {
		return inner().isSuperclass();
	}

}
