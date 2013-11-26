package org.cmdbuild.bim.service.bimserver;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;

import org.bimserver.interfaces.objects.SDataObject;
import org.bimserver.interfaces.objects.SDownloadResult;
import org.bimserver.interfaces.objects.SProject;
import org.bimserver.interfaces.objects.SRevision;
import org.bimserver.shared.exceptions.UserException;
import org.cmdbuild.bim.model.Entity;
import org.cmdbuild.bim.service.BimError;
import org.cmdbuild.bim.service.BimProject;
import org.cmdbuild.bim.service.BimRevision;
import org.cmdbuild.bim.service.BimService;
import org.cmdbuild.bim.service.Deserializer;
import org.cmdbuild.bim.service.ReferenceAttribute;
import org.cmdbuild.bim.service.Serializer;

import com.google.common.collect.Lists;

public class BimserverService implements BimService {

	private final BimserverClientHolder clientHolder;

	public BimserverService(final BimserverClientHolder clientHolder) {
		this.clientHolder = clientHolder;
	}

	@Override
	public void abortTransaction(final String transactionId) {
		try {
			final Long tid = Long.parseLong(transactionId);
			clientHolder.get().getBimsie1LowLevelInterface().abortTransaction(tid);
		} catch (final Throwable e) {
			throw new BimError(e);
		}
	}

	@Override
	public void addDoubleAttribute(final String transactionId, final String objectId, final String attributeName,
			final double value) {
		try {
			final Long tid = new Long(transactionId);
			final Long oid = new Long(objectId);
			clientHolder.get().getBimsie1LowLevelInterface().addDoubleAttribute(tid, oid, attributeName, value);
		} catch (final Throwable e) {
			throw new BimError(e);
		}
	}

	@Override
	public void addReference(final String transactionId, final String objectId, final String relationName,
			final String referenceId) {
		try {
			final Long tid = Long.parseLong(transactionId);
			final Long oid = Long.parseLong(objectId);
			final Long refid = Long.parseLong(referenceId);
			clientHolder.get().getBimsie1LowLevelInterface().addReference(tid, oid, relationName, refid);
		} catch (final Throwable e) {
			throw new BimError(e);
		}
	}

	@Override
	public void addStringAttribute(final String transactionId, final String objectId, final String attributeName,
			final String value) {
		try {
			final Long tid = Long.parseLong(transactionId);
			final Long oid = Long.parseLong(objectId);
			clientHolder.get().getBimsie1LowLevelInterface().addStringAttribute(tid, oid, attributeName, value);
		} catch (final Throwable e) {
			throw new BimError(e);
		}
	}

	@Override
	public void branchToExistingProject(final String revisionId, final String destinationProjectId) {
		try {
			final Long roid = Long.parseLong(revisionId);
			final Long poid = Long.parseLong(destinationProjectId);
			clientHolder.get().getBimsie1ServiceInterface().branchToExistingProject(roid, poid, "", true);
		} catch (final Throwable e) {
			throw new BimError(e);
		}
	}

	@Override
	public void branchToNewProject(final String revisionId, final String projectName) {
		try {
			final Long roid = Long.parseLong(revisionId);
			clientHolder.get().getBimsie1ServiceInterface().branchToNewProject(roid, projectName, "", true);
		} catch (final Throwable e) {
			throw new BimError(e);
		}

	}

	@Override
	public void checkin(final String projectId, final File file) {
		checkin(projectId, file, false);
	}

	@Override
	public void checkin(final String projectId, final File file, final boolean merge) {
		try {
			final Long poid = Long.parseLong(projectId);
			final Deserializer deserializer = new BimserverDeserializer(clientHolder.get().getBimsie1ServiceInterface()
					.getSuggestedDeserializerForExtension("ifc"));
			final DataSource dataSource = new FileDataSource(file);
			final DataHandler dataHandler = new DataHandler(dataSource);
			checkin(poid, "test", deserializer.getOid(), file.length(), file.getName(), dataHandler, merge, true);
		} catch (final Throwable e) {
			throw new BimError(e);
		}
	}

	@Override
	public String commitTransaction(final String transactionId) {
		try {

			final Long tid = Long.parseLong(transactionId);
			final Long roid = clientHolder.get().getBimsie1LowLevelInterface().commitTransaction(tid, "");
			return roid.toString();

		} catch (final Throwable e) {
			throw new BimError(e);
		}
	}

