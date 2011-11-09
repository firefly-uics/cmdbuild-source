package org.cmdbuild.dms.alfresco.webservice;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

abstract class AbstractSearchCommand<T> extends AlfrescoWebserviceCommand<T> {

	protected final Logger logger = LoggerFactory.getLogger(getClass());

}
