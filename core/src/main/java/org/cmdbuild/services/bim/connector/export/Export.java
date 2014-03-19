package org.cmdbuild.services.bim.connector.export;

import org.cmdbuild.services.bim.connector.Output;

public interface Export {

	String export(String sourceProjectId, Output output);
	
	boolean isSynch(String sourceProjectId);

	String getLastGeneratedOutput(String baseProjectId);

}
