package org.cmdbuild.auth.acl;

import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang.Validate;
import org.cmdbuild.common.Builder;

public class GroupImpl implements CMGroup {

	public static class GroupImplBuilder implements Builder<GroupImpl> {

		private String name;
		private String description;
		private List<PrivilegePair> privileges;

		private GroupImplBuilder() {
			privileges = new ArrayList<PrivilegePair>();
		}

		public GroupImplBuilder withName(final String name) {
			this.name = name;
			return this;
		}

		public GroupImplBuilder withDescription(final String description) {
			this.description = description;
			return this;
		}

		public GroupImplBuilder withPrivileges(final List<PrivilegePair> privileges) {
			this.privileges.addAll(privileges);
			return this;
		}

		public GroupImplBuilder withPrivilege(final PrivilegePair privilege) {
			this.privileges.add(privilege);
			return this;
		}

		@Override
		public GroupImpl build() {
			Validate.notNull(name);
			Validate.notNull(privileges);
			if (description == null) {
				description = name;
			}
			return new GroupImpl(this);
		}
	}

	private final String name;
	private final String description;
	private final List<PrivilegePair> privileges;

	private GroupImpl(final GroupImplBuilder builder) {
		this.name = builder.name;
		this.description = builder.description;
		this.privileges = builder.privileges;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public String getDescription() {
		return description;
	}

	@Override
	public List<PrivilegePair> getAllPrivileges() {
		return privileges;
	}

	public static GroupImplBuilder newInstanceBuilder() {
		return new GroupImplBuilder();
	}

	@Override
	public boolean equals(final Object obj) {
		if (obj == null) {
			return false;
		}
		if (!CMGroup.class.isAssignableFrom(obj.getClass())) {
			return false;
		}
		final CMGroup other = CMGroup.class.cast(obj);
		return name.equals(other.getName());
	}

	@Override
	public int hashCode() {
		return name.hashCode();
	}

	@Override
	public String toString() {
		return String.format("[Group %s]", this.getName());
	}
}
