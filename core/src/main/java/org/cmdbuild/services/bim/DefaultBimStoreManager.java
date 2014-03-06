package org.cmdbuild.services.bim;

import java.util.List;

import org.cmdbuild.bim.service.BimError;
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
	public BimLayer readLayer(final String className) {
		return layerStore.read(new Storable() {
			@Override
			public String getIdentifier() {
				return className;
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

	@Override
	public Iterable<BimLayer> readAllLayers() {
		return layerStore.list();
	}

	@Override
	public void saveActiveStatus(String className, String value) {
		BimLayer layerForClass = layerStore.read(storableWithId(className));
		if (layerForClass == null) {
			layerForClass = new BimLayer(className);
			layerForClass.setActive(Boolean.parseBoolean(value));
			layerStore.create(layerForClass);
		} else {
			layerForClass.setActive(Boolean.parseBoolean(value));
			layerStore.update(layerForClass);
		}
		
	}

	@Override
	public void saveRoot(String className, boolean value) {
		BimLayer layer = layerStore.read(storableWithId(className));
		if (layer == null) {
			layer = new BimLayer(className);
			layer.setRoot(value);
			layerStore.create(layer);
		} else {
			layer.setRoot(value);
			layerStore.update(layer);
		}
	}

	@Override
	public void saveExportStatus(String className, String value) {
		BimLayer layerForClass = layerStore.read(storableWithId(className));
		boolean exportValue = Boolean.parseBoolean(value);
		if (layerForClass == null) {
			layerForClass = new BimLayer(className);
			layerForClass.setExport(exportValue);
			layerStore.create(layerForClass);
		} else {
			layerForClass.setExport(exportValue);
			layerStore.update(layerForClass);
		}
	}

	@Override
	public void saveContainerStatus(String className, String value) {
		BimLayer layerForClass = layerStore.read(storableWithId(className));
		boolean containerValue = Boolean.parseBoolean(value);
		if (layerForClass == null) {
			layerForClass = new BimLayer(className);
			layerForClass.setContainer(containerValue);
			layerStore.create(layerForClass);
		} else {
			layerForClass.setContainer(containerValue);
			layerStore.update(layerForClass);
		}

	}

	@Override
	public BimLayer findRoot() {
		List<BimLayer> layerList = layerStore.list();
		for (BimLayer layer : layerList) {
			if (layer.isRoot()) {
				return layer;
			}
		}
		return null;
	}

	@Override
	public BimLayer findContainer() {
		List<BimLayer> layerList = layerStore.list();
		for (BimLayer layer : layerList) {
			if (layer.isContainer()) {
				return layer;
			}
		}
		return null;
	}

	@Override
	public boolean isActive(final String className) {
		boolean response = false;
		BimLayer layer = layerStore.read(storableWithId(className));
		if (layer != null) {
			response = layer.isActive();
		}
		return response;
	}

	@Override
	public String getContainerClassName() {
		BimLayer containerLayer = findContainer();
		if (containerLayer == null) {
			throw new BimError("Container layer not configured");
		} else {
			return containerLayer.getClassName();
		}
	}


}
