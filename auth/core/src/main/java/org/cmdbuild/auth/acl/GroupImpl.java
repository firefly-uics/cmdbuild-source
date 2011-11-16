package org.cmdbuild.auth.acl;

import org.apache.commons.lang.Validate;
import org.cmdbuild.common.Builder;

public class GroupImpl implements CMGroup {

	public static class GroupImplBuilder implements Builder<GroupImpl> {

		private String name;
		private String description;
		private CMSecurityManager sm;

		private GroupImplBuilder() {
		}

		public GroupImplBuilder withName(final String name) {
			this.name = name;
			return this;
		}

		public GroupImplBuilder withDescription(final String description) {
			this.description = description;
			return this;
		}

		public GroupImplBuilder withSecurityManager(final CMSecurityManager sm) {
			this.sm = sm;
			return this;
		}

		@Override
		public GroupImpl build() {
			Validate.notNull(name);
			Validate.notNull(description);
			Validate.notNull(sm);
			return new GroupImpl(this);
		}
	}

	private final String name;
	private final String description;
	private final CMSecurityManager sm;

	private GroupImpl(final GroupImplBuilder builder) {
		this.name = builder.name;
		this.description = builder.description;
		this.sm = builder.sm;
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
	public CMSecurityManager getSecurityManager() {
		return this.sm;
	}

	public static GroupImplBuilder newInstanceBuilder() {
		return new GroupImplBuilder();
	}
}
