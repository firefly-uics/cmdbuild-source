package org.cmdbuild.dao.view.user;

import org.cmdbuild.auth.acl.PrivilegeContext;
import org.cmdbuild.dao.entrytype.CMClass;

public class UserClass extends UserEntryType implements CMClass {

	private final CMClass inner;

	static UserClass newInstance(final UserDataView view, final CMClass inner) {
		final PrivilegeContext privilegeContext = view.getPrivilegeContext();
		if (isUserAccessible(privilegeContext, inner)) {
			return new UserClass(view, inner);
		} else {
			return null;
		}
	}

	public static boolean isUserAccessible(final PrivilegeContext privilegeContext, final CMClass inner) {
		if (inner == null) {
			return false;
		}

		if (inner.isSystem() && !inner.isSystemButUsable()) {
			return false;
		}

		// TODO remove when system-but-usable privileges will be managed
		if (inner.isSystemButUsable()) {
			return true;
		}

		return (privilegeContext.hasReadAccess(inner) || inner.isBaseClass() || privilegeContext
				.hasDatabaseDesignerPrivileges());
	}

	private UserClass(final UserDataView view, final CMClass inner) {
		super(view);
		this.inner = inner;
	}

	@Override
	protected final CMClass inner() {
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
	public Iterable<UserClass> getDescendants() {
		return view.proxyClasses(inner().getDescendants());
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

	@Override
	public boolean isUserStoppable() {
		return inner.isUserStoppable();
	}

}
