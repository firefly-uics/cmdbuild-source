package org.cmdbuild.logic.email.rules;

import org.apache.commons.lang.Validate;
import org.cmdbuild.dao.view.CMDataView;
import org.cmdbuild.dms.DmsConfiguration;
import org.cmdbuild.dms.DmsService;
import org.cmdbuild.dms.DocumentCreatorFactory;

public class DownloadAttachmentsFactory implements RuleFactory<DownloadAttachments> {

	private final DmsConfiguration configuration;
	private final DocumentCreatorFactory documentCreatorFactory;
	private final DmsService service;
	private final CMDataView dataView;

	public DownloadAttachmentsFactory( //
			final DmsConfiguration dmsConfiguration, //
			final DocumentCreatorFactory documentCreatorFactory, //
			final DmsService dmsService, //
			final CMDataView dataView //
	) {
		this.configuration = dmsConfiguration;
		this.documentCreatorFactory = documentCreatorFactory;
		this.service = dmsService;
		this.dataView = dataView;
	}

	@Override
	public DownloadAttachments create() {
		Validate.notNull(configuration, "null configuration");
		Validate.notNull(documentCreatorFactory, "null document creator factory");
		Validate.notNull(service, "null service");
		Validate.notNull(dataView, "null data view");
		return new DownloadAttachments(configuration, documentCreatorFactory, service, dataView);
	}

}
