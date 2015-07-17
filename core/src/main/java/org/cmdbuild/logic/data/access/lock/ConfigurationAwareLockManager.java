package org.cmdbuild.logic.data.access.lock;

import org.cmdbuild.config.CmdbuildConfiguration;

public class ConfigurationAwareLockManager extends ForwardingLockManager {

	private final CmdbuildConfiguration cmdbuildProperties;
	private final LockManager whenNotConfigured;
	private final LockManager whenConfigured;

	public ConfigurationAwareLockManager(final CmdbuildConfiguration cmdbuildProperties,
			final LockManager whenNotConfigured, final LockManager whenConfigured) {
		this.cmdbuildProperties = cmdbuildProperties;
		this.whenNotConfigured = whenNotConfigured;
		this.whenConfigured = whenConfigured;
	}

	@Override
	protected LockManager delegate() {
		return cmdbuildProperties.getLockCard() ? whenConfigured : whenNotConfigured;
	}

}
