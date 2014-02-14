package org.cmdbuild.data.store.email;

import java.util.LinkedList;
import java.util.List;

import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.dao.view.CMDataView;
import org.cmdbuild.data.store.Store;
import org.cmdbuild.model.email.EmailTemplate;

public class EmailTemplateStore implements Store<EmailTemplate> {

	private Store<EmailTemplate> baseStore;
	private CMDataView dataView;

	public EmailTemplateStore( //
			final Store<EmailTemplate> baseStore, //
			final CMDataView dataView //
		) {

		this.baseStore = baseStore;
		this.dataView = dataView;
	}

	@Override
	public Storable create(final EmailTemplate emailTemplate) {
		return baseStore.create(emailTemplate);
	}

	@Override
	public EmailTemplate read(final Storable emailTemplate) {
		return baseStore.read(emailTemplate);
	}

	@Override
	public void update(final EmailTemplate emailTemplate) {
		baseStore.update(emailTemplate);
	}

	@Override
	public void delete(final Storable emailTemplate) {
		baseStore.delete(emailTemplate);
	}

	@Override
	public List<EmailTemplate> list() {
		return baseStore.list();
	}

	public List<EmailTemplate> readForEntryType(final String entryTypeName) {
		final List<EmailTemplate> fetchedTemplates = list();
		final List<EmailTemplate> templatesOfInterest = new LinkedList<EmailTemplate>();

		if (!isStringEmptyOrNull(entryTypeName)) {
			final CMClass entryType = dataView.findClass(entryTypeName);
			if (entryType != null) {
				for (EmailTemplate emailTemplate: fetchedTemplates) {
					final Long ownerEntryTypeId = emailTemplate.getOwnerClassId();
					if (isOfInterest(entryType, ownerEntryTypeId)) {
						templatesOfInterest.add(emailTemplate);
					}
				}
			}
		}

		return templatesOfInterest;
	}

	private boolean isStringEmpty(final String tested) {
		return "".equals(tested);
	}

	private boolean isStringEmptyOrNull(final String tested) {
		return (isStringEmpty(tested) || tested == null);
	}

	private boolean isOfInterest(final CMClass entryType, final Long ownerEntryTypeId) {
		return ownerEntryTypeId == null // interests to all classes
				|| entryType.getId().equals(ownerEntryTypeId); // is specific of the given entryType
	}
}
