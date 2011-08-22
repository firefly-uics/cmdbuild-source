package org.cmdbuild.dms.alfresco.webservice;

import org.apache.log4j.Logger;

abstract class AbstractSearchCommand<T> extends AlfrescoWebserviceCommand<T> {

	protected final Logger logger = Logger.getLogger(getClass());

}
