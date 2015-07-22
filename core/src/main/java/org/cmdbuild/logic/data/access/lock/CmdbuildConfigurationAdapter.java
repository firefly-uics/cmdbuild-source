package org.cmdbuild.logic.data.access.lock;

import org.cmdbuild.config.CmdbuildConfiguration;

public class CmdbuildConfigurationAdapter implements InMemoryLockableStore.Configuration {

	private final CmdbuildConfiguration delegate;

	public CmdbuildConfigurationAdapter(final CmdbuildConfiguration delegate) {
		this.delegate = delegate;
	}

	@Override
	public long getExpirationTimeInMilliseconds() {
		return delegate.getLockCardTimeOut() * 1000; // To have milliseconds
	}

}
