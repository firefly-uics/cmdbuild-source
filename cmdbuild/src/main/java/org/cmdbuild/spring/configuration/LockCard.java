package org.cmdbuild.spring.configuration;

import org.cmdbuild.auth.UserStore;
import org.cmdbuild.config.CmdbuildConfiguration;
import org.cmdbuild.logic.data.access.lock.EmptyLockCard;
import org.cmdbuild.logic.data.access.lock.InMemoryLockCard;
import org.cmdbuild.logic.data.access.lock.LockCardManager;
import org.cmdbuild.logic.data.access.lock.LockCardManager.LockCardConfiguration;
import org.cmdbuild.logic.data.access.lock.LockCardManagerFactory;
import org.cmdbuild.services.store.LockedCardStore;
import org.cmdbuild.spring.annotations.ConfigurationComponent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Scope;

@ConfigurationComponent
public class LockCard {

	@Autowired
	private CmdbuildConfiguration cmdbuildProperties;

	@Autowired
	private LockCardConfiguration lockCardConfiguration;

	@Autowired
	private UserStore userStore;

	@Bean
	@Qualifier("system")
	public EmptyLockCard emptyLockCardManager() {
		return new EmptyLockCard();
	}

	@Bean
	protected LockedCardStore lockedCardStore() {
		return new LockedCardStore(lockCardConfiguration);
	}

	@Bean
	@Scope("prototype")
	public InMemoryLockCard memoryLockCardManager() {
		return new InMemoryLockCard(lockCardConfiguration, userStore.getUser(), lockedCardStore());
	}

	@Bean
	@Scope("prototype")
	@Qualifier("user")
	public LockCardManager userLockCardManager() {
		final LockCardManagerFactory factory = new LockCardManagerFactory();
		factory.setCmdbuildProperties(cmdbuildProperties);
		factory.setEmptyLockCard(emptyLockCardManager());
		factory.setInMemoryLockCard(memoryLockCardManager());
		return factory.create();
	}

}
