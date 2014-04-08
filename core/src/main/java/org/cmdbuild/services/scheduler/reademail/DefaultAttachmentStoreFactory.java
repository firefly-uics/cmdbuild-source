package org.cmdbuild.services.scheduler.reademail;

import org.apache.commons.lang3.Validate;
import org.cmdbuild.dao.view.CMDataView;
import org.cmdbuild.dms.DmsConfiguration;
import org.cmdbuild.dms.DmsService;
import org.cmdbuild.dms.DocumentCreatorFactory;

public class DefaultAttachmentStoreFactory implements AttachmentStoreFactory {

	private final CMDataView dataView;
	private final DocumentCreatorFactory documentCreatorFactory;
	private final DmsConfiguration dmsConfiguration;
	private final DmsService dmsService;
	private final String user;

	public DefaultAttachmentStoreFactory( //
			final CMDataView dataView, //
			final DocumentCreatorFactory documentCreatorFactory, //
			final DmsConfiguration dmsConfiguration, //
			final DmsService dmsService, //
			final String user) {
		this.dataView = dataView;
		this.documentCreatorFactory = documentCreatorFactory;
		this.dmsConfiguration = dmsConfiguration;
		this.dmsService = dmsService;
		this.user = user;
	}

	@Override
	public AttachmentStore create(final String className, final Long id) {
		Validate.notNull(dataView, "null data view");
		Validate.notNull(documentCreatorFactory, "null document creator factory");
		Validate.notNull(dmsConfiguration, "null dms configuration");
		Validate.notNull(dmsService, "null dms service");
		Validate.notNull(user, "null user");
		Validate.notNull(className, "null class name");
		Validate.notNull(id, "null id");
		return new DefaultAttachmentStore( //
				dataView, //
				className, //
				id, //
				user, //
				documentCreatorFactory, //
				dmsConfiguration, //
				dmsService);
	}

}
