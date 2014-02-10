package org.cmdbuild.services.bim.connector.export;

import org.cmdbuild.bim.model.Catalog;

public interface Export {

	String export(Catalog catalog, String sourceProjectId);

}
