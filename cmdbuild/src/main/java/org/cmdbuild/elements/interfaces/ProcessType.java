package org.cmdbuild.elements.interfaces;

import java.io.InputStream;

import org.cmdbuild.workflow.utils.SimpleXMLDoc;
import org.cmdbuild.workflow.CmdbuildProcessInfo;
import org.cmdbuild.workflow.operation.ActivityDO;


public interface ProcessType extends ITable {

	interface XPDLManager {
		public byte[] download(int version);
		public void upload(InputStream inputStream, boolean userStoppable);
		public SimpleXMLDoc template(String[] users, String[] roles);
	}

	static final String BaseTable = "Activity";

	ProcessFactory cards();
	XPDLManager getXPDLManager();

	// TODO

	CmdbuildProcessInfo getProcInfo();
	Integer[] getPackageVersions();
	ActivityDO startActivityTemplate();
}
