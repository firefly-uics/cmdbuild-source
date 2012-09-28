package org.cmdbuild.dms;

import java.util.List;

import javax.activation.DataHandler;

import org.cmdbuild.dms.DmsService.LoggingSupport;
import org.cmdbuild.dms.MetadataAutocompletion.AutocompletionRules;
import org.cmdbuild.dms.exception.DmsException;

public class LoggedDmsService extends ForwardingDmsService implements LoggingSupport {

	public LoggedDmsService(final DmsService dmsService) {
		super(dmsService);
	}

	@Override
	public DmsConfiguration getConfiguration() {
		logger.info("getting configuration");
		return super.getConfiguration();
	}

	@Override
	public void setConfiguration(final DmsConfiguration configuration) {
		logger.info("setting configuration");
		super.setConfiguration(configuration);
	}

	@Override
	public Iterable<DocumentTypeDefinition> getTypeDefinitions() {
		logger.info("getting document type definitions");
		return super.getTypeDefinitions();
	}

	@Override
	public List<StoredDocument> search(final DocumentSearch document) {
		logger.info("fetching stored documents");
		return super.search(document);
	}

	@Override
	public void upload(final StorableDocument document) throws DmsException {
		logger.info("storing document");
		super.upload(document);
	}

	@Override
	public DataHandler download(final DocumentDownload document) throws DmsException {
		logger.info("retrieving document");
		return super.download(document);
	}

	@Override
	public void delete(final DocumentDelete document) throws DmsException {
		logger.info("deleting document");
		super.delete(document);
	}

	@Override
	public void updateDescriptionAndMetadata(final DocumentUpdate document) throws DmsException {
		logger.info("updating description and/or metadata");
		super.updateDescriptionAndMetadata(document);
	}

	@Override
	public AutocompletionRules getAutoCompletionRules() {
		logger.info("getting autocompletion rules");
		return super.getAutoCompletionRules();
	}

	@Override
	public void clearCache() {
		logger.info("clearing internal cache");
		super.clearCache();
	}

}
