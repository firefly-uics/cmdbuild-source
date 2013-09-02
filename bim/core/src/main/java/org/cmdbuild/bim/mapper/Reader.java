package org.cmdbuild.bim.mapper;

import java.util.EventListener;

import org.cmdbuild.bim.model.Entity;

public interface Reader {

	interface ReaderListener extends EventListener {
		
		void retrieved(Entity entity);

	}

	Iterable<Entity> read(String revisionId);
	
	Iterable<Entity> readEntities(String revisionId, int i);

	void read(String revisionId, ReaderListener listener);
	
	void read(String revisionId, ReaderListener listener, int i);
	
	int getNumberOfEntititesDefinitions();
	
	String getCmdbClass(int i);
	
	String getIfcType(int i);

}
