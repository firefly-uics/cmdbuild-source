package org.cmdbuild.logic.data.access.lock;

import org.cmdbuild.config.CmdbuildProperties;
import org.cmdbuild.logic.data.access.lock.LockCardManager.LockCardConfiguration;

public class DefaultLockCardConfiguration implements LockCardConfiguration {

	private final CmdbuildProperties properties;

	public DefaultLockCardConfiguration(final CmdbuildProperties properties) {
		this.properties = properties;
	}

	@Override
	public boolean isLockerUsernameVisible() {
		return properties.getLockCardUserVisible();
	}

	@Override
	public long getExpirationTimeInMilliseconds() {
		return properties.getLockCardTimeOut() * 1000; // To have milliseconds
	}

}
