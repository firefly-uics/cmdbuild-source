package org.cmdbuild.dao.view.user;

import java.util.Map;

import org.cmdbuild.dao.entry.ForwardingAttribute;
import org.cmdbuild.dao.entrytype.CMAttribute;

public class UserAttribute extends ForwardingAttribute {

	private final static String NO_PRIVILEGE = "none";
	final UserDataView view;
	final String mode;

	static UserAttribute newInstance( //
			final UserDataView view, //
			final CMAttribute inner //
	) {

		if (inner == null) {
			return null;
		}

		/*
		 * For non administrator user, remove the attribute with mode "hidden"
		 */
		final boolean isAdmin = view.getPrivilegeContext().hasAdministratorPrivileges();
		final String mode = getAttributeMode(view, inner);
		if (isAdmin || !NO_PRIVILEGE.equals(mode)) {
			return new UserAttribute(view, inner, mode);
		}

		return null;
	}

	private static String getAttributeMode(final UserDataView view, final CMAttribute inner) {
		final Map<String, String> attributesPrivileges = view.getAttributesPrivilegesFor(inner.getOwner());
		return attributesPrivileges.get(inner.getName());
	}

	private UserAttribute( //
			final UserDataView view, //
			final CMAttribute inner, //
			final String mode //
	) {
		super(inner);
		this.view = view;
		this.mode = mode;
	}

	@Override
	public UserEntryType getOwner() {
		return view.proxy(super.getOwner());
	}

	@Override
	public Mode getMode() {
		if (mode != null) {
			return Mode.valueOf(mode.toUpperCase());
		} else {
			return super.getMode();
		}
	}

}
