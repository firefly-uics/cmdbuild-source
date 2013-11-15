package org.cmdbuild.auth.user;

import java.util.List;
import java.util.Set;

public class ForwardingUser implements CMUser {

	private final CMUser inner;

	public ForwardingUser(final CMUser user) {
		this.inner = user;
	}

	@Override
	public Long getId() {
		return inner.getId();
	}

	@Override
	public String getUsername() {
		return inner.getUsername();
	}

	@Override
	public String getDescription() {
		return inner.getDescription();
	}

	@Override
	public Set<String> getGroupNames() {
		return inner.getGroupNames();
	}

	@Override
	public List<String> getGroupDescriptions() {
		return inner.getGroupDescriptions();
	}

	@Override
	public String getDefaultGroupName() {
		return inner.getDefaultGroupName();
	}

	@Override
	public String getEmail() {
		return inner.getEmail();
	}

	@Override
	public boolean isActive() {
		return inner.isActive();
	}

}
