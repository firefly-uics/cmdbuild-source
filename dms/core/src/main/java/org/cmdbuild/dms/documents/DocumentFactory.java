package org.cmdbuild.dms.documents;

import java.io.InputStream;

public interface DocumentFactory {

	public DocumentSearch createDocumentSearch(final String className, final int cardId);

	public StorableDocument createStorableDocument(final String author, final String className, final int cardId,
			final InputStream inputStream, final String fileName, final String category, final String description);

	public DocumentDownload createDocumentDownload(final String className, final int cardId, final String fileName);

	public DocumentDelete createDocumentDelete(final String className, final int cardId, final String fileName);

	public DocumentUpdate createDocumentUpdate(final String className, final int cardId, final String filename,
			final String description);

}