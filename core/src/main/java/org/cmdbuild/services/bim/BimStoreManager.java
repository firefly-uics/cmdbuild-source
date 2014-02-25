package org.cmdbuild.services.bim;

import org.cmdbuild.model.bim.StorableProject;

public interface BimStoreManager {
	
	Iterable<StorableProject> readAll();
	
	StorableProject read(final String identifier);

	void write(final StorableProject project);
	
	void disableProject(final String identifier);
	
	void enableProject(final String identifier);

	
}
