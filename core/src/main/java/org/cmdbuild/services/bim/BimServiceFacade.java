package org.cmdbuild.services.bim;

import java.io.File;

import org.cmdbuild.model.bim.BimProjectInfo;
import org.joda.time.DateTime;

public interface BimServiceFacade {

	void store(BimProjectInfo projectInfo, File ifcFile);

	DateTime upload(BimProjectInfo projectInfo, File ifcFile);

	String create(String string);

	void disableProject(String projectId);

	void enableProject(String projectId);

	String fetch(String name);

	void update(BimProjectInfo updatedProjectInfo);

}
