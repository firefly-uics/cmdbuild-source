package org.cmdbuild.logic.email.rules;

import static com.google.common.collect.Iterables.isEmpty;

import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.dao.view.CMDataView;
import org.cmdbuild.dms.DmsConfiguration;
import org.cmdbuild.dms.DmsService;
import org.cmdbuild.dms.DocumentCreator;
import org.cmdbuild.dms.DocumentCreatorFactory;
import org.cmdbuild.dms.StorableDocument;
import org.cmdbuild.logic.Logic;
import org.cmdbuild.model.email.Attachment;
import org.slf4j.Logger;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

public class DefaultAttachmentStore implements AttachmentStore {

	private static final Logger logger = Logic.logger;
	private static final Marker marker = MarkerFactory.getMarker(DefaultAttachmentStore.class.getName());

	private final CMDataView dataView;
	private final String className;
	private final Long id;
	private final String user;
	private final DocumentCreatorFactory documentCreatorFactory;
	private final DmsConfiguration dmsConfiguration;
	private final DmsService dmsService;

	public DefaultAttachmentStore( //
			final CMDataView dataView, //
			final String className, //
			final Long id, //
			final String user, //
			final DocumentCreatorFactory documentCreatorFactory, //
			final DmsConfiguration dmsConfiguration, //
			final DmsService dmsService //
	) {
		this.dataView = dataView;
		this.className = className;
		this.id = id;
		this.user = user;
		this.documentCreatorFactory = documentCreatorFactory;
		this.dmsConfiguration = dmsConfiguration;
		this.dmsService = dmsService;
	}

	@Override
	public void store(final Iterable<Attachment> attachments) {
		logger.info("storing attachments '{}'");

		
		if (!isEmpty(attachments) && !dmsConfiguration.isEnabled()) {
			logger.warn(marker, "dms service not enabled, cannot store attachments");
			return;
		}

		final CMClass fetchedClass = dataView.findClass(className);
		final DocumentCreator documentFactory = documentCreatorFactory.create(fetchedClass);

		for (final Attachment attachment : attachments) {
			InputStream inputStream = null;
			try {
				logger.debug(marker, "storing attachment '{}'", attachment);
				inputStream = attachment.getDataHandler().getInputStream();
				final StorableDocument document = documentFactory.createStorableDocument( //
						user, //
						className, //
						id.toString(), //
						inputStream, //
						attachment.getName(), //
						dmsConfiguration.getLookupNameForAttachments(), //
						attachment.getName());
				dmsService.upload(document);
			} catch (final Exception e) {
				logger.warn(marker, "error storing attachment into dms", e);
			} finally {
				if (inputStream != null) {
					IOUtils.closeQuietly(inputStream);
				}
			}
		}
	}

}
