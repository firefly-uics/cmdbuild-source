package org.cmdbuild.spring.configuration;

import javax.servlet.http.HttpServletRequest;

import org.cmdbuild.auth.DefaultTokenManager;
import org.cmdbuild.auth.ObservableUserStore;
import org.cmdbuild.auth.SimpleTokenGenerator;
import org.cmdbuild.auth.TokenGenerator;
import org.cmdbuild.auth.TokenManager;
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
		return new SessionVars(httpSessionBasedValuesStore(), properties.cmdbuildProperties());
	}

	@Bean
	public UserStore observableUserStore() {
		final ObservableUserStore bean = new ObservableUserStore(valuesStoreBasedUserStore());
		bean.add(defaultTokenManager());
		return bean;
	}

	// TODO should be a bean
	private UserStore valuesStoreBasedUserStore() {
		return new ValuesStoreBasedUserStore(httpSessionBasedValuesStore());
	}

	@Bean
	protected ValuesStore httpSessionBasedValuesStore() {
		return new HttpSessionBasedValuesStore(optionalRequestSupplier(), properties.cmdbuildProperties());
	}

	@Bean
	protected Supplier<Optional<HttpServletRequest>> optionalRequestSupplier() {
		return new OptionalRequestSupplier(threadLocalContextStore());
	}

	@Bean
	public Notifier contextStoreNotifier() {
		return new ContextStoreNotifier(threadLocalContextStore());
	}

	@Bean
	public ContextStore threadLocalContextStore() {
		return new ThreadLocalContextStore();
	}

	@Bean
	public TokenManager defaultTokenManager() {
		return new DefaultTokenManager(simpleTokenGenerator());
	}

	@Bean
	public TokenGenerator simpleTokenGenerator() {
		return new SimpleTokenGenerator();
	}

}
