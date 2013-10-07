package org.cmdbuild.services.bim;

import java.io.File;
import java.util.List;

import org.cmdbuild.bim.mapper.Reader;
import org.cmdbuild.bim.mapper.xml.BimReader;
import org.cmdbuild.bim.model.Entity;
import org.cmdbuild.bim.model.EntityDefinition;
import org.cmdbuild.bim.service.BimError;
import org.cmdbuild.bim.service.BimProject;
import org.cmdbuild.bim.service.BimRevision;
import org.cmdbuild.bim.service.BimService;
import org.cmdbuild.model.bim.BimProjectInfo;
import org.joda.time.DateTime;

public class DefaultBimServiceFacade implements BimServiceFacade {

	private static final String IFC_SPACE = "IfcSpace";
	private final BimService service;
	private final Reader reader;
	
	public DefaultBimServiceFacade(BimService bimservice) {
		this.service = bimservice;
		reader = new BimReader(bimservice);
	}

	@Override
	public String createProject(final String projectName) {

		login();
		String projectId = service.createProject(projectName).getIdentifier();
		logout();

		return projectId;
	}

	@Override
	public void disableProject(final String projectId) {

		login();
		service.disableProject(projectId);
		logout();

	}

	@Override
	public void download(String projectId) {
		String revisionId = service.getProjectByPoid(projectId).getLastRevisionId();
		service.downloadIfc(revisionId);		
	}

	@Override
	public void enableProject(final String projectId) {

		login();
		service.enableProject(projectId);
		logout();

	}

	@Override
	public List<Entity> fetchContainers(String projectId) {
		login();
		String revisionId = service.getProjectByPoid(projectId).getLastRevisionId();
		List<Entity> entities =service.getEntitiesByType(revisionId, IFC_SPACE);
		logout();
		return entities;
	}

	@Override
	public String fetchShapeRevision(String shapeName) {
		throw new RuntimeException("fetchShapeRevision not implemented");
	}

	@Override
	public List<Entity> readEntityFromProject(EntityDefinition entityDefinition, BimProjectInfo projectInfo) {
		login();
		String revisionId = service.getProjectByPoid(projectInfo.getProjectId()).getLastRevisionId();
		List<Entity> source = reader.readEntities(revisionId, entityDefinition);
		logout();
		return source;
	}

	@Deprecated
	public BimService service(){
		return service;
	}

	@Override
	public void updateProject(final BimProjectInfo updatedProjectInfo) {

		login();
		updateStatus(updatedProjectInfo);
		logout();

	}

	@Override
	public DateTime updateProject(final BimProjectInfo projectInfo, final File ifcFile) {

		login();

		updateStatus(projectInfo);

		String projectId = projectInfo.getIdentifier();
		service.checkin(projectInfo.getIdentifier(), ifcFile);
		final BimProject updatedProject = service.getProjectByPoid(projectId);
		final BimRevision lastRevision = service.getRevision(updatedProject.getLastRevisionId());
		if (lastRevision == null) {
			throw new BimError("Upload failed");
		}
		DateTime checkinTimeStamp = new DateTime(lastRevision.getDate().getTime());
		logout();

		return checkinTimeStamp;
	}

	@Override
	public void writeCardIntoProject() {
		throw new RuntimeException("writeCardIntoProject not implemented");
	}

	private void login() {
	}

	private void logout() {
	}

	private void updateStatus(final BimProjectInfo projectInfo) {
		String projectId = projectInfo.getIdentifier();
		BimProject bimProject = service.getProjectByPoid(projectId);

		if (bimProject.isActive() != projectInfo.isActive()) {
			if (projectInfo.isActive()) {
				service.enableProject(projectId);
			} else {
				service.disableProject(projectId);
			}
		}
	}

}
