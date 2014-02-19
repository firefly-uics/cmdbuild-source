package org.cmdbuild.services.bim;

import java.util.List;

import org.cmdbuild.model.bim.BimLayer;
import org.cmdbuild.model.bim.BimProjectInfo;

public interface BimDataPersistence {

	void saveProject(BimProjectInfo projectInfo);

	void disableProject(String projectId);

	void enableProject(String projectId);

	BimProjectInfo fetchProjectInfo(String projectId);

	List<BimProjectInfo> listProjectInfo();

	List<BimLayer> listLayers();

	void saveActiveStatus(String className, String value);

	BimLayer findRoot();

	BimLayer findContainer();

	void saveRoot(String className, boolean value);

	void setSynchronized(BimProjectInfo projectInfo, boolean isSynch);

	void saveExportStatus(String className, String value);

	void saveContainerStatus(String className, String value);
	
	String getProjectIdFromCardId(Long cardId);

	boolean getActiveForClassname(String classname);

	String getContainerClassName();

}
