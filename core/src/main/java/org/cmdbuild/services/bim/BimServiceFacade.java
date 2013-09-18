package org.cmdbuild.services.bim;

import java.io.File;
import java.util.List;

import org.cmdbuild.bim.model.Entity;
import org.cmdbuild.bim.model.EntityDefinition;
import org.cmdbuild.model.bim.BimProjectInfo;
import org.joda.time.DateTime;

public interface BimServiceFacade {

	DateTime updateProject(BimProjectInfo projectInfo, File ifcFile);

	String createProject(String projectName);

	void disableProject(String projectId);

	void enableProject(String projectId);

	void updateProject(BimProjectInfo updatedProjectInfo);
	
	List<Entity> readEntityFromProject(EntityDefinition entityDefinition, BimProjectInfo projectInfo);

}
