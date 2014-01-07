package org.cmdbuild.services.bim;

import java.util.ArrayList;
import java.util.List;

import org.cmdbuild.bim.model.Entity;

public interface BimDataModelManager {

	void createBimTableIfNeeded(String className);

	void deleteBimDomainOnClass(String oldClass);

	void createBimDomainOnClass(String className);

	void bindProjectToCards(String projectId, String className,
			ArrayList<String> cardsId);

	ArrayList<String> fetchCardsBindedToProject(String projectId,
			String className);
	
	@Deprecated
	void updateCardsFromSource(List<Entity> source) throws Exception;

	void addPositionFieldIfNeeded(String className);

	void addPerimeterAndHeightFieldsIfNeeded(String className);

}