package org.cmdbuild.dao.view.user;

import org.cmdbuild.auth.user.OperationUser;
import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.dao.entrytype.DBClass;

public class UserClass extends UserEntryType implements CMClass {

	private final DBClass inner;

	static UserClass newInstance(final UserDataView view, final DBClass inner) {
		final OperationUser user = view.getOperationUser();
		if (isUserAccessible(user, inner)) {
			return new UserClass(view, inner);
		} else {
			return null;
		}
	}

	public static boolean isUserAccessible(final OperationUser user, final DBClass inner) {
		return inner != null && //
				!inner.isSystem() && //
				(user.hasReadAccess(inner) || inner.isBaseClass() || //
				user.hasDatabaseDesignerPrivileges());
	}

	private UserClass(final UserDataView view, final DBClass inner) {
		super(view);
		this.inner = inner;
	}

	@Override
	protected final DBClass inner() {
		return inner;
	}

	@Override
	public UserClass getParent() {
		return UserClass.newInstance(view, inner().getParent());
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
	public boolean isAncestorOf(final CMClass cmClass) {
		// Is there any way to protect this?
		return inner.isAncestorOf(cmClass);
	}

	@Override
	public boolean isSuperclass() {
		return inner().isSuperclass();
	}

	@Override
	public boolean holdsHistory() {
		return inner().holdsHistory();
	}

	@Override
	public String getCodeAttributeName() {
		return inner.getCodeAttributeName();
	}

	@Override
	public String getDescriptionAttributeName() {
		return inner.getDescriptionAttributeName();
	}

}
