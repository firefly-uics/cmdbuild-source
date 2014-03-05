package org.cmdbuild.services.bim.connector.export;

import org.cmdbuild.bim.model.Catalog;
import org.cmdbuild.services.bim.connector.Output;

public interface Export {

	String export(Catalog catalog, String sourceProjectId, Output output);
	
	boolean isSynch(Catalog catalog, String sourceProjectId);

}
