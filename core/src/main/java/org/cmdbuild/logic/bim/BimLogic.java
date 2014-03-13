package org.cmdbuild.logic.bim;

import java.io.File;
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

	List<BimLayer> readBimLayer();

	void updateBimLayer(String className, String attributeName, String value);

	void importIfc(String projectId);

	void exportIfc(String sourceProjectId);

	boolean isSynchForExport(String projectId);

	BimLayer getRootLayer();

	BimCard fetchCardDataFromObjectId(String objectId, String revisionId);

	String fetchJsonForBimViewer(String revisionId);

	boolean getActiveForClassname(String classname);

	String getLastRevisionOfProject(String exportProjectId);

	// methods for the viewer

	String getDescriptionOfRoot(Long cardId, String className);

	String getBaseRevisionIdForViewer(Long cardId, String className);

	String getExportedRevisionIdForViewer(Long cardId, String className);

}