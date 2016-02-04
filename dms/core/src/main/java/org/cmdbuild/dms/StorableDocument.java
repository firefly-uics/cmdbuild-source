package org.cmdbuild.dms;

import java.io.InputStream;

public interface StorableDocument extends DocumentUpdate {

	String getAuthor();

	InputStream getInputStream();

}
