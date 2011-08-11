package org.cmdbuild.dms.alfresco.webservice;

import java.util.Collection;

import org.cmdbuild.dms.DocumentSearch;
import org.cmdbuild.elements.TableImpl;
import org.cmdbuild.exception.NotFoundException;
import org.cmdbuild.logger.Log;

abstract class AbstractSearchCommand<T> extends AlfrescoWebserviceCommand<T> {

	protected static String[] path(final DocumentSearch search) throws NotFoundException {
		final Collection<String> classWithAncestors = TableImpl.tree().path(search.getClassName());
		final String[] path = new String[classWithAncestors.size() + 1];
		classWithAncestors.toArray(path);
		path[classWithAncestors.size()] = "Id" + search.getCardId();
		Log.DMS.debug("Requested path " + path);
		return path;
	}

}
