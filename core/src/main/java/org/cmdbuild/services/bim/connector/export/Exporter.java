package org.cmdbuild.services.bim.connector.export;

import org.cmdbuild.bim.model.Catalog;
import org.cmdbuild.bim.model.EntityDefinition;

public interface Exporter {

	String export(EntityDefinition entityDefinition, String projectId);

	void export(Catalog catalog, String projectId);

}
