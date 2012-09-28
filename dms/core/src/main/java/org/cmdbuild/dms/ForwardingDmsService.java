package org.cmdbuild.dms;

import java.util.List;

import javax.activation.DataHandler;

import org.cmdbuild.dms.MetadataAutocompletion.AutocompletionRules;
import org.cmdbuild.dms.exception.DmsException;

public class ForwardingDmsService implements DmsService {

	private final DmsService dmsService;

	public ForwardingDmsService(final DmsService dmsService) {
		this.dmsService = dmsService;
	}

	@Override
	public DmsConfiguration getConfiguration() {
		return dmsService.getConfiguration();
	}

	@Override
	public void setConfiguration(final DmsConfiguration configuration) {
		dmsService.setConfiguration(configuration);
	}

	@Override
	public Iterable<DocumentTypeDefinition> getTypeDefinitions() {
		return dmsService.getTypeDefinitions();
	}

	@Override
	public List<StoredDocument> search(final DocumentSearch document) {
		return dmsService.search(document);
	}

	@Override
	public void upload(final StorableDocument document) throws DmsException {
		dmsService.upload(document);
	}

	@Override
	public DataHandler download(final DocumentDownload document) throws DmsException {
		return dmsService.download(document);
	}

	@Override
	public void delete(final DocumentDelete document) throws DmsException {
		dmsService.delete(document);
	}

	@Override
	public void updateDescriptionAndMetadata(final DocumentUpdate document) throws DmsException {
		dmsService.updateDescriptionAndMetadata(document);
	}

	@Override
	public AutocompletionRules getAutoCompletionRules() {
		return dmsService.getAutoCompletionRules();
	}

	@Override
	public void clearCache() {
		dmsService.clearCache();
	}

}
