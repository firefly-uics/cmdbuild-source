package org.cmdbuild.services.bim;

import java.util.List;

import org.cmdbuild.model.bim.BimMapperInfo;
import org.cmdbuild.model.bim.BimProjectInfo;

public interface BimDataPersistence {

	void store(BimProjectInfo projectInfo);

	void disableProject(String projectId);

	void enableProject(String projectId);

	List<BimProjectInfo> readBimProjectInfo();

	BimProjectInfo fetch(String identifier);

	List<BimMapperInfo> readBimMapperInfo();

	void store(BimMapperInfo mapperInfo);

	void create(BimMapperInfo _mapperInfo);

	void createOrUpdateMapperInfo(String className, String attribute, String value);

	BimMapperInfo fetchMapperInfo(String className);

	void setActive(String className, String value);

	BimMapperInfo findBimRoot();

	void setBimRootOnClass(String className, boolean value);

	void createBimRootOnClass(String className);

}
