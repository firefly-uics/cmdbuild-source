package org.cmdbuild.services.bim;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import org.cmdbuild.bim.model.Entity;
import org.cmdbuild.bim.service.BimError;
import org.cmdbuild.data.store.Storable;
import org.cmdbuild.data.store.Store;
import org.cmdbuild.model.bim.BimLayer;
import org.cmdbuild.model.bim.BimProjectInfo;

public class DefaultBimDataPersistence implements BimDataPersistence {

	private final Store<BimProjectInfo> projectInfoStore;
	private final Store<BimLayer> layerStore;

	public DefaultBimDataPersistence(Store<BimProjectInfo> projectInfoStore, Store<BimLayer> layerStore) {
		this.projectInfoStore = projectInfoStore;
		this.layerStore = layerStore;
	}

	/**
	 * This method updates only active, description and lastCheckin attributes
	 * */
	@Override
	public void saveProject(BimProjectInfo projectInfoWithUpdatedValues) {
		BimProjectInfo toUpdateProjectInfo = fetchProject(projectInfoWithUpdatedValues.getIdentifier());
		if (toUpdateProjectInfo != null) {
			toUpdateProjectInfo.setActive(projectInfoWithUpdatedValues.isActive());
			toUpdateProjectInfo.setDescription(projectInfoWithUpdatedValues.getDescription());
			toUpdateProjectInfo.setLastCheckin(projectInfoWithUpdatedValues.getLastCheckin());
			projectInfoStore.update(toUpdateProjectInfo);
		} else {
			projectInfoStore.create(projectInfoWithUpdatedValues);
		}
	}

	@Override
	public void disableProject(String projectId) {
		BimProjectInfo projectInfo = fetchProject(projectId);
		projectInfo.setActive(false);
		projectInfoStore.update(projectInfo);
	}

	@Override
	public void enableProject(String projectId) {
		BimProjectInfo projectInfo = fetchProject(projectId);
		projectInfo.setActive(true);
		projectInfoStore.update(projectInfo);
	}

	@Override
	public List<BimProjectInfo> listProjectInfo() {
		return projectInfoStore.list();
	}
	
	
	

	@Override
	public List<BimLayer> listLayers() {
		return layerStore.list();
	}

	@Override
	public void saveActiveStatus(String className, String value) {
		BimLayer layerForClass = fetchLayer(className);
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
	public void saveExportStatus(String className, String value) {
		BimLayer layerForClass = fetchLayer(className);
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
		BimLayer layerForClass = fetchLayer(className);
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
	public void saveRoot(String className, boolean value) {
		BimLayer layer = fetchLayer(className);
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

	private BimLayer fetchLayer(final String className) {
		try {
			return layerStore.read(storableWithId(className));
		} catch (NoSuchElementException e) {
			return null;
		}
	}

	private BimProjectInfo fetchProject(final String projectId) {
		try {
			return projectInfoStore.read(storableWithId(projectId));
		} catch (NoSuchElementException e) {
			return null;
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
	public BimProjectInfo fetchProjectInfo(String projectId) {
		return projectInfoStore.read(storableWithId(projectId));
	}

	@Override
	public void setSynchronized(BimProjectInfo projectInfo, boolean isSynch) {
		projectInfo.setSynch(isSynch);
		projectInfoStore.update(projectInfo);
	}

	@Override
	public String getProjectIdFromCardId(Long cardId) {
		List<BimProjectInfo> bimProjectInfoList = listProjectInfo();
		for(BimProjectInfo projectInfo : bimProjectInfoList){
			if(projectInfo.getCardId().equals(cardId)){
				return projectInfo.getProjectId();
			}
		}
		return null;
	}

	@Override
	public boolean getActiveForClassname(String classname) {
		boolean response = false;
		BimLayer layer = fetchLayer(classname);
		if(layer != null){
			response = layer.isActive();
		}
		return response;
	}

	@Override
	public String getContainerClassName() {
		BimLayer containerLayer = findContainer();
		if(containerLayer == null){
			throw new BimError("Container layer not configured");
		}else{
			return containerLayer.getClassName();
		}
	}

}
