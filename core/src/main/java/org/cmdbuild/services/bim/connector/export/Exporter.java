package org.cmdbuild.services.bim.connector.export;

import org.cmdbuild.bim.model.Catalog;

public interface Exporter {

	String export(Catalog catalog, String sourceProjectId);

}
