package org.cmdbuild.logic.email.rules;

import org.apache.commons.lang.Validate;
import org.cmdbuild.dao.view.CMDataView;
import org.cmdbuild.data.store.lookup.LookupStore;
import org.cmdbuild.services.email.EmailPersistence;
import org.cmdbuild.services.email.EmailService;
import org.cmdbuild.services.email.EmailTemplateResolver;
import org.cmdbuild.services.email.SubjectHandler;

public class AnswerToExistingMailFactory implements RuleFactory<AnswerToExistingMail> {

	private final EmailPersistence persistence;
	private final SubjectHandler subjectHandler;
	private final EmailTemplateResolver.DataFacade dataFacade;
	private final CMDataView dataView;
	private final LookupStore lookupStore;
	private EmailService service;

	public AnswerToExistingMailFactory( //
			final EmailPersistence persistence, //
			final SubjectHandler subjectHandler, //
			final EmailTemplateResolver.DataFacade dataFacade, //
			final CMDataView dataView, //
			final LookupStore lookupStore //
	) {
		this.persistence = persistence;
		this.subjectHandler = subjectHandler;
		this.dataFacade = dataFacade;
		this.dataView = dataView;
		this.lookupStore = lookupStore;
	}

	public AnswerToExistingMailFactory( //
			final EmailService service, //
			final EmailPersistence persistence, //
			final SubjectHandler subjectHandler, //
			final EmailTemplateResolver.DataFacade dataFacade, //
			final CMDataView dataView, //
			final LookupStore lookupStore //
	) {
		this(persistence, subjectHandler, dataFacade, dataView, lookupStore);
		this.service = service;
	}

	@Override
	public AnswerToExistingMail create() {
		Validate.notNull(service, "null service");
		Validate.notNull(persistence, "null persistence");
		Validate.notNull(subjectHandler, "null subject handler");
		Validate.notNull(dataFacade, "null template resolver data facade");
		return create(service);
	}

	public AnswerToExistingMail create(final EmailService service) {
		Validate.notNull(service, "null service");
		Validate.notNull(persistence, "null persistence");
		Validate.notNull(subjectHandler, "null subject handler");
		Validate.notNull(dataFacade, "null template resolver data facade");
		Validate.notNull(dataView, "null data view");
		Validate.notNull(lookupStore, "null lookup store");
		return new AnswerToExistingMail(service, persistence, subjectHandler, dataFacade, dataView, lookupStore);
	}

}
