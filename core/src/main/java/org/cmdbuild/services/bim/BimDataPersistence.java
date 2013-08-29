package org.cmdbuild.services.bim;

import java.util.List;

import org.cmdbuild.model.bim.BimMapperInfo;
import org.cmdbuild.model.bim.BimProjectInfo;

public interface BimDataPersistence {

	void saveProject(BimProjectInfo projectInfo);

	void disableProject(String projectId);

	void enableProject(String projectId);

	List<BimProjectInfo> listProjectInfo();

	List<BimMapperInfo> listMapperInfo();

	void saveActiveStatus(String className, String value);

	BimMapperInfo findRoot();

	void saveRoot(String className, boolean value);

}
