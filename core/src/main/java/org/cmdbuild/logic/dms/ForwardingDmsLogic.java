package org.cmdbuild.logic.dms;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

import javax.activation.DataHandler;

import org.cmdbuild.dms.DocumentTypeDefinition;
import org.cmdbuild.dms.MetadataGroup;
import org.cmdbuild.dms.StoredDocument;
import org.cmdbuild.dms.exception.DmsError;
import org.cmdbuild.exception.CMDBException;
import org.cmdbuild.exception.DmsException;

public abstract class ForwardingDmsLogic implements DmsLogic {

	private final DmsLogic delegate;

	protected ForwardingDmsLogic(final DmsLogic delegate) {
		this.delegate = delegate;
	}

	@Override
	public String getCategoryLookupType() {
		return delegate.getCategoryLookupType();
	}

	@Override
	public DocumentTypeDefinition getCategoryDefinition(final String category) {
		return delegate.getCategoryDefinition(category);
	}

	@Override
	public Iterable<DocumentTypeDefinition> getCategoryDefinitions() throws DmsError {
		return delegate.getCategoryDefinitions();
	}

	@Override
	public Map<String, Map<String, String>> getAutoCompletionRulesByClass(final String classname) throws DmsException {
		return delegate.getAutoCompletionRulesByClass(classname);
	}

	@Override
	public List<StoredDocument> search(final String className, final Long cardId) {
		return delegate.search(className, cardId);
	}

	@Override
	public void upload(final String author, final String className, final Long cardId, final InputStream inputStream,
			final String fileName, final String category, final String description,
			final Iterable<MetadataGroup> metadataGroups) throws IOException, CMDBException {
		delegate.upload(author, className, cardId, inputStream, fileName, category, description, metadataGroups);
	}

	@Override
	public DataHandler download(final String className, final Long cardId, final String fileName) {
		return delegate.download(className, cardId, fileName);
	}

	@Override
	public void delete(final String className, final Long cardId, final String fileName) throws DmsException {
		delegate.delete(className, cardId, fileName);
	}

	@Override
	public void updateDescriptionAndMetadata(final String className, final Long cardId, final String filename,
			final String description, final Iterable<MetadataGroup> metadataGroups) {
		delegate.updateDescriptionAndMetadata(className, cardId, filename, description, metadataGroups);
	}

}