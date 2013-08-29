package org.cmdbuild.services.bim;

import java.util.List;
import java.util.NoSuchElementException;

import org.cmdbuild.data.store.Store;
import org.cmdbuild.data.store.Store.Storable;
import org.cmdbuild.model.bim.BimMapperInfo;
import org.cmdbuild.model.bim.BimProjectInfo;

public class DefaultBimDataPersistence implements BimDataPersistence {

	private final Store<BimProjectInfo> projectInfoStore;
	private final Store<BimMapperInfo> mapperInfoStore;

	public DefaultBimDataPersistence(Store<BimProjectInfo> projectInfoStore, Store<BimMapperInfo> mapperInfoStore) {
		this.projectInfoStore = projectInfoStore;
		this.mapperInfoStore = mapperInfoStore;
	}

	@Override //update only active, description and lastCheckin attributes
	public void saveProject(BimProjectInfo projectInfo) {
		BimProjectInfo toUpdateProjectInfo = fetchProject(projectInfo.getIdentifier());
		if(toUpdateProjectInfo != null){
			toUpdateProjectInfo.setActive(projectInfo.isActive());
			toUpdateProjectInfo.setDescription(projectInfo.getDescription());
			toUpdateProjectInfo.setLastCheckin(projectInfo.getLastCheckin());
			projectInfoStore.update(toUpdateProjectInfo);
		}else{
			projectInfoStore.create(projectInfo);
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
	public List<BimMapperInfo> listMapperInfo() {
		return mapperInfoStore.list();
	}

	@Override
	public void saveActiveStatus(String className, String value) {
		BimMapperInfo mapperForClass = fetchMapper(className);
		if (mapperForClass == null) {
			mapperForClass = new BimMapperInfo(className);
			mapperForClass.setActive(Boolean.parseBoolean(value));
			mapperInfoStore.create(mapperForClass);
		} else {
			mapperForClass.setActive(Boolean.parseBoolean(value));
			mapperInfoStore.update(mapperForClass);
		}
	}

	@Override
	public void saveRoot(String className, boolean value) {
		BimMapperInfo mapperInfo = fetchMapper(className);
		if (mapperInfo == null) {
			mapperInfo = new BimMapperInfo(className);
			mapperInfo.setBimRoot(value);
			mapperInfoStore.create(mapperInfo);
		} else {
			mapperInfo.setBimRoot(value);
			mapperInfoStore.update(mapperInfo);
		}
	}
	

	@Override
	public BimMapperInfo findRoot() {
		List<BimMapperInfo> mapperList = mapperInfoStore.list();
		for (BimMapperInfo mapper : mapperList) {
			if (mapper.isBimRoot()) {
				return mapper;
			}
		}
		return null;
	}

	private BimMapperInfo fetchMapper(final String className) {
		try {
			return mapperInfoStore.read(storeableWithId(className));
		} catch (NoSuchElementException e) {
			return null;
		}
	}

	private BimProjectInfo fetchProject(final String projectId) {
		try {
			return projectInfoStore.read(storeableWithId(projectId));
		} catch (NoSuchElementException e) {
			return null;
		}
	}

	private Storable storeableWithId(final String identifier) {
		return new Storable() {

			@Override
			public String getIdentifier() {
				return identifier;
			}

		};
	}

}
