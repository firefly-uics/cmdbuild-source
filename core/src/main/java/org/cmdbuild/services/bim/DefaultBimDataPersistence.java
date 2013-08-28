package org.cmdbuild.services.bim;

import java.util.List;

import org.cmdbuild.data.store.Store;
import org.cmdbuild.model.bim.BimMapperInfo;
import org.cmdbuild.model.bim.BimProjectInfo;

public class DefaultBimDataPersistence implements BimDataPersistence {
	
	private final Store<BimProjectInfo> projectInfoStore;
	private final Store<BimMapperInfo> mapperInfoStore;
	
	public DefaultBimDataPersistence(Store<BimProjectInfo> projectInfoStore, Store<BimMapperInfo> mapperInfoStore){
		this.projectInfoStore = projectInfoStore;
		this.mapperInfoStore = mapperInfoStore;
	}
	
	@Override
	public void store(BimProjectInfo projectInfo) {
		projectInfoStore.update(projectInfo);
	}

	@Override
	public void disableProject(String projectId) {
		// TODO Auto-generated method stub

	}

	@Override
	public void enableProject(String projectId) {
		// TODO Auto-generated method stub

	}

	@Override
	public List<BimProjectInfo> readBimProjectInfo() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public BimProjectInfo fetch(String identifier) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<BimMapperInfo> readBimMapperInfo() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void store(BimMapperInfo mapperInfo) {
		// TODO Auto-generated method stub

	}

	@Override
	public void create(BimMapperInfo _mapperInfo) {
		// TODO Auto-generated method stub

	}

	@Override
	public void createOrUpdateMapperInfo(String className, String attribute, String value) {
		// TODO Auto-generated method stub

	}

	@Override
	public BimMapperInfo fetchMapperInfo(String className) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setActive(String className, String value) {
		// TODO Auto-generated method stub

	}

	@Override
	public BimMapperInfo findBimRoot() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setBimRootOnClass(String className, boolean value) {
		// TODO Auto-generated method stub

	}

	@Override
	public void createBimRootOnClass(String className) {
		// TODO Auto-generated method stub

	}

}
