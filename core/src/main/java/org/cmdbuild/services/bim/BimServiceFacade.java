package org.cmdbuild.services.bim;

import java.io.File;

import org.cmdbuild.bim.model.Entity;
import org.cmdbuild.model.bim.BimProjectInfo;
import org.joda.time.DateTime;

public interface BimServiceFacade {

	DateTime update(BimProjectInfo projectInfo, File ifcFile);

	String create(String projectName);

	void disableProject(String projectId);

	void enableProject(String projectId);

	void update(BimProjectInfo updatedProjectInfo);

	Iterable<Entity> readFrom(BimProjectInfo projectInfo);

}
