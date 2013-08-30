package org.cmdbuild.logic.email.rules;

import org.apache.commons.lang.Validate;
import org.cmdbuild.services.email.EmailPersistence;
import org.cmdbuild.services.email.EmailRecipientTemplateResolver;
import org.cmdbuild.services.email.EmailService;
import org.cmdbuild.services.email.SubjectHandler;

public class AnswerToExistingMailFactory implements RuleFactory<AnswerToExistingMail> {

	private final EmailPersistence persistence;
	private final SubjectHandler subjectHandler;
	private final EmailRecipientTemplateResolver templateResolver;
	private EmailService service;

	public AnswerToExistingMailFactory( //
			final EmailPersistence persistence, //
			final SubjectHandler subjectHandler, //
			final EmailRecipientTemplateResolver templateResolver //
	) {
		this.persistence = persistence;
		this.subjectHandler = subjectHandler;
		this.templateResolver = templateResolver;
	}

	public AnswerToExistingMailFactory( //
			final EmailService service, //
			final EmailPersistence persistence, //
			final SubjectHandler subjectHandler, //
			final EmailRecipientTemplateResolver templateResolver //
	) {
		this(persistence, subjectHandler, templateResolver);
		this.service = service;
	}

	@Override
	public AnswerToExistingMail create() {
		Validate.notNull(service, "null service");
		Validate.notNull(persistence, "null persistence");
		Validate.notNull(subjectHandler, "null subject handler");
		Validate.notNull(templateResolver, "null template resolver");
		return create(service);
	}

	public AnswerToExistingMail create(final EmailService service) {
		Validate.notNull(service, "null service");
		Validate.notNull(persistence, "null persistence");
		Validate.notNull(subjectHandler, "null subject handler");
		Validate.notNull(templateResolver, "null template resolver");
		return new AnswerToExistingMail(service, persistence, subjectHandler, templateResolver);
	}

}