	@Override
	public String createObject(final String transactionId, final String className) {
		try {
			final Long tid = Long.parseLong(transactionId);
			final Long oid = clientHolder.get().getBimsie1LowLevelInterface().createObject(tid, className);
			return oid.toString();
		} catch (final Throwable e) {
			throw new BimError(e);
		}
	}

	@Override
	public BimProject createProject(final String projectName) {
		try {
			final BimProject project = new BimserverProject(clientHolder.get().getBimsie1ServiceInterface()
					.addProject(projectName));
			return project;
		} catch (final Throwable e) {
			throw new BimError(e);
		}
	}

	@Override
	public BimProject createSubProject(final String projectName, final String parentIdentifier) {
		try {
			final Long parentPoid = new Long(parentIdentifier);
			final BimProject project = new BimserverProject(clientHolder.get().getBimsie1ServiceInterface()
					.addProjectAsSubProject(projectName, parentPoid));
			return project;
		} catch (final Throwable e) {
			throw new BimError(e);
		}
	}

	@Override
	public void disableProject(final String projectId) {
		try {
			final Long poid = new Long(projectId);
			clientHolder.get().getBimsie1ServiceInterface().deleteProject(poid);
		} catch (final Throwable e) {
			throw new BimError(e);
		}
	}

	@Override
	public FileOutputStream downloadIfc(final String roid) {
		try {
			final Serializer serializer = getSerializerByContentType("application/ifc");
			final long downloadId = clientHolder.get().getBimsie1ServiceInterface()
					.download(new Long(roid), serializer.getOid(), true, true);
			final SDownloadResult bimserverResult = clientHolder.get().getBimsie1ServiceInterface()
					.getDownloadData(new Long(downloadId));
			final DataHandler dataHandler = bimserverResult.getFile();
			final File file = File.createTempFile("ifc", null);
			final FileOutputStream outputStream = new FileOutputStream(file);
			dataHandler.writeTo(outputStream);
			return outputStream;
		} catch (final Throwable e) {
			throw new BimError(e);
		}
	}

	@Override
	public FileOutputStream downloadJson(final String roid) {
		throw new BimError("Not implemented");
	}

	@Override
	public FileOutputStream downloadLastRevisionOfProject(final String projectId) {
		try {
			final Long poid = new Long(projectId);
			if (!getProjectByPoid(projectId).isActive()) {
				throw new BimError("Cannot download disabled projects");
			}
			final Long roid = clientHolder.get().getBimsie1ServiceInterface().getProjectByPoid(poid)
					.getLastRevisionId();
			final Serializer serializer = getSerializerByContentType("application/ifc");
			final long downloadId = clientHolder.get().getBimsie1ServiceInterface()
					.download(new Long(roid), serializer.getOid(), true, true);
			final SDownloadResult bimserverResult = clientHolder.get().getBimsie1ServiceInterface()
					.getDownloadData(new Long(downloadId));
			final DataHandler dataHandler = bimserverResult.getFile();
			final File file = File.createTempFile("ifc", null);
			final FileOutputStream outputStream = new FileOutputStream(file);
			dataHandler.writeTo(outputStream);
			outputStream.close();
			return outputStream;
		} catch (final Throwable e) {
			throw new BimError(e);
		}

	}

	@Override
	public void enableProject(final String projectId) {
		try {
			final Long poid = new Long(projectId);
			clientHolder.get().getBimsie1ServiceInterface().undeleteProject(poid);
		} catch (final Throwable e) {
			throw new BimError(e);
		}
	}

	@Override
	public List<BimProject> getAllProjects() {
		try {
			final List<SProject> bimserverProjects = clientHolder.get().getServiceInterface().getAllReadableProjects();

			final List<BimProject> projects = new ArrayList<BimProject>();

			for (final SProject bimserverProject : bimserverProjects) {
				final BimProject project = new BimserverProject(bimserverProject);
				projects.add(project);
			}
			return projects;
		} catch (final Throwable e) {
			throw new BimError(e);
		}
	}

	@Override
	public List<String> getAvailableClasses() {
		try {
			final List<String> classes = clientHolder.get().getServiceInterface().getAvailableClasses();
			return classes;
		} catch (final Throwable e) {
			throw new BimError(e);
		}
	}

	@Override
	public List<String> getAvailableClassesInRevision(final String revisionId) {
		try {
			final Long roid = new Long(revisionId);
			final List<String> classes = clientHolder.get().getServiceInterface().getAvailableClassesInRevision(roid);
			return classes;
		} catch (final Throwable e) {
			throw new BimError(e);
		}
	}

