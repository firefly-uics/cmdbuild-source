package org.cmdbuild.workflow.api;

import static org.cmdbuild.common.Constants.DESCRIPTION_ATTRIBUTE;

import org.cmdbuild.api.fluent.Card;
import org.cmdbuild.api.fluent.CardDescriptor;
import org.cmdbuild.api.fluent.FluentApi;
import org.cmdbuild.api.fluent.FluentApiExecutor;
import org.cmdbuild.common.mail.MailApi;
import org.cmdbuild.common.mail.NewMail;
import org.cmdbuild.workflow.type.LookupType;
import org.cmdbuild.workflow.type.ReferenceType;

public class WorkflowApi extends FluentApi implements SchemaApi, MailApi {

	private final SchemaApi schemaApi;
	private final MailApi mailApi;

	/**
	 * It's really ugly but fortunately all is hidden behind the
	 * {@link SharkWorkflowApiFactory}.
	 */
	public WorkflowApi(final FluentApiExecutor executor, final SchemaApi schemaApi, final MailApi mailApi) {
		super(executor);
		this.schemaApi = schemaApi;
		this.mailApi = mailApi;
	}

	/*
	 * Schema
	 */

	@Override
	public ClassInfo findClass(final String className) {
		return schemaApi.findClass(className);
	}

	@Override
	public ClassInfo findClass(final int classId) {
		return schemaApi.findClass(classId);
	}

	@Override
	public LookupType selectLookupById(final int id) {
		return schemaApi.selectLookupById(id);
	}

	@Override
	public LookupType selectLookupByCode(final String type, final String code) {
		return schemaApi.selectLookupByCode(type, code);
	}

	@Override
	public LookupType selectLookupByDescription(final String type, final String description) {
		return schemaApi.selectLookupByDescription(type, description);
	}

	/*
	 * Mail
	 */

	@Override
	public NewMail newMail() {
		return mailApi.newMail();
	}

	/*
	 * Data type conversion
	 */

	public ReferenceType referenceTypeFrom(final Card card) {
		return referenceTypeFrom(card, card.getDescription());
	}

	public ReferenceType referenceTypeFrom(final CardDescriptor cardDescriptor) {
		return referenceTypeFrom(cardDescriptor, null);
	}

	private ReferenceType referenceTypeFrom(final CardDescriptor cardDescriptor, final String description) {
		return new ReferenceType( //
				cardDescriptor.getId(), //
				findClass(cardDescriptor.getClassName()).getId(), //
				(description == null) ? descriptionFor(cardDescriptor) : description);
	}

	private String descriptionFor(final CardDescriptor cardDescriptor) {
		return existingCard(cardDescriptor) //
				.with(DESCRIPTION_ATTRIBUTE, null) //
				.fetch() //
				.get(DESCRIPTION_ATTRIBUTE, String.class);
	}

	public CardDescriptor cardDescriptorFrom(final ReferenceType referenceType) {
		return new CardDescriptor( //
				findClass(referenceType.getIdClass()).getName(), //
				referenceType.getId());
	}

	public Card cardFrom(final ReferenceType referenceType) {
		return existingCard(cardDescriptorFrom(referenceType)).fetch();
	}

}
