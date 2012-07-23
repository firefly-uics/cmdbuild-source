package org.cmdbuild.dms.documents;

import java.io.InputStream;

public interface StorableDocument extends Document {

	String getAuthor();

	InputStream getInputStream();

	String getFileName();

	String getCategory();

	String getDescription();

}
