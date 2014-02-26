package org.cmdbuild.services.bim;

import java.util.List;

import org.cmdbuild.bim.model.Entity;
import org.cmdbuild.services.bim.BimPersistence.CmProject;

public interface BimDataModelManager {

	void createBimTableIfNeeded(String className);

	void deleteBimDomainOnClass(String oldClass);

	void createBimDomainOnClass(String className);
	
	public void bindProjectToCards(String projectCardId, String className, Iterable<String> cardsToBind);

	Iterable<String> readCardsBindedToProject(String projectId,
			String className);
	
	@Deprecated
	void updateCardsFromSource(List<Entity> source) throws Exception;

	void addPositionFieldIfNeeded(String className);

	void addPerimeterAndHeightFieldsIfNeeded(String className);

	void saveCardBinding(CmProject persistenceProject);

}