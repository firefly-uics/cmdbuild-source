package org.cmdbuild.spring.configuration;

import static org.cmdbuild.services.email.Predicates.isDefault;
import static org.cmdbuild.spring.util.Constants.PROTOTYPE;

import org.cmdbuild.auth.UserStore;
import org.cmdbuild.common.api.mail.MailApiFactory;
import org.cmdbuild.common.api.mail.javax.mail.JavaxMailBasedMailApiFactory;
import org.cmdbuild.data.store.InMemoryStore;
import org.cmdbuild.data.store.Store;
import org.cmdbuild.data.store.StoreSupplier;
import org.cmdbuild.data.store.dao.DataViewStore;
import org.cmdbuild.data.store.dao.StorableConverter;
import org.cmdbuild.data.store.email.EmailAccountStorableConverter;
import org.cmdbuild.data.store.email.EmailConverter;
import org.cmdbuild.data.store.email.EmailTemplateStorableConverter;
import org.cmdbuild.data.store.email.ExtendedEmailTemplate;
import org.cmdbuild.data.store.email.ExtendedEmailTemplateStore;
import org.cmdbuild.logic.email.DefaultEmailAccountLogic;
import org.cmdbuild.logic.email.DefaultEmailAttachmentsLogic;
import org.cmdbuild.logic.email.DefaultEmailLogic;
import org.cmdbuild.logic.email.DefaultEmailTemplateLogic;
import org.cmdbuild.logic.email.EmailAccountLogic;
import org.cmdbuild.logic.email.EmailAttachmentsLogic;
import org.cmdbuild.logic.email.EmailLogic;
import org.cmdbuild.logic.email.EmailTemplateLogic;
import org.cmdbuild.logic.email.TransactionalEmailTemplateLogic;
import org.cmdbuild.notification.Notifier;
import org.cmdbuild.services.email.ConfigurableEmailServiceFactory;
import org.cmdbuild.services.email.DefaultSubjectHandler;
import org.cmdbuild.services.email.EmailAccount;
import org.cmdbuild.services.email.EmailServiceFactory;
import org.cmdbuild.services.email.SubjectHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

import com.google.common.base.Supplier;

@Configuration
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
	protected Supplier<EmailAccount> defaultEmailAccountSupplier() {
		return StoreSupplier.of(EmailAccount.class, emailAccountStore(), isDefault());
	}

	@Bean
	public MailApiFactory mailApiFactory() {
		return new JavaxMailBasedMailApiFactory();
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
	public EmailServiceFactory emailServiceFactory() {
		return ConfigurableEmailServiceFactory.newInstance() //
				.withApiFactory(mailApiFactory()) //
				.withPersistence(emailStore()) //
				.withDefaultAccountSupplier(defaultEmailAccountSupplier()) //
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
	@Scope(PROTOTYPE)
	public EmailAttachmentsLogic emailAttachmentsLogic() {
		return new DefaultEmailAttachmentsLogic( //
				data.systemDataView(), //
				properties.dmsProperties(), //
				dms.dmsService(), //
				dms.documentCreatorFactory(), //
				userStore.getUser());
	}

	@Bean
	@Scope(PROTOTYPE)
	public EmailLogic emailLogic() {
		return new DefaultEmailLogic( //
				emailStore(), //
				emailTemporaryStore(), //
				emailServiceFactory(), //
				emailAccountStore(), //
				subjectHandler(), //
				notifier, 
				emailAttachmentsLogic());
	}

	@Bean
	protected Store<org.cmdbuild.data.store.email.Email> emailTemporaryStore() {
		return InMemoryStore.of(org.cmdbuild.data.store.email.Email.class);
	}

	@Bean
	public EmailTemplateLogic emailTemplateLogic() {
		return new TransactionalEmailTemplateLogic(new DefaultEmailTemplateLogic(templateStore(), emailAccountStore()));
	}

	@Bean
	protected Store<ExtendedEmailTemplate> templateStore() {
		return ExtendedEmailTemplateStore.newInstance() //
				.withDataView(data.systemDataView()) //
				.withConverter(emailTemplateStorableConverter()) //
				.build();
	}

	@Bean
	public EmailAccountLogic emailAccountLogic() {
		return new DefaultEmailAccountLogic(emailAccountStore());
	}

}
