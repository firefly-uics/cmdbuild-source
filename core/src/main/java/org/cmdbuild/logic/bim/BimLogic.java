package org.cmdbuild.logic.bim;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.activation.DataHandler;

import org.cmdbuild.logic.Logic;
import org.cmdbuild.model.bim.BimLayer;
import org.cmdbuild.services.bim.connector.DefaultBimDataView.BimCard;
import org.joda.time.DateTime;

public interface BimLogic extends Logic {

	public static interface Project {
	
		String getProjectId();
	
		String getName();
	
		String getDescription();
	
		boolean isActive();
	
		boolean isSynch();
	
		String getImportMapping();
	
		String getExportMapping();
	
		DateTime getLastCheckin();
		
		Iterable<String> getCardBinding();

		File getFile();
	
	}
	
	Project createProject(Project project);
	
	Iterable<Project> readAllProjects();
	
	void updateProject(Project project);
	
	DataHandler download(String projectId);
	
	void enableProject(Project project);
	
	void disableProject(Project project);

	String getProjectId(Long cardId);
	
	
	List<BimLayer> readBimLayer();

	void updateBimLayer(String className, String attributeName, String value);

	void bindProjectToCards(String projectId, ArrayList<String> cardsId);

	String getPoidForCardId(Long cardId);

	String getLastRevisionIdFromCmCardId(Long cardId);

	Iterable<String> readCardsBindedToProject(String projectId, String className);

	void importIfc(String projectId);

	void exportIfc(String sourceProjectId);


	BimLayer getRootLayer();

	BimCard fetchCardDataFromObjectId(String objectId, String revisionId);

	String fetchJsonForBimViewer(String revisionId);

	boolean getActiveForClassname(String classname);



}