	@Override
	public List<Entity> getEntitiesByType(final String revisionId, final String className) {
		try {
			final Long roid = new Long(revisionId);
			final List<Entity> entities = new ArrayList<Entity>();
			final List<SDataObject> objects = clientHolder.get().getBimsie1LowLevelInterface()
					.getDataObjectsByType(roid, className);
			if (objects != null) {
				for (final SDataObject object : objects) {
					final Entity entity = new BimserverEntity(object);
					entities.add(entity);
				}
			}
			return entities;
		} catch (final Throwable e) {
			throw new BimError(e);
		}
	}

	@Override
	public Entity getEntityByGuid(final String revisionId, final String guid) {
		Entity entity = Entity.NULL_ENTITY;
		try {
			final Long roid = new Long(revisionId);
			final SDataObject object = clientHolder.get().getBimsie1LowLevelInterface().getDataObjectByGuid(roid, guid);
			entity = new BimserverEntity(object);
		} catch (final UserException e) {
		} catch (final Throwable e) {
			throw new BimError(e);
		}
		return entity;
	}

	@Override
	public Entity getEntityByOid(final String revisionId, final String objectId) {
		Entity entity = Entity.NULL_ENTITY;
		try {
			final Long roid = new Long(revisionId);
			final Long oid = new Long(objectId);
			final SDataObject object = clientHolder.get().getBimsie1LowLevelInterface().getDataObjectByOid(roid, oid);
			entity = new BimserverEntity(object);
		} catch (final UserException e) {
			//warning: objectId not found
		} catch (final Throwable e) {
			throw new BimError(e);
		}
		return entity;
	}

	@Override
	public List<BimRevision> getLastRevisionOfAllProjects() {
		final List<BimProject> projects = getAllProjects();
		final List<BimRevision> revisions = Lists.newArrayList();
		try {
			for (final BimProject project : projects) {
				final BimRevision revision = getRevision(new Long(project.getLastRevisionId()).toString());
				if (revision != null) {
					revisions.add(revision);
				}
			}
			return revisions;
		} catch (final Throwable e) {
			throw new BimError(e);
		}
	}

	@Override
	public BimProject getProjectByName(final String name) {
		BimProject bimProject = null;
		final List<BimProject> projects = new ArrayList<BimProject>();
		List<SProject> bimserverProjects;
		try {
			bimserverProjects = clientHolder.get().getBimsie1ServiceInterface().getProjectsByName(name);
		} catch (final Throwable e) {
			throw new BimError(e);
		}
		for (final SProject bimserverProject : bimserverProjects) {
			final BimProject project = new BimserverProject(bimserverProject);
			projects.add(project);
		}
		if (projects.size() == 0) {
			return BimProject.NULL_PROJECT;
		} else if (projects.size() == 1) {
			bimProject = projects.get(0);
		} else if (projects.size() > 1) {
			throw new BimError("More than one project found with name '" + name + "'");
		} else {
			throw new BimError("No projects found with name '" + name + "'");
		}
		return bimProject;
	}

	@Override
	public BimProject getProjectByPoid(final String projectId) {
		try {
			final Long poid = new Long(projectId);
			final SProject bimserverProject = clientHolder.get().getBimsie1ServiceInterface().getProjectByPoid(poid);
			final BimProject project = new BimserverProject(bimserverProject);
			return project;
		} catch (final Throwable e) {
			throw new BimError(e);
		}
	}

	@Override
	public Entity getReferencedEntity(final ReferenceAttribute reference, final String revisionId) {
		Entity entity = Entity.NULL_ENTITY;
		try {
			final Long roid = new Long(revisionId);
			if (reference.getGlobalId() != null) {
				final String guid = reference.getGlobalId();
				entity = new BimserverEntity(clientHolder.get().getBimsie1LowLevelInterface()
						.getDataObjectByGuid(roid, guid));
			} else {
				final Long oid = reference.getOid();
				entity = new BimserverEntity(clientHolder.get().getBimsie1LowLevelInterface()
						.getDataObjectByOid(roid, oid));
			}
			return entity;
		} catch (final Throwable e) {
			throw new BimError(e);
		}
	}

