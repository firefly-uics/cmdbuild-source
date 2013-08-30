package org.cmdbuild.services.bim;

import java.util.ArrayList;

public interface BimDataModelManager {

	void createBimTableIfNeeded(String className);

	void deleteBimDomainOnClass(String oldClass);

	void createBimDomainOnClass(String className);

	void bindProjectToCards(String projectId, String className, ArrayList<String> cardsId);

	ArrayList<String> fetchCardsBindedToProject(String projectId, String className);

}