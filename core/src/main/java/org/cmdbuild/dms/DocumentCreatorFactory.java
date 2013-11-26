package org.cmdbuild.dms;

import org.cmdbuild.dao.entrytype.CMClass;

public interface DocumentCreatorFactory {

	DocumentCreator create(String name);

	DocumentCreator create(CMClass target);

}
