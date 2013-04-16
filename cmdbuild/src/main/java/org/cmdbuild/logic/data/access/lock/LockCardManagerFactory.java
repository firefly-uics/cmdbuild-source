package org.cmdbuild.logic.data.access.lock;

import org.cmdbuild.config.CmdbuildProperties;

public class LockCardManagerFactory {

	private CmdbuildProperties cmdbuildProperties;
	private EmptyLockCard emptyLockCard;
	private InMemoryLockCard inMemoryLockCard;

	public void setCmdbuildProperties(final CmdbuildProperties cmdbuildProperties) {
		this.cmdbuildProperties = cmdbuildProperties;
	}

	public void setEmptyLockCard(final EmptyLockCard emptyLockCard) {
		this.emptyLockCard = emptyLockCard;
	}

	public void setInMemoryLockCard(final InMemoryLockCard inMemoryLockCard) {
		this.inMemoryLockCard = inMemoryLockCard;
	}

	public LockCardManager create() {
		LockCardManager lockCardManager;
		if (cmdbuildProperties.getLockCard()) {
			lockCardManager = inMemoryLockCard;
		} else {
			lockCardManager = emptyLockCard;
		}
		return lockCardManager;
	}

}
