package org.cmdbuild.services.bim;

import java.io.File;
import java.util.List;

import org.cmdbuild.bim.model.Entity;
import org.cmdbuild.bim.model.EntityDefinition;
import org.cmdbuild.model.bim.BimProjectInfo;
import org.joda.time.DateTime;

public interface BimServiceFacade {

	DateTime update(BimProjectInfo projectInfo, File ifcFile);

	String create(String projectName);

	void disableProject(String projectId);

	void enableProject(String projectId);

	void update(BimProjectInfo updatedProjectInfo);
	
	List<Entity> read(BimProjectInfo projectInfo, EntityDefinition entityDefinition);

}
