package org.cmdbuild.spring.configuration;

import static org.cmdbuild.spring.util.Constants.PROTOTYPE;

import org.cmdbuild.common.mail.DefaultMailApiFactory;
import org.cmdbuild.common.mail.MailApiFactory;
import org.cmdbuild.config.EmailConfiguration;
import org.cmdbuild.dao.view.DBDataView;
import org.cmdbuild.data.converter.EmailConverter;
import org.cmdbuild.data.store.DataViewStore;
import org.cmdbuild.data.store.DataViewStore.StorableConverter;
import org.cmdbuild.data.store.Store;
import org.cmdbuild.data.store.email.EmailAccount;
import org.cmdbuild.data.store.email.EmailAccountStorableConverter;
import org.cmdbuild.data.store.email.EmailTemplateStorableConverter;
import org.cmdbuild.data.store.email.EmailTemplateStore;
import org.cmdbuild.data.store.lookup.LookupStore;
import org.cmdbuild.logic.email.DefaultEmailTemplateLogic;
import org.cmdbuild.logic.email.EmailLogic;
import org.cmdbuild.logic.email.EmailTemplateLogic;
import org.cmdbuild.notification.Notifier;
import org.cmdbuild.services.email.ConfigurableEmailServiceFactory;
import org.cmdbuild.services.email.DefaultEmailConfigurationFactory;
import org.cmdbuild.services.email.DefaultEmailPersistence;
import org.cmdbuild.services.email.DefaultEmailService;
import org.cmdbuild.services.email.DefaultEmailTemplateResolver;
import org.cmdbuild.services.email.DefaultSubjectHandler;
import org.cmdbuild.services.email.EmailConfigurationFactory;
import org.cmdbuild.services.email.EmailPersistence;
import org.cmdbuild.services.email.EmailService;
import org.cmdbuild.services.email.FailSafeDataFacade;
import org.cmdbuild.services.email.SubjectHandler;
import org.cmdbuild.spring.annotations.ConfigurationComponent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Scope;

@ConfigurationComponent
public class Email {

	@Autowired
	private LookupStore lookupStore;

	@Autowired
	private Notifier notifier;

	@Autowired
	private DBDataView systemDataView;

	@Bean
	protected StorableConverter<EmailAccount> emailAccountConverter() {
		return new EmailAccountStorableConverter();
	}

	@Bean
	public Store<EmailAccount> emailAccountStore() {
		return new DataViewStore<EmailAccount>(systemDataView, emailAccountConverter());
	}

	@Bean
	@Scope(PROTOTYPE)
	public EmailConfigurationFactory defaultEmailConfigurationFactory() {
		return new DefaultEmailConfigurationFactory(emailAccountStore());
	}

	@Bean
	public MailApiFactory mailApiFactory() {
		return new DefaultMailApiFactory();
	}

	@Bean
	public EmailPersistence emailPersistence() {
		return new DefaultEmailPersistence( //
				emailStore(), //
				emailTemplateStore());
	}

	@Bean
	protected Store<org.cmdbuild.model.email.Email> emailStore() {
		return new DataViewStore<org.cmdbuild.model.email.Email>(systemDataView, emailStorableConverter());
	}

	@Bean
	protected StorableConverter<org.cmdbuild.model.email.Email> emailStorableConverter() {
		return new EmailConverter(lookupStore);
	}

	@Bean
	@Scope(PROTOTYPE)
	public EmailService defaultEmailService() {
		return new DefaultEmailService( //
				defaultEmailConfigurationFactory(), //
				mailApiFactory(), //
				emailPersistence());
	}

	@Bean
	public ConfigurableEmailServiceFactory configurableEmailServiceFactory() {
		return new ConfigurableEmailServiceFactory(mailApiFactory(), emailPersistence());
	}

	@Bean
	protected DefaultEmailTemplateResolver.DataFacade dataFacade() {
		return new FailSafeDataFacade(defaultDataFacade());
	}

	@Bean
	protected DefaultEmailTemplateResolver.DataFacade defaultDataFacade() {
		return new DefaultEmailTemplateResolver.DefaultDataFacade(systemDataView);
	}

	@Bean
	public SubjectHandler subjectHandler() {
		return new DefaultSubjectHandler();
	}

	@Bean
	protected EmailTemplateStorableConverter emailTemplateStorableConverter() {
		return new EmailTemplateStorableConverter();
	}

	@Bean
	public EmailTemplateStore emailTemplateStore() {
		return new EmailTemplateStore(emailTemplateStorableConverter(), systemDataView);
	}

	@Bean
	@Scope(PROTOTYPE)
	public EmailLogic emailLogic() {
		return new EmailLogic( //
				defaultEmailConfigurationFactory(), //
				defaultEmailService(), //
				subjectHandler(), //
				notifier);
	}

	@Bean
	@Scope(PROTOTYPE)
	public EmailTemplateLogic emailTemplateLogic() {
		return new DefaultEmailTemplateLogic(emailTemplateStore());
	}

}
