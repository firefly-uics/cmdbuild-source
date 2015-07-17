package org.cmdbuild.logic.data.access.lock;

import org.cmdbuild.config.CmdbuildConfiguration;
import org.cmdbuild.logic.data.access.lock.InMemoryLockManager.Configuration;

public class CmdbuildConfigurationAdapter implements Configuration {

	private final CmdbuildConfiguration delegate;

	public CmdbuildConfigurationAdapter(final CmdbuildConfiguration delegate) {
		this.delegate = delegate;
	}

	@Override
	public boolean isUsernameVisible() {
		return delegate.getLockCardUserVisible();
	}

	@Override
	public long getExpirationTimeInMilliseconds() {
		return delegate.getLockCardTimeOut() * 1000; // To have milliseconds
	}

}
