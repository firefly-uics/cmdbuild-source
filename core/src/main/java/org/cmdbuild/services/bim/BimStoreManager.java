package org.cmdbuild.services.bim;

import org.cmdbuild.model.bim.BimLayer;
import org.cmdbuild.model.bim.StorableProject;

public interface BimStoreManager {
	
	Iterable<StorableProject> readAll();
	
	StorableProject read(final String identifier);

	void write(final StorableProject project);
	
	void disableProject(final String identifier);
	
	void enableProject(final String identifier);

	Iterable<BimLayer> readAllLayers();
	
	void saveActiveStatus(String className, String value);

	void saveRoot(String className, boolean value);
	
	void saveExportStatus(String className, String value);
	
	void saveContainerStatus(String className, String value);
	
	boolean isActive(String className);
	
	String getContainerClassName();

	BimLayer findRoot();

	BimLayer findContainer();

	BimLayer readLayer(String className);
	
}
