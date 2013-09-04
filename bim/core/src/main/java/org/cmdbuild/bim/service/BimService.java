package org.cmdbuild.bim.service;

import java.io.File;
import java.io.FileOutputStream;
import java.util.List;

import org.cmdbuild.bim.model.Entity;

public interface BimService {

	// Authentication

	void connect();

	/**
	 * Use the configuration info to login
	 */
	void login();

	void login(String username, String password);

	void logout();

	// User management

	BimUser addUser(String username, String name, String type);

	BimUser getUserFromName(String username);

	BimUser getUserFromId(String userId);

	void changePassword(String username, String newPassword, String oldPassword);

	// File management

	void checkin(String projectId, File file);

	void checkin(String projectId, File file, boolean merge);

	void branchToNewProject(String revisionId, String projectName);

	void branchToExistingProject(String revisionId, String destinationProjectId);

	FileOutputStream downloadLastRevisionOfProject(String projectId);

	FileOutputStream downloadIfc(String roid);

	FileOutputStream downloadJson(String roid);

	// Project management

	BimProject createProject(String name);

	List<BimProject> getAllProjects();

	List<BimRevision> getRevisionsOfProject(BimProject project);

	BimRevision getRevision(String revisionIdentifier);

	List<BimRevision> getLastRevisionOfAllProjects();

	BimProject getProjectByName(String name);

	BimProject getProjectByPoid(String projectId);

	void disableProject(String projectId);

	void enableProject(String projectId);

	// File contents

	List<Entity> getEntitiesByType(String revisionId, String className);

	Entity getEntityByGuid(String revisionId, String globalId);

	@Deprecated
	Entity getEntityByOid(String revisionId, String objectId);

	List<BimRevision> getRevisionsFromGuid(String guid);

	List<String> getAvailableClasses();

	List<String> getAvailableClassesInRevision(String revisionId);

	Entity getReferencedEntity(ReferenceAttribute reference, String revisionId);

	// File editing

	String openTransaction(String projectId);

	String commitTransaction(String id);

	void abortTransaction(String transactionId);

	String createObject(String transactionId, String className);

	void removeObject(String transactionId, String objectId);

	void removeReference(String transactionId, String objectId, String attributeName, int index);

	void unsetReference(String transactionId, String objectId, String referenceName);

	void setStringAttribute(String transactionId, String objectId, String attributeName, String value);

	void addStringAttribute(String transactionId, String objectId, String attributeName, String value);

	void setReference(String transactionId, String objectId, String referenceName, String globalIdOid);

	void addReference(String transactionId, String relationId, String string, String objectId);

	void addDoubleAttribute(String transactionId, String locationId, String string, double d);

	BimProject createSubProject(String projectName, String parentIdentifier);

}
