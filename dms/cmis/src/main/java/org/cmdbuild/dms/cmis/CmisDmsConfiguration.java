package org.cmdbuild.dms.cmis;

import org.cmdbuild.dms.DmsConfiguration;

public interface CmisDmsConfiguration extends DmsConfiguration {

	String getServerURL();

	String getAlfrescoUser();

	String getAlfrescoPassword();

	Object getRepositoryWSPath();

	Object getRepositoryApp();

}
