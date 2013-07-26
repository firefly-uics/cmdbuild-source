package org.cmdbuild.dms;

import java.io.InputStream;

public interface DocumentCreator {

	DocumentSearch createDocumentSearch( //
			String className, //
			int cardId);

	StorableDocument createStorableDocument( //
			String author, //
			String className, //
			int cardId, //
			InputStream inputStream, //
			String fileName, //
			String category, //
			String description);

	StorableDocument createStorableDocument( //
			String author, //
			String className, //
			int cardId, //
			InputStream inputStream, //
			String fileName, //
			String category, //
			String description, //
			Iterable<MetadataGroup> metadataGroups);

	DocumentDownload createDocumentDownload( //
			String className, //
			int cardId, //
			String fileName);

	DocumentDelete createDocumentDelete( //
			String className, //
			int cardId, //
			String fileName);

	DocumentUpdate createDocumentUpdate( //
			String className, //
			int cardId, //
			String filename, //
			String description);

	DocumentUpdate createDocumentUpdate( //
			String className, //
			int cardId, //
			String filename, //
			String description, //
			Iterable<MetadataGroup> metadataGroups);

}
