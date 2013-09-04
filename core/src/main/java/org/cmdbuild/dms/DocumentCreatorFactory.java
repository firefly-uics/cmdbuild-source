package org.cmdbuild.dms;

import org.cmdbuild.dao.entrytype.CMClass;

public interface DocumentCreatorFactory {

	void setClass(CMClass target);

	DocumentCreator create();

}
