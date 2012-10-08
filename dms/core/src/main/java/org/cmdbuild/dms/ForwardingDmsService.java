package org.cmdbuild.dms;

import java.util.List;

import javax.activation.DataHandler;

import org.cmdbuild.dms.MetadataAutocompletion.AutocompletionRules;
import org.cmdbuild.dms.exception.DmsError;

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
	public Iterable<DocumentTypeDefinition> getTypeDefinitions() throws DmsError {
		return dmsService.getTypeDefinitions();
	}

	@Override
	public List<StoredDocument> search(final DocumentSearch document) throws DmsError {
		return dmsService.search(document);
	}

	@Override
	public void upload(final StorableDocument document) throws DmsError {
		dmsService.upload(document);
	}

	@Override
	public DataHandler download(final DocumentDownload document) throws DmsError {
		return dmsService.download(document);
	}

	@Override
	public void delete(final DocumentDelete document) throws DmsError {
		dmsService.delete(document);
	}

	@Override
	public void updateDescriptionAndMetadata(final DocumentUpdate document) throws DmsError {
		dmsService.updateDescriptionAndMetadata(document);
	}

	@Override
	public AutocompletionRules getAutoCompletionRules() throws DmsError {
		return dmsService.getAutoCompletionRules();
	}

	@Override
	public void clearCache() {
		dmsService.clearCache();
	}

}
