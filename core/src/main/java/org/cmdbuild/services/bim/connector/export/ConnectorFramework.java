package org.cmdbuild.services.bim.connector.export;

public interface ConnectorFramework {
	
	boolean isSynch(Object input);
	
	void executeSynchronization(Object input, Output output);

	Object getLastGeneratedOutput(Object input);
	
}
