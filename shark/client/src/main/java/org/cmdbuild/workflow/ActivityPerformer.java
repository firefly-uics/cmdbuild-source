package org.cmdbuild.workflow;

import org.apache.commons.lang.Validate;

public class ActivityPerformer {

	private enum Type {
		ROLE, ADMIN // fake performer for our ugly stuff
	}

	private final String name;
	private final Type type;

	public ActivityPerformer(final Type type, final String name) {
		this.name = name;
		this.type = type;
	}

	public String getName() {
		return name;
	}

	public boolean isRole(final String roleName) {
		return (type == Type.ROLE) && (name.equals(roleName));
	}

	public boolean isAdmin() {
		return (type == Type.ADMIN);
	}

	public static ActivityPerformer newRolePerformer(final String roleName) {
		Validate.notNull(roleName);
		return new ActivityPerformer(Type.ROLE, roleName);
	}

	public static ActivityPerformer newAdminPerformer() {
		return new ActivityPerformer(Type.ADMIN, null);
	}
}
