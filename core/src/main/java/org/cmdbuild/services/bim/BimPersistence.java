package org.cmdbuild.services.bim;

import org.cmdbuild.model.bim.BimLayer;
import org.joda.time.DateTime;

public interface BimPersistence {

	public interface CmProject {

		String getProjectId();

		String getName();

		String getDescription();
		
		Long getCmId();

		boolean isActive();

		boolean isSynch();

		String getImportMapping();

		String getExportMapping();

		String getExportProjectId();

		DateTime getLastCheckin();

		Iterable<String> getCardBinding();

		void setSynch(boolean synch);

		void setProjectId(String projectId);

		void setLastCheckin(DateTime lastCheckin);

		void setName(String name);

		void setDescription(String description);

		void setCardBinding(Iterable<String> cardBinding);

		void setActive(boolean active);

	}
	
	void saveProject(CmProject project);

	Iterable<CmProject> readAll();

	CmProject read(String projectId);
	
	void disableProject(CmProject project);

	void enableProject(CmProject project);

	

	Iterable<BimLayer> listLayers();

	void saveActiveStatus(String className, String value);

	void saveRoot(String className, boolean value);
	
	void saveExportStatus(String className, String value);
	
	void saveContainerStatus(String className, String value);

	BimLayer findRoot();

	BimLayer findContainer();


	String getProjectIdFromCardId(Long cardId);

	Long getCardIdFromProjectId(String projectId);

	boolean isActiveLayer(String classname);

	String getContainerClassName();

	BimLayer readLayer(String className);






}
