package org.cmdbuild.dms;

import java.util.List;

import javax.activation.DataHandler;

import org.cmdbuild.dms.MetadataAutocompletion.AutocompletionRules;
import org.cmdbuild.dms.exception.DmsException;

public interface DmsService {

	/**
	 * Gets the {@link DmsConfiguration}.
	 * 
	 * @return the actual {@link DmsConfiguration}.
	 */
	DmsConfiguration getConfiguration();

	/**
	 * Sets the {@link DmsConfiguration}.
	 */
	void setConfiguration(DmsConfiguration configuration);

	/**
	 * Gets all {@link DocumentTypeDefinition}s.
	 * 
	 * @return all {@link DocumentTypeDefinition}s.
	 */
	Iterable<DocumentTypeDefinition> getTypeDefinitions();

	/**
	 * Search for all documents matching the specified query.
	 * 
	 * @param document
	 *            the document query parameters.
	 * 
	 * @return the list found documents (never null).
	 */
	List<StoredDocument> search(DocumentSearch document);

	/**
	 * Upload the specified document.
	 * 
	 * @param document
	 *            the definition for the document that needs to be upload.
	 * 
	 * @throws DmsException
	 *             if something goes wrong.
	 */
	void upload(StorableDocument document) throws DmsException;

	/**
	 * Downloads the specified document.
	 * 
	 * @param document
	 *            the definition for the document that needs to be downloaded.
	 * 
	 * @return the {@link DataHandler} associated with the document.
	 * 
	 * @throws DmsException
	 *             if something goes wrong.
	 */
	DataHandler download(DocumentDownload document) throws DmsException;

	/**
	 * Deletes the specified document.
	 * 
	 * @param document
	 *            the definition for the document that needs to be deleted.
	 * 
	 * @throws DmsException
	 *             if something goes wrong.
	 */
	void delete(DocumentDelete document) throws DmsException;

	/**
	 * Updates the description of an existing document.
	 * 
	 * @param document
	 *            the definition for the document that needs to be updated.
	 * 
	 * @throws DmsException
	 */
	void updateDescription(DocumentUpdate document) throws DmsException;

	/**
	 * Gets the autocompletion rules.
	 * 
	 * return the autocompletion rules.
	 */
	AutocompletionRules getAutoCompletionRules();

	/**
	 * Clears cache (if supported).
	 */
	public void clearCache();

}
