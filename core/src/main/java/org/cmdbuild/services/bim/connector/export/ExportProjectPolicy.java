package org.cmdbuild.services.bim.connector.export;


public interface ExportProjectPolicy {
	
	/**
	 * returns the id of the project for export
	 * **/
	String createProjectForExport(String projectId);
	
	/**
	 * returns the id of the project for export
	 * **/
	String updateProjectForExport(String projectId);

	void beforeExport(String exportProjectId);
	
}

