package org.cmdbuild.workflow;

import org.apache.commons.lang.Validate;

public class ActivityPerformer {

	public enum Type {
		ROLE, EXPRESSION, ADMIN // fake performer for our ugly stuff
	}

	private final String value;
	private final Type type;

	public ActivityPerformer(final Type type, final String name) {
		this.value = name;
		this.type = type;
	}

	public Type getType() {
		return type;
	}

	public String getValue() {
		return value;
	}

	public boolean isRole(final String roleName) {
		return (type == Type.ROLE) && (value.equals(roleName));
	}

	public boolean isAdmin() {
		return (type == Type.ADMIN);
	}

	public static ActivityPerformer newRolePerformer(final String roleName) {
		Validate.notNull(roleName);
		return new ActivityPerformer(Type.ROLE, roleName);
	}

	public static ActivityPerformer newExpressionPerformer(final String expression) {
		Validate.notNull(expression);
		return new ActivityPerformer(Type.EXPRESSION, expression);
	}

	public static ActivityPerformer newAdminPerformer() {
		return new ActivityPerformer(Type.ADMIN, null);
	}
}
