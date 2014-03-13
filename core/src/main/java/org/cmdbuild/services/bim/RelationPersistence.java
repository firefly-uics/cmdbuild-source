package org.cmdbuild.services.bim;

public interface RelationPersistence {

	public interface ProjectRelations {

		Long getProjectCardId();

		Iterable<String> getBindedCards();

	}
	
	ProjectRelations readRelations(final Long projectCardId, final String rootClassName);
	
	void removeRelations(final Long projectCardId, final String rootClassName);

	void writeRelations(Long projectCardId, Iterable<String> cardBinding, String className);
	
}
