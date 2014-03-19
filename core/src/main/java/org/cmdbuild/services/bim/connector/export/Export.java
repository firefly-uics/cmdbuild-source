package org.cmdbuild.services.bim.connector.export;

import java.util.Map;

import org.cmdbuild.bim.model.Entity;


public interface Export {

	void setConfiguration(Object input);
	
	void setTarget(Object input, Output output);

	
	Map<String, Entity> getSourceData();
	
	Map<String, Entity> getTargetData();
	
	
	void export(String sourceProjectId, Output output);
	
		
	boolean isSynch(String sourceProjectId);

	String getLastGeneratedOutput(Object input);
	
}
