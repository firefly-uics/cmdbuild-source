package org.cmdbuild.spring.configuration;

import org.cmdbuild.auth.UserStore;
import org.cmdbuild.logic.data.ConfigurationAwareLockLogic;
import org.cmdbuild.logic.data.DefaultLockLogic;
import org.cmdbuild.logic.data.DummyLockLogic;
import org.cmdbuild.logic.data.LockLogic;
import org.cmdbuild.logic.data.access.lock.CmdbuildConfigurationAdapter;
import org.cmdbuild.logic.data.access.lock.DefaultLockManager;
import org.cmdbuild.logic.data.access.lock.ExpiringLockableStore;
import org.cmdbuild.logic.data.access.lock.LockManager;
import org.cmdbuild.logic.data.access.lock.LockableStore;
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

	public static final String USER_LOCK_LOGIC = "UserLockLogic";

	@Bean(name = USER_LOCK_LOGIC)
	public LockLogic configurationAwareLockLogic() {
		return new ConfigurationAwareLockLogic(properties.cmdbuildProperties(), dummyLockLogic(), defaultLockLogic());
	}

	@Bean
	protected LockLogic defaultLockLogic() {
		// TODO wrap properties
		return new DefaultLockLogic(properties.cmdbuildProperties(), synchronizedLockCardManager());
	}

	@Bean
	public LockLogic dummyLockLogic() {
		return new DummyLockLogic();
	}

	@Bean
	protected LockManager synchronizedLockCardManager() {
		return new SynchronizedLockManager(defaultLockManager());
	}

	@Bean
	protected LockManager defaultLockManager() {
		return new DefaultLockManager(expiringLockableStore(), usernameSupplier());
	}

	@Bean
	protected LockableStore<DefaultLockManager.Lock> expiringLockableStore() {
		return new ExpiringLockableStore<DefaultLockManager.Lock>(cmdbuildConfigurationAdapter());
	}

	@Bean
	protected CmdbuildConfigurationAdapter cmdbuildConfigurationAdapter() {
		return new CmdbuildConfigurationAdapter(properties.cmdbuildProperties());
	}

	@Bean
	protected Supplier<String> usernameSupplier() {
		return new UsernameSupplier(userStore);
	}

}
