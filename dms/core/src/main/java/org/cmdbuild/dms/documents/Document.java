package org.cmdbuild.dms.documents;

import java.util.List;

public interface Document {

	String getClassName();

	int getCardId();

	List<String> getPath();

}
