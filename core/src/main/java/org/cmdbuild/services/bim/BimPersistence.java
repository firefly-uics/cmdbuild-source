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

		String getShapeProjectId();
		
		void setExportProjectId(String projectId);

	}
	
	void saveProject(CmProject project);

	Iterable<CmProject> readAll();

	CmProject read(String projectId);
	
	void disableProject(CmProject project);

	void enableProject(CmProject project);

	

	Iterable<BimLayer> listLayers();

	void saveActiveFlag(String className, String value);

	void saveRootFlag(String className, boolean value);
	
	void saveExportFlag(String className, String value);
	
	void saveContainerFlag(String className, String value);

	void saveRootReferenceName(String className, String value);

	BimLayer findRoot();

	BimLayer findContainer();


	String getProjectIdFromCardId(Long cardId);

	Long getCardIdFromProjectId(String projectId);

	boolean isActiveLayer(String classname);

	String getContainerClassName();

	BimLayer readLayer(String className);

}
