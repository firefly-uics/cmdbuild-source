package org.cmdbuild.spring.configuration;

import org.cmdbuild.auth.UserStore;
import org.cmdbuild.logic.data.DefaultLockLogic;
import org.cmdbuild.logic.data.LockLogic;
import org.cmdbuild.logic.data.access.lock.CmdbuildConfigurationAdapter;
import org.cmdbuild.logic.data.access.lock.ConfigurationAwareLockManager;
import org.cmdbuild.logic.data.access.lock.EmptyLockManager;
import org.cmdbuild.logic.data.access.lock.InMemoryLockManager;
import org.cmdbuild.logic.data.access.lock.LockManager;
import org.cmdbuild.logic.data.access.lock.SynchronizedLockManager;
import org.cmdbuild.logic.data.access.lock.UsernameSupplier;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.google.common.base.Supplier;

@Configuration
public class Lock {

	@Autowired
	private Properties properties;

	@Autowired
	private UserStore userStore;

	@Bean
	public LockLogic lockLogic() {
		return new DefaultLockLogic(lockManager());
	}

	@Bean
	public LockManager lockManager() {
		return new ConfigurationAwareLockManager(properties.cmdbuildProperties(), emptyLockManager(),
				inMemoryLockCardManager());
	}

	@Bean
	public LockManager emptyLockManager() {
		return new EmptyLockManager();
	}

	@Bean
	protected LockManager inMemoryLockCardManager() {
		return new SynchronizedLockManager(new InMemoryLockManager(inMemoryLockCardConfiguration(), usernameSupplier()));
	}

	@Bean
	protected Supplier<String> usernameSupplier() {
		return new UsernameSupplier(userStore);
	}

	@Bean
	protected InMemoryLockManager.Configuration inMemoryLockCardConfiguration() {
		return new CmdbuildConfigurationAdapter(properties.cmdbuildProperties());
	}

}
