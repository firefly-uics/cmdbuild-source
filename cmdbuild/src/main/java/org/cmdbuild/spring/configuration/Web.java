package org.cmdbuild.spring.configuration;

import javax.servlet.http.HttpServletRequest;

import org.cmdbuild.auth.UserStore;
import org.cmdbuild.listeners.ContextStore;
import org.cmdbuild.listeners.ContextStoreNotifier;
import org.cmdbuild.listeners.HttpSessionBasedValuesStore;
import org.cmdbuild.listeners.OptionalRequestSupplier;
import org.cmdbuild.listeners.ThreadLocalContextStore;
import org.cmdbuild.listeners.ValuesStore;
import org.cmdbuild.notification.Notifier;
import org.cmdbuild.services.SessionVars;
import org.cmdbuild.services.ValuesStoreBasedUserStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.google.common.base.Optional;
import com.google.common.base.Supplier;

@Configuration
public class Web {

	@Autowired
	private Properties properties;

	@Bean
	public SessionVars sessionVars() {
		return new SessionVars(valuesStore(), properties.cmdbuildProperties());
	}

	@Bean
	public UserStore userStore() {
		return new ValuesStoreBasedUserStore(valuesStore());
	}

	@Bean
	protected ValuesStore valuesStore() {
		return new HttpSessionBasedValuesStore(optionalRequestSupplier(), properties.cmdbuildProperties());
	}

	@Bean
	protected Supplier<Optional<HttpServletRequest>> optionalRequestSupplier() {
		return new OptionalRequestSupplier(contextStore());
	}

	@Bean
	public Notifier notifier() {
		return new ContextStoreNotifier(contextStore());
	}

	@Bean
	protected ContextStore contextStore() {
		return new ThreadLocalContextStore();
	}

}
