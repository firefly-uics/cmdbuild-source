package org.cmdbuild.logic.email.rules;

import static org.cmdbuild.data.converter.EmailConverter.EMAIL_CLASS_NAME;

import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.dao.view.CMDataView;
import org.cmdbuild.dms.DmsConfiguration;
import org.cmdbuild.dms.DmsService;
import org.cmdbuild.dms.DocumentCreator;
import org.cmdbuild.dms.DocumentCreatorFactory;
import org.cmdbuild.dms.StorableDocument;
import org.cmdbuild.logic.Logic;
import org.cmdbuild.model.email.Attachment;
import org.cmdbuild.model.email.Email;
import org.cmdbuild.services.email.EmailCallbackHandler.Rule;
import org.cmdbuild.services.email.EmailCallbackHandler.RuleAction;
import org.slf4j.Logger;

public class DownloadAttachments implements Rule {

	private static final Logger logger = Logic.logger;

	private static final String USER_FOR_ATTACHMENTS_UPLOAD = "system";

	private final DmsConfiguration dmsConfiguration;
	private final DocumentCreatorFactory documentCreatorFactory;
	private final DmsService dmsService;
	private final CMDataView dataView;

	public DownloadAttachments( //
			final DmsConfiguration dmsConfiguration, //
			final DocumentCreatorFactory documentCreatorFactory, //
			final DmsService dmsService, //
			final CMDataView dataView //
	) {
		this.dmsConfiguration = dmsConfiguration;
		this.documentCreatorFactory = documentCreatorFactory;
		this.dmsService = dmsService;
		this.dataView = dataView;
	}

	@Override
	public boolean applies(final Email email) {
		return true;
	}

	@Override
	public Email adapt(final Email email) {
		return email;
	}

	@Override
	public RuleAction action(final Email email) {
		return new RuleAction() {

			@Override
			public void execute() {
				storeAttachmentsOf(email);
			}

			private void storeAttachmentsOf(final Email email) {
				logger.info("storing attachments for email {}", email);

				if (!dmsConfiguration.isEnabled()) {
					logger.warn("dms service not enabled");
					return;
				}

				final CMClass fetchedClass = dataView.findClass(EMAIL_CLASS_NAME);
				documentCreatorFactory.setClass(fetchedClass);
				final DocumentCreator documentFactory = documentCreatorFactory.create();

				for (final Attachment attachment : email.getAttachments()) {
					InputStream inputStream = null;
					try {
						logger.debug("uploading attachment '{}'", attachment.getName());
						inputStream = attachment.getDataHandler().getInputStream();
						final StorableDocument document = documentFactory.createStorableDocument( //
								USER_FOR_ATTACHMENTS_UPLOAD, //
								EMAIL_CLASS_NAME, //
								email.getId().intValue(), //
								inputStream, //
								attachment.getName(), //
								dmsConfiguration.getLookupNameForAttachments(), //
								attachment.getName());
						dmsService.upload(document);
					} catch (final Exception e) {
						logger.warn("error storing attachment into dms", e);
					} finally {
						if (inputStream != null) {
							IOUtils.closeQuietly(inputStream);
						}
					}
				}
			}

		};
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE) //
				.toString();
	}

}