	@Override
	public BimRevision getRevision(final String identifier) {
		final Long roid = new Long(identifier);
		BimRevision revision = null;
		try {
			if (roid != -1) {
				revision = new BimserverRevision(clientHolder.get().getBimsie1ServiceInterface().getRevision(roid));
			}
			return revision;
		} catch (final Throwable e) {
			throw new BimError(e);
		}
	}

	@Override
	public List<BimRevision> getRevisionsFromGuid(final String guid) {
		try {
			final List<BimRevision> revisions = Lists.newArrayList();
			for (final BimRevision revision : getLastRevisionOfAllProjects()) {
				final Entity entity = getEntityByGuid(revision.getIdentifier(), guid);
				if (entity.isValid()) {
					revisions.add(revision);
				}
			}
			return revisions;
		} catch (final Throwable e) {
			throw new BimError(e);
		}
	}

	@Override
	public List<BimRevision> getRevisionsOfProject(final BimProject project) {
		try {
			final List<BimRevision> revisions = Lists.newArrayList();
			final Long poid = new Long(project.getIdentifier());
			final List<org.bimserver.interfaces.objects.SRevision> srevisions = clientHolder.get()
					.getBimsie1ServiceInterface().getAllRevisionsOfProject(poid);
			if (srevisions != null) {
				for (final SRevision srevision : srevisions) {
					final BimRevision revision = new BimserverRevision(srevision);
					revisions.add(revision);
				}
			}
			return revisions;
		} catch (final Throwable e) {
			throw new BimError(e);
		}
	}

	@Override
	public String openTransaction(final String projectId) {
		try {
			final Long poid = new Long(projectId);
			final Long tid = clientHolder.get().getBimsie1LowLevelInterface().startTransaction(poid);
			return tid.toString();
		} catch (final Throwable e) {
			throw new BimError(e);
		}
	}

	@Override
	public void removeObject(final String transactionId, final String objectId) {
		try {
			final Long tid = new Long(transactionId);
			final Long oid = new Long(objectId);
			clientHolder.get().getBimsie1LowLevelInterface().removeObject(tid, oid);
		} catch (final Throwable e) {
			throw new BimError(e);
		}
	}

	@Override
	public void removeReference(final String transactionId, final String objectId, final String attributeName,
			final int index) {
		try {
			final Long tid = new Long(transactionId);
			final Long oid = new Long(objectId);
			clientHolder.get().getBimsie1LowLevelInterface().removeReference(tid, oid, attributeName, index);
		} catch (final Throwable e) {
			throw new BimError(e);
		}

	}

	@Override
	public void setReference(final String transactionId, final String objectId, final String referenceName,
			final String relatedObjectId) {
		try {
			final Long tid = new Long(transactionId);
			final Long oid1 = new Long(objectId);
			final Long oid2 = new Long(relatedObjectId);
			clientHolder.get().getBimsie1LowLevelInterface().setReference(tid, oid1, referenceName, oid2);
		} catch (final Throwable e) {
			throw new BimError(e);
		}
	}

	@Override
	public void setStringAttribute(final String transactionId, final String objectId, final String attributeName,
			final String value) {
		try {
			final Long tid = new Long(transactionId);
			final Long oid = new Long(objectId);
			clientHolder.get().getBimsie1LowLevelInterface().setStringAttribute(tid, oid, attributeName, value);
		} catch (final Throwable e) {
			throw new BimError(e);
		}
	}

	@Override
	public void unsetReference(final String transactionId, final String objectId, final String referenceName) {
		try {
			final Long tid = new Long(transactionId);
			final Long oid = new Long(objectId);
			clientHolder.get().getBimsie1LowLevelInterface().unsetReference(tid, oid, referenceName);
		} catch (final Throwable e) {
			throw new BimError(e);
		}
	}

	private void checkin(final Long poid, final String comment, final Long deserializerOid, final Long fileSize,
			final String fileName, final DataHandler ifcFile, final boolean merge, final boolean sync) {
		try {
			clientHolder.get().getServiceInterface()
					.checkin(poid, comment, deserializerOid, fileSize, fileName, ifcFile, merge, sync);
		} catch (final Throwable e) {
			throw new BimError(e);
		}

	}

	private Serializer getSerializerByContentType(final String contentType) {
		try {
			final Serializer serializer = new BimserverSerializer(clientHolder.get().getBimsie1ServiceInterface()
					.getSerializerByContentType(contentType));
			return serializer;
		} catch (final Throwable e) {
			throw new BimError(e);
		}
	}

}
