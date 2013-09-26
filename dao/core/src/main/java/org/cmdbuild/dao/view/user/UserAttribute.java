package org.cmdbuild.dao.view.user;

import java.util.Map;

import org.cmdbuild.dao.entrytype.CMAttribute;
import org.cmdbuild.dao.entrytype.attributetype.CMAttributeType;

public class UserAttribute implements CMAttribute {

	private final UserDataView view;
	private final CMAttribute inner;
	private final String mode;

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
		if (isAdmin || !"hidden".equals(mode)) {
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

		this.view = view;
		this.inner = inner;
		this.mode = mode;
	}

	@Override
	public UserEntryType getOwner() {
		return view.proxy(inner.getOwner());
	}

	@Override
	public CMAttributeType<?> getType() {
		return inner.getType();
	}

	@Override
	public String getName() {
		return inner.getName();
	}

	@Override
	public String getDescription() {
		return inner.getDescription();
	}

	@Override
	public boolean isSystem() {
		return inner.isSystem();
	}

	@Override
	public boolean isInherited() {
		return inner.isInherited();
	}

	@Override
	public boolean isActive() {
		return inner.isActive();
	}

	@Override
	public boolean isDisplayableInList() {
		return inner.isDisplayableInList();
	}

	@Override
	public boolean isMandatory() {
		return inner.isMandatory();
	}

	@Override
	public boolean isUnique() {
		return inner.isUnique();
	}

	@Override
	public Mode getMode() {
		if (mode != null) {
			return Mode.valueOf(mode.toUpperCase());
		} else {
			return inner.getMode();
		}
	}

	@Override
	public int getIndex() {
		return inner.getIndex();
	}

	@Override
	public String getDefaultValue() {
		return inner.getDefaultValue();
	}

	@Override
	public String getGroup() {
		return inner.getGroup();
	}

	@Override
	public int getClassOrder() {
		return inner.getClassOrder();
	}

	@Override
	public String getEditorType() {
		return inner.getEditorType();
	}

	@Override
	public String getFilter() {
		return inner.getFilter();
	}

	@Override
	public String getForeignKeyDestinationClassName() {
		return inner.getForeignKeyDestinationClassName();
	}

	/*
	 * Object overrides
	 */

	@Override
	public int hashCode() {
		return inner.hashCode();
	}

	@Override
	public boolean equals(final Object obj) {
		return inner.equals(obj);
	}

	@Override
	public String toString() {
		// TODO Add username
		return inner.toString();
	}

}
