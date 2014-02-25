package org.cmdbuild.services.bim;

import org.cmdbuild.data.store.Storable;
import org.cmdbuild.data.store.Store;
import org.cmdbuild.model.bim.BimLayer;
import org.cmdbuild.model.bim.StorableProject;

public class DefaultBimStoreManager implements BimStoreManager {

	private final Store<StorableProject> projectInfoStore;
	private final Store<BimLayer> layerStore;

	public DefaultBimStoreManager(Store<StorableProject> projectInfoStore, Store<BimLayer> layerStore) {
		this.projectInfoStore = projectInfoStore;
		this.layerStore = layerStore;
	}

	@Override
	public Iterable<StorableProject> readAll() {
		return projectInfoStore.list();
	}

	@Override
	public StorableProject read(final String identifier) {
		return projectInfoStore.read(new Storable() {
			@Override
			public String getIdentifier() {
				return identifier;
			}
		});
	}

	@Override
	public void write(StorableProject project) {
		StorableProject projectAlreadyStored = projectInfoStore.read(storableWithId(project.getProjectId()));
		if (projectAlreadyStored != null) {
			project.setName(projectAlreadyStored.getName());
			if (project.getLastCheckin() == null) {
				project.setLastCheckin(projectAlreadyStored.getLastCheckin());
			}
			projectInfoStore.update(project);
		} else {
			projectInfoStore.create(project).getIdentifier();
		}

	}

	private Storable storableWithId(final String identifier) {
		return new Storable() {

			@Override
			public String getIdentifier() {
				return identifier;
			}

		};
	}

	@Override
	public void disableProject(String identifier) {
		StorableProject projectToEnable = read(identifier);
		projectToEnable.setActive(false);
		write(projectToEnable);
	}

	@Override
	public void enableProject(String identifier) {
		StorableProject projectToEnable = read(identifier);
		projectToEnable.setActive(true);
		write(projectToEnable);
	}

}
