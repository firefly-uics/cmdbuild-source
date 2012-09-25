package org.cmdbuild.dms;

import java.util.List;

public interface Document {

	String getClassName();

	int getCardId();

	List<String> getPath();

}
