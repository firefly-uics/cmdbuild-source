package org.cmdbuild.dms;

import java.io.InputStream;

public interface StorableDocument extends Document {

	String getAuthor();

	InputStream getInputStream();

	String getFileName();

	String getCategory();

	String getDescription();

	Iterable<MetadataGroup> getMetadataGroups();

}
