package org.cmdbuild.services.bim;

import java.io.File;

import org.cmdbuild.bim.service.BimProject;
import org.cmdbuild.bim.service.BimRevision;
import org.cmdbuild.bim.service.BimService;
import org.cmdbuild.model.bim.BimProjectInfo;
import org.joda.time.DateTime;

public class DefaultBimServiceFacade implements BimServiceFacade {

	private final BimService service;

	public DefaultBimServiceFacade(BimService bimservice) {
		this.service = bimservice;
	}

	@Override
	public String create(final String projectName) {

		login();
		String projectId = service.createProject(projectName).getIdentifier();
		logout();

		return projectId;
	}

	@Override
	public DateTime update(final BimProjectInfo projectInfo, final File ifcFile) {

		login();

		updateStatus(projectInfo);

		String projectId = projectInfo.getIdentifier();
		service.checkin(projectInfo.getIdentifier(), ifcFile);
		final BimProject updatedProject = service.getProjectByPoid(projectId);
		final BimRevision lastRevision = service.getRevision(updatedProject.getLastRevisionId());
		DateTime checkinTimeStamp = new DateTime(lastRevision.getDate().getTime());

		logout();

		return checkinTimeStamp;
	}

	@Override
	public void disableProject(final String projectId) {

		login();
		service.disableProject(projectId);
		logout();

	}

	@Override
	public void enableProject(final String projectId) {

		login();
		service.enableProject(projectId);
		logout();

	}

	@Override
	public void update(final BimProjectInfo updatedProjectInfo) {

		login();
		updateStatus(updatedProjectInfo);
		logout();

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

	private void login() {
		service.connect();
		service.login();
	}

	private void logout() {
		service.logout();
	}

}
