package org.cmdbuild.logic;

import java.io.File;
import java.util.List;

import org.cmdbuild.bim.service.BimProject;
import org.cmdbuild.bim.service.BimRevision;
import org.cmdbuild.bim.service.BimService;
import org.cmdbuild.data.store.DataViewStore;
import org.cmdbuild.data.store.Store.Storable;
import org.cmdbuild.model.bim.BimProjectInfo;
import org.joda.time.DateTime;

public class BIMLogic implements Logic {

	final private DataViewStore<BimProjectInfo> store;
	final private BimService bimService;

	public BIMLogic( //
			final DataViewStore<BimProjectInfo> store, //
			final BimService bimService //
		) {

		this.store = store;
		this.bimService = bimService;
	}

	public List<BimProjectInfo> read() {
		return store.list();
	}

	public BimProjectInfo create(final BimProjectInfo projectInfo, final File fileIFC) {
		login();

		final BimProject createdProject = bimService.createProject(projectInfo.getName());
		projectInfo.setProjectId(createdProject.getIdentifier());
		store.create(projectInfo);

		doUpdate(projectInfo, fileIFC);

		logout();

		return projectInfo;
	}

	public void update(final BimProjectInfo projectInfo, final File fileIFC) {
		login();
		doUpdate(projectInfo, fileIFC);
		logout();
	}

	public void enableProject(final String projectId) {
		// update bim-server project
		login();
		bimService.enableProject(projectId);
		logout();

		// update in CMDBuild
		final BimProjectInfo fetchedProject = fetchProject(projectId);
		fetchedProject.setActive(true);
		store.update(fetchedProject);
	}

	public void disableProject(final String projectId) {
		// update bim-server project
		login();
		bimService.disableProject(projectId);
		logout();

		// update in CMDBuild
		final BimProjectInfo fetchedProject = fetchProject(projectId);
		fetchedProject.setActive(false);
		store.update(fetchedProject);
	}

	private void doUpdate(final BimProjectInfo projectInfo, final File fileIFC) {
		final BimProjectInfo fetchedProject = fetchProject(projectInfo.getIdentifier());

		// enable or disable if needed
		if (fetchedProject.isActive() != projectInfo.isActive()) {
			if (projectInfo.isActive()) {
				bimService.enableProject(projectInfo.getIdentifier());
			} else {
				bimService.disableProject(projectInfo.getIdentifier());
			}

			fetchedProject.setActive(projectInfo.isActive());
		}

		uploadIFCAndUpdateProjectInfo( //
				projectInfo.getIdentifier(), //
				fileIFC, //
				fetchedProject //
			);

		// update on CMDBuild
		store.update(fetchedProject);
	}

	private BimProjectInfo fetchProject(final String projectId) {
		return store.read(storeableWithId(projectId));
	}

	private void login() {
		bimService.connect();
		bimService.login();
	}

	private void logout() {
		bimService.logout();
	}

	/*
	 * The service must be already connected
	 * and logged in
	 */
	private void uploadIFCAndUpdateProjectInfo( //
			final String projectId, //
			final File fileIFC, //
			final BimProjectInfo projectInfo) {

		// do the first check-in if there
		// is a IFC file
		if (fileIFC != null) {
			bimService.checkin(projectId, fileIFC);
			// retrieve the last check-in date to add it
			// to the info to store in CMDBuild
			final BimProject updatedProject = bimService.getProjectByPoid(projectId);
			final BimRevision lastRevision = bimService.getRevision(updatedProject.getLastRevisionId());
			projectInfo.setLastCheckin(new DateTime(lastRevision.getDate().getTime()));
		}
	}

	private Storable storeableWithId(final String projectId) {
		return new Storable() {

			@Override
			public String getIdentifier() {
				return projectId;
			}

		};
	}
}
