package org.cmdbuild.elements.interfaces;

import org.cmdbuild.workflow.CmdbuildProcessInfo;
import org.cmdbuild.workflow.operation.ActivityDO;


public interface ProcessType extends ITable {

	static final String BaseTable = "Activity";

	ProcessFactory cards();

	// TODO

	CmdbuildProcessInfo getProcInfo();
	ActivityDO startActivityTemplate();
}
