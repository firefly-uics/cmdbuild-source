package org.cmdbuild.bim.service.bimserver;

import java.io.File;
import java.io.FileOutputStream;
import java.util.List;
import java.util.Map;

import javax.activation.DataHandler;

import org.cmdbuild.bim.model.Entity;
import org.cmdbuild.bim.service.BimProject;
import org.cmdbuild.bim.service.BimRevision;
import org.cmdbuild.bim.service.ReferenceAttribute;

public interface BimserverClient {

	// connection
	
	void connect();

	void disconnect();

	boolean isConnected();
	
	// projects	

	List<BimProject> getAllProjects();

	BimProject createProject(final String projectName);
	
	BimProject createSubProject(String projectName, String parentIdentifier);

	void disableProject(String projectId);

	void branchToExistingProject(String revisionId, String destinationProjectId);
	
	void branchToNewProject(String revisionId, String projectName);
	
	void checkin(String projectId, File file, boolean merge);

	FileOutputStream downloadIfc(String roid);

	DataHandler fetchProjectStructure(String revisionId);

	FileOutputStream downloadLastRevisionOfProject(String projectId);
	
	
	// modify data 
	
	void addDoubleAttribute(String transactionId, String objectId, String attributeName, double value);

	void addReference(String transactionId, String objectId, String relationName, String referenceId);

	void addStringAttribute(String transactionId, String objectId, String attributeName, String value);
	
	
	// transactions
	
	void abortTransaction(String transactionId);
	
	String commitTransaction(String transactionId);

	String createObject(String transactionId, String className);

	void enableProject(String projectId);

	Iterable<Entity> getEntitiesByType(String revisionId, String className);

	Map<Long, String> getAllGloabalId(String revisionId);

	Entity getEntityByGuid(String revisionId, String guid);

	Entity getEntityByOid(String revisionId, String objectId);

	BimProject getProjectByName(String name);

	BimProject getProjectByPoid(String projectId);

	Entity getReferencedEntity(ReferenceAttribute reference, String revisionId);

	BimRevision getRevision(String identifier);

	List<BimRevision> getRevisionsOfProject(BimProject project);

	String openTransaction(String projectId);

	void setReference(String transactionId, String objectId, String referenceName, String relatedObjectId);

	void setStringAttribute(String transactionId, String objectId, String attributeName, String value);

	void removeObject(String transactionId, String revisionId, String globalId);

}
