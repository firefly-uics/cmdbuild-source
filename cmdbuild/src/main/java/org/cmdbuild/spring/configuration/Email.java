package org.cmdbuild.spring.configuration;

import org.cmdbuild.common.mail.DefaultMailApiFactory;
import org.cmdbuild.common.mail.MailApiFactory;
import org.cmdbuild.config.EmailConfiguration;
import org.cmdbuild.dao.view.DBDataView;
import org.cmdbuild.data.store.email.EmailTemplateStorableConverter;
import org.cmdbuild.data.store.email.EmailTemplateStore;
import org.cmdbuild.data.store.lookup.LookupStore;
import org.cmdbuild.dms.DmsConfiguration;
import org.cmdbuild.dms.DmsService;
import org.cmdbuild.dms.DocumentCreatorFactory;
import org.cmdbuild.logic.email.DefaultEmailTemplateLogic;
import org.cmdbuild.logic.email.EmailLogic;
import org.cmdbuild.logic.email.EmailTemplateLogic;
import org.cmdbuild.notification.Notifier;
import org.cmdbuild.services.email.DefaultEmailPersistence;
import org.cmdbuild.services.email.DefaultEmailService;
import org.cmdbuild.services.email.DefaultSubjectHandler;
import org.cmdbuild.services.email.EmailPersistence;
import org.cmdbuild.services.email.EmailRecipientTemplateResolver;
import org.cmdbuild.services.email.EmailService;
import org.cmdbuild.services.email.SubjectHandler;
import org.cmdbuild.spring.annotations.ConfigurationComponent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Scope;

@ConfigurationComponent
public class Email {

	@Autowired
	private DmsService dmsService;

	@Autowired
	private DmsConfiguration dmsConfiguration;

	@Autowired
	private DocumentCreatorFactory documentCreatorFactory;

	@Autowired
	private EmailConfiguration emailConfiguration;

	@Autowired
	private LookupStore lookupStore;

	@Autowired
	private Notifier notifier;

	@Autowired
	private DBDataView systemDataView;

	@Bean
	public MailApiFactory mailApiFactory() {
		return new DefaultMailApiFactory();
	}

	@Bean
	public EmailPersistence emailPersistence() {
		return new DefaultEmailPersistence( //
				systemDataView, //
				lookupStore, //
				dmsService, //
				dmsConfiguration, //
				documentCreatorFactory);
	}

	@Bean
	public EmailService emailService() {
		return new DefaultEmailService( //
				emailConfiguration, //
				mailApiFactory(), //
				emailPersistence());
	}

	@Bean
	public EmailRecipientTemplateResolver emailRecipientTemplateResolver() {
		return new EmailRecipientTemplateResolver(systemDataView);
	}

	@Bean
	protected SubjectHandler subjectHandler() {
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
	@Scope("prototype")
	public EmailLogic emailLogic() {
		return new EmailLogic(emailConfiguration, emailService(), subjectHandler(), notifier);
	}

	@Bean
	@Scope("prototype")
	public EmailTemplateLogic emailTemplateLogic() {
		return new DefaultEmailTemplateLogic(emailTemplateStore());
	}

}
