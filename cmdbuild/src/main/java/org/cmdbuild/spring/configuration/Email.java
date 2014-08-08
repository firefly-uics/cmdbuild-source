package org.cmdbuild.spring.configuration;

import static org.cmdbuild.services.email.Predicates.isDefault;
import static org.cmdbuild.spring.util.Constants.PROTOTYPE;

import org.cmdbuild.auth.UserStore;
import org.cmdbuild.common.api.mail.MailApiFactory;
import org.cmdbuild.common.api.mail.javax.mail.JavaxMailBasedMailApiFactory;
import org.cmdbuild.data.store.Store;
import org.cmdbuild.data.store.dao.DataViewStore;
import org.cmdbuild.data.store.dao.StorableConverter;
import org.cmdbuild.data.store.email.EmailAccountStorableConverter;
import org.cmdbuild.data.store.email.EmailConverter;
import org.cmdbuild.data.store.email.EmailTemplateStorableConverter;
import org.cmdbuild.data.store.email.ExtendedEmailTemplate;
import org.cmdbuild.data.store.email.ExtendedEmailTemplateStore;
import org.cmdbuild.data.store.email.DefaultEmailAccount;
import org.cmdbuild.logic.email.DefaultEmailAccountLogic;
import org.cmdbuild.logic.email.DefaultEmailTemplateLogic;
import org.cmdbuild.logic.email.EmailAccountLogic;
import org.cmdbuild.logic.email.EmailLogic;
import org.cmdbuild.logic.email.EmailTemplateLogic;
import org.cmdbuild.logic.email.TransactionalEmailTemplateLogic;
import org.cmdbuild.notification.Notifier;
import org.cmdbuild.services.email.ConfigurableEmailServiceFactory;
import org.cmdbuild.services.email.DefaultEmailPersistence;
import org.cmdbuild.services.email.DefaultSubjectHandler;
import org.cmdbuild.services.email.EmailAccount;
import org.cmdbuild.services.email.EmailPersistence;
import org.cmdbuild.services.email.EmailService;
import org.cmdbuild.services.email.EmailServiceFactory;
import org.cmdbuild.services.email.PredicateEmailAccountSupplier;
import org.cmdbuild.services.email.SubjectHandler;
import org.cmdbuild.spring.annotations.ConfigurationComponent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Scope;

import com.google.common.base.Supplier;

@ConfigurationComponent
public class Email {

	@Autowired
	private Data data;

	@Autowired
	private Dms dms;

	@Autowired
	private Notifier notifier;

	@Autowired
	private Properties properties;

	@Autowired
	private UserStore userStore;

	@Bean
	protected StorableConverter<EmailAccount> emailAccountConverter() {
		return new EmailAccountStorableConverter();
	}

	@Bean
	public Store<EmailAccount> emailAccountStore() {
		return DataViewStore.<EmailAccount> newInstance() //
				.withDataView(data.systemDataView()) //
				.withStorableConverter(emailAccountConverter()) //
				.build();
	}

	@Bean
	public Supplier<EmailAccount> defaultEmailAccountSupplier() {
		return PredicateEmailAccountSupplier.of(emailAccountStore(), isDefault());
	}

	@Bean
	public MailApiFactory mailApiFactory() {
		return new JavaxMailBasedMailApiFactory();
	}

	@Bean
	public EmailPersistence emailPersistence() {
		return new DefaultEmailPersistence( //
				emailStore(), //
				emailTemplateStore());
	}

	@Bean
	protected Store<org.cmdbuild.data.store.email.Email> emailStore() {
		return DataViewStore.<org.cmdbuild.data.store.email.Email> newInstance() //
				.withDataView(data.systemDataView()) //
				.withStorableConverter(emailStorableConverter()) //
				.build();
	}

	@Bean
	protected StorableConverter<org.cmdbuild.data.store.email.Email> emailStorableConverter() {
		return new EmailConverter(data.lookupStore());
	}

	@Bean
	public EmailService defaultEmailService() {
		return emailServiceFactory() //
				.create();

	}

	@Bean
	public EmailServiceFactory emailServiceFactory() {
		return ConfigurableEmailServiceFactory.newInstance() //
				.withApiFactory(mailApiFactory()) //
				.withPersistence(emailPersistence()) //
				.withConfiguration(defaultEmailAccountSupplier()) //
				.build();
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
	protected Store<ExtendedEmailTemplate> emailTemplateStore() {
		return ExtendedEmailTemplateStore.newInstance() //
				.withDataView(data.systemDataView()) //
				.withConverter(emailTemplateStorableConverter()) //
				.build();
	}

	@Bean
	@Scope(PROTOTYPE)
	public EmailLogic emailLogic() {
		return new EmailLogic( //
				data.systemDataView(), //
				defaultEmailAccountSupplier(), //
				defaultEmailService(), //
				subjectHandler(), //
				properties.dmsProperties(), //
				dms.dmsService(), //
				dms.documentCreatorFactory(), //
				notifier, //
				userStore.getUser());
	}

	@Bean
	public EmailTemplateLogic emailTemplateLogic() {
		return new TransactionalEmailTemplateLogic(new DefaultEmailTemplateLogic(emailTemplateStore()));
	}

	@Bean
	public EmailAccountLogic emailAccountLogic() {
		return new DefaultEmailAccountLogic(emailAccountStore());
	}

}
