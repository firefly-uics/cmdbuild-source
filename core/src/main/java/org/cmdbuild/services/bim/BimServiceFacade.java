package org.cmdbuild.services.bim;

import java.io.File;
import java.util.List;
import java.util.Map;

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

	String fetchShapeRevision(String shapeName);

	void writeCardIntoProject();

	List<Entity> fetchContainers(String projectId);

	void download(String projectId);

	void insertCard(Map<String, String> bimData, String projectId, String ifcType, String container);

	String commitTransaction(String projectId);

}
