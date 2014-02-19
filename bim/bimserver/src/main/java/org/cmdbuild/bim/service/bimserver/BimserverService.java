package org.cmdbuild.bim.service.bimserver;

import java.io.File;
import java.io.FileOutputStream;
import java.util.List;
import java.util.Map;

import javax.activation.DataHandler;

import org.cmdbuild.bim.model.Entity;
import org.cmdbuild.bim.service.BimError;
import org.cmdbuild.bim.service.BimProject;
import org.cmdbuild.bim.service.BimRevision;
import org.cmdbuild.bim.service.BimService;
import org.cmdbuild.bim.service.ReferenceAttribute;

public class BimserverService implements BimService {

	private final BimserverClient client;

	public BimserverService(final BimserverClient client) {
		this.client = client;
	}

	@Override
	public void abortTransaction(final String transactionId) {
		client.abortTransaction(transactionId);
	}

	@Override
	public void addDoubleAttribute(final String transactionId, final String objectId, final String attributeName,
			final double value) {
		client.addDoubleAttribute(transactionId, objectId, attributeName, value);
	}

	@Override
	public void addReference(final String transactionId, final String objectId, final String relationName,
			final String referenceId) {
		client.addReference(transactionId, objectId, relationName, referenceId);
	}

	@Override
	public void addStringAttribute(final String transactionId, final String objectId, final String attributeName,
			final String value) {
		client.addStringAttribute(transactionId, objectId, attributeName, value);
	}

	@Override
	public void branchToExistingProject(final String revisionId, final String destinationProjectId) {
		client.branchToExistingProject(revisionId, destinationProjectId);
	}

	@Override
	public void branchToNewProject(final String revisionId, final String projectName) {
		client.branchToNewProject(revisionId, projectName);
	}

	@Override
	public void checkin(final String projectId, final File file) {
		checkin(projectId, file, false);
	}

	@Override
	public void checkin(final String projectId, final File file, final boolean merge) {
		client.checkin(projectId, file, merge);
	}

	@Override
	public String commitTransaction(final String transactionId) {
		return client.commitTransaction(transactionId);
	}

	@Override
	public String createObject(final String transactionId, final String className) {
		return client.createObject(transactionId, className);
	}

	@Override
	public BimProject createProject(final String projectName) {
		return client.createProject(projectName);
	}

	@Override
	public BimProject createSubProject(final String projectName, final String parentIdentifier) {
		return client.createSubProject(projectName, parentIdentifier);
	}

	@Override
	public void enableProject(String projectId) {
		client.enableProject(projectId);
	}

	@Override
	public void disableProject(final String projectId) {
		client.disableProject(projectId);
	}

	@Override
	public DataHandler downloadIfc(final String revisionId) {
		return client.downloadIfc(revisionId);
	}

	@Override
	public DataHandler fetchProjectStructure(String revisionId) {
		return client.fetchProjectStructure(revisionId);
	}

	@Override
	public FileOutputStream downloadJson(final String roid) {
		throw new BimError("Not implemented");
	}

	@Override
	public FileOutputStream downloadLastRevisionOfProject(final String projectId) {
		return client.downloadLastRevisionOfProject(projectId);

	}

	@Override
	public List<BimProject> getAllProjects() {
		return client.getAllProjects();
	}

	@Override
	public List<String> getAvailableClasses() {
		throw new BimError("Not implemented");
	}

	@Override
	public List<String> getAvailableClassesInRevision(final String revisionId) {
		throw new BimError("Not implemented");
	}

	@Override
	public Iterable<Entity> getEntitiesByType(final String revisionId, final String className) {
		return client.getEntitiesByType(revisionId, className);
	}

	@Override
	public Map<Long, String> getAllGloabalId(String revisionId) {
		return client.getAllGloabalId(revisionId);
	}

	@Override
	public Entity getEntityByGuid(final String revisionId, final String guid) {
		return client.getEntityByGuid(revisionId, guid);
	}

	@Override
	public Entity getEntityByOid(final String revisionId, final String objectId) {
		return client.getEntityByOid(revisionId, objectId);
	}

	@Override
	public List<BimRevision> getLastRevisionOfAllProjects() {
		throw new BimError("Not implemented");
	}

	@Override
	public BimProject getProjectByName(final String name) {
		return client.getProjectByName(name);
	}

	@Override
	public BimProject getProjectByPoid(final String projectId) {
		return client.getProjectByPoid(projectId);
	}

	@Override
	public Entity getReferencedEntity(final ReferenceAttribute reference, final String revisionId) {
		return client.getReferencedEntity(reference, revisionId);
	}

	@Override
	public BimRevision getRevision(final String identifier) {
		return client.getRevision(identifier);
	}

	@Override
	public List<BimRevision> getRevisionsFromGuid(final String guid) {
		throw new BimError("not implemented");
	}

	@Override
	public List<BimRevision> getRevisionsOfProject(final BimProject project) {
		return client.getRevisionsOfProject(project);
	}

	@Override
	public String openTransaction(final String projectId) {
		return client.openTransaction(projectId);
	}

	@Override
	public void removeObject(final String transactionId, String revisionId, final String globalId) {
		client.removeObject(transactionId, revisionId, globalId);
	}

	@Override
	public void removeReference(final String transactionId, final String objectId, final String attributeName,
			final int index) {
		throw new BimError("Not implemented");
	}

	@Override
	public void setReference(final String transactionId, final String objectId, final String referenceName,
			final String relatedObjectId) {
		client.setReference(transactionId, objectId, referenceName, relatedObjectId);
	}

	@Override
	public void setStringAttribute(final String transactionId, final String objectId, final String attributeName,
			final String value) {
		client.setStringAttribute(transactionId, objectId, attributeName, value);
	}

	@Override
	public void unsetReference(final String transactionId, final String objectId, final String referenceName) {
		throw new BimError("Not implemented");
	}

}
