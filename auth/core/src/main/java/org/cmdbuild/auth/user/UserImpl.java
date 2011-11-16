package org.cmdbuild.auth.user;

import java.util.HashSet;
import java.util.Set;
import org.apache.commons.lang.Validate;
import org.cmdbuild.auth.acl.CMGroup;
import org.cmdbuild.common.Builder;

public class UserImpl implements CMUser {

	public static class UserImplBuilder implements Builder<UserImpl> {

		private String name;
		private Set<CMGroup> groups;

		private UserImplBuilder() {
			this.groups = new HashSet<CMGroup>();
		}

		public UserImplBuilder withName(final String name) {
			this.name = name;
			return this;
		}

		public UserImplBuilder withGroup(CMGroup group) {
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
			Validate.noNullElements(groups);
			return new UserImpl(this);
		}
	}

	private final String name;
	private final Set<CMGroup> groups;

	private UserImpl(final UserImplBuilder builder) {
		this.name = builder.name;
		this.groups = builder.groups;
	}

	@Override
	public String getName() {
		return this.name;
	}

	@Override
	public Set<CMGroup> getGroups() {
		return this.groups;
	}

	public static UserImplBuilder newInstanceBuilder() {
		return new UserImplBuilder();
	}
}
