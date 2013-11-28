package org.cmdbuild.dms;

import org.cmdbuild.dao.entrytype.CMClass;

public interface DocumentCreatorFactory {

	DocumentCreator create(Iterable<String> path);

	DocumentCreator create(CMClass target);

}
