package org.cmdbuild.elements.interfaces;

import java.io.InputStream;

import org.cmdbuild.workflow.CmdbuildProcessInfo;
import org.cmdbuild.workflow.operation.ActivityDO;


public interface ProcessType extends ITable {

	interface XPDLManager {
		public byte[] download(int version);
		public void upload(InputStream inputStream, boolean userStoppable);
	}

	static final String BaseTable = "Activity";

	ProcessFactory cards();
	XPDLManager getXPDLManager();

	// TODO

	CmdbuildProcessInfo getProcInfo();
	ActivityDO startActivityTemplate();
}
