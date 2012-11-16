package org.cmdbuild.auth.user;

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang.Validate;
import org.cmdbuild.auth.acl.CMGroup;
import org.cmdbuild.common.Builder;

public class UserImpl implements CMUser {

	public static class UserImplBuilder implements Builder<UserImpl> {

		private Long id;
		private String name;
		private String description;
		private final Set<CMGroup> groups;

		private UserImplBuilder() {
			this.groups = new HashSet<CMGroup>();
		}

		public UserImplBuilder withId(final Long id) {
			this.id = id;
			return this;
		}

		public UserImplBuilder withName(final String name) {
			this.name = name;
			return this;
		}

		public UserImplBuilder withDescription(final String description) {
			this.description = description;
			return this;
		}

		public UserImplBuilder withGroup(final CMGroup group) {
			this.groups.add(group);
			return this;
		}

		public UserImplBuilder withGroups(final Set<CMGroup> groups) {
			this.groups.addAll(groups);
			return this;
		}

		@Override
		public UserImpl build() {
			Validate.notNull(name);
			Validate.notNull(description);
			Validate.noNullElements(groups);
			return new UserImpl(this);
		}
	}

	private final Long id;
	private final String name;
	private final String description;
	private final Set<CMGroup> groups;

	private UserImpl(final UserImplBuilder builder) {
		this.id = builder.id;
		this.name = builder.name;
		this.description = builder.description;
		this.groups = builder.groups;
	}

	@Override
	public Long getId() {
		return id;
	}

	@Override
	public String getName() {
		return this.name;
	}

	@Override
	public String getDescription() {
		return this.description;
	}

	@Override
	public Set<CMGroup> getGroups() {
		return this.groups;
	}

	@Override
	public String getDefaultGroupName() {
		// TODO: see also the correspondent method of the AuthenticationFacade
		// class
		return null;
	}

	public static UserImplBuilder newInstanceBuilder() {
		return new UserImplBuilder();
	}

	@Override
	public boolean equals(final Object obj) {
		if (obj == null) {
			return false;
		}
		if (!CMUser.class.isAssignableFrom(obj.getClass())) {
			return false;
		}
		final CMUser other = CMUser.class.cast(obj);
		return name.equals(other.getName());
	}

	@Override
	public int hashCode() {
		return name.hashCode();
	}

}
