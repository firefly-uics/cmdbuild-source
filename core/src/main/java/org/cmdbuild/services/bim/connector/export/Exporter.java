package org.cmdbuild.services.bim.connector.export;

import org.cmdbuild.bim.model.Catalog;

public interface Exporter {

	void export(Catalog catalog, String projectId);

}
