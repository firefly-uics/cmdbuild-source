package org.cmdbuild.dms;

import java.util.List;

import javax.activation.DataHandler;

public interface DmsService {

	List<StoredDocument> search(DocumentSearch search);

	// FIXME better to remove return value and throws an exception
	boolean upload(StorableDocument storableDocument);

	DataHandler download(DocumentDownload download);

	// FIXME better to remove return value and throws an exception
	boolean delete(DocumentDelete delete);

	// FIXME better to remove return value and throws an exception
	boolean updateDescription(DocumentUpdate update);

}
