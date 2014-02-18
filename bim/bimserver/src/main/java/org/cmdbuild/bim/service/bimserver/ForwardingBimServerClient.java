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

public abstract class ForwardingBimServerClient implements BimserverClient {

	private final BimserverClient delegate;

	protected ForwardingBimServerClient(final BimserverClient delegate) {
		this.delegate = delegate;
	}

	@Override
	public void connect() {
		delegate.connect();
	}

	@Override
	public void disconnect() {
		delegate.disconnect();
	}

	@Override
	public boolean isConnected() {
		return delegate.isConnected();
	}

	@Override
	public BimProject createProject(String projectName) {
		return delegate.createProject(projectName);
	}

	@Override
	public List<BimProject> getAllProjects() {
		return delegate.getAllProjects();
	}

	@Override
	public BimProject createSubProject(String projectName, String parentIdentifier) {
		return delegate.createSubProject(projectName, parentIdentifier);
	}

	@Override
	public void disableProject(String projectId) {
		delegate.disableProject(projectId);
	}

	@Override
	public void branchToExistingProject(String revisionId, String destinationProjectId) {
		delegate.branchToExistingProject(revisionId, destinationProjectId);
	}

	@Override
	public void branchToNewProject(String revisionId, String projectName) {
		delegate.branchToNewProject(revisionId, projectName);
	}

	@Override
	public void checkin(String projectId, File file, boolean merge) {
		delegate.checkin(projectId, file, merge);
	}

	@Override
	public DataHandler downloadIfc(String roid) {
		return delegate.downloadIfc(roid);
	}

	@Override
	public DataHandler fetchProjectStructure(String revisionId) {
		return delegate.fetchProjectStructure(revisionId);
	}

	@Override
	public FileOutputStream downloadLastRevisionOfProject(String projectId) {
		return delegate.downloadLastRevisionOfProject(projectId);
	}

	@Override
	public void addDoubleAttribute(String transactionId, String objectId, String attributeName, double value) {
		delegate.addDoubleAttribute(transactionId, objectId, attributeName, value);
	}

	@Override
	public void addReference(String transactionId, String objectId, String relationName, String referenceId) {
		delegate.addReference(transactionId, objectId, relationName, referenceId);
	}

	@Override
	public void addStringAttribute(String transactionId, String objectId, String attributeName, String value) {
		delegate.addStringAttribute(transactionId, objectId, attributeName, value);
	}

	@Override
	public void abortTransaction(String transactionId) {
		delegate.abortTransaction(transactionId);
	}

	@Override
	public String commitTransaction(String transactionId) {
		return delegate.commitTransaction(transactionId);
	}

	@Override
	public String createObject(String transactionId, String className) {
		return delegate.createObject(transactionId, className);
	}
	
	@Override
	public void removeObject(String transactionId, String revisionId, String globalId) {
		delegate.removeObject(transactionId, revisionId, globalId);
	}

	@Override
	public void enableProject(String projectId) {
		delegate.enableProject(projectId);
	}

	@Override
	public Iterable<Entity> getEntitiesByType(String revisionId, String className) {
		return delegate.getEntitiesByType(revisionId, className);
	}

	@Override
	public Map<Long, String> getAllGloabalId(String revisionId) {
		return delegate.getAllGloabalId(revisionId);
	}

	@Override
	public Entity getEntityByGuid(String revisionId, String guid) {
		return delegate.getEntityByGuid(revisionId, guid);
	}

	@Override
	public Entity getEntityByOid(String revisionId, String objectId) {
		return delegate.getEntityByOid(revisionId, objectId);
	}

	@Override
	public BimProject getProjectByName(String name) {
		return delegate.getProjectByName(name);
	}

	@Override
	public BimProject getProjectByPoid(String projectId) {
		return delegate.getProjectByPoid(projectId);
	}

	@Override
	public Entity getReferencedEntity(ReferenceAttribute reference, String revisionId) {
		return delegate.getReferencedEntity(reference, revisionId);
	}

	@Override
	public BimRevision getRevision(String identifier) {
		return delegate.getRevision(identifier);
	}

	@Override
	public List<BimRevision> getRevisionsOfProject(BimProject project) {
		return delegate.getRevisionsOfProject(project);
	}

	@Override
	public String openTransaction(String projectId) {
		return delegate.openTransaction(projectId);
	}

	@Override
	public void setReference(String transactionId, String objectId, String referenceName, String relatedObjectId) {
		delegate.setReference(transactionId, objectId, referenceName, relatedObjectId);
	}

	@Override
	public void setStringAttribute(String transactionId, String objectId, String attributeName, String value) {
		delegate.setStringAttribute(transactionId, objectId, attributeName, value);
	}

}
