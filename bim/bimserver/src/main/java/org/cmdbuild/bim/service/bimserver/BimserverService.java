package org.cmdbuild.bim.service.bimserver;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;

import org.bimserver.client.BimServerClient;
import org.bimserver.client.BimServerClientFactory;
import org.bimserver.client.soap.SoapBimServerClientFactory;
import org.bimserver.interfaces.objects.SDataObject;
import org.bimserver.interfaces.objects.SDownloadResult;
import org.bimserver.interfaces.objects.SProject;
import org.bimserver.interfaces.objects.SRevision;
import org.bimserver.interfaces.objects.SUser;
import org.bimserver.interfaces.objects.SUserType;
import org.bimserver.shared.AuthenticationInfo;
import org.bimserver.shared.UsernamePasswordAuthenticationInfo;
import org.bimserver.shared.exceptions.UserException;
import org.bimserver.shared.interfaces.AuthInterface;
import org.bimserver.shared.interfaces.ServiceInterface;
import org.bimserver.shared.interfaces.bimsie1.Bimsie1LowLevelInterface;
import org.bimserver.shared.interfaces.bimsie1.Bimsie1ServiceInterface;
import org.cmdbuild.bim.model.Entity;
import org.cmdbuild.bim.service.BimError;
import org.cmdbuild.bim.service.BimProject;
import org.cmdbuild.bim.service.BimRevision;
import org.cmdbuild.bim.service.BimService;
import org.cmdbuild.bim.service.BimUser;
import org.cmdbuild.bim.service.Deserializer;
import org.cmdbuild.bim.service.ReferenceAttribute;
import org.cmdbuild.bim.service.Serializer;

import com.google.common.collect.Lists;

public class BimserverService implements BimService {

	private BimServerClient client;
	private ServiceInterface serviceInterface;
	private BimServerClientFactory factory;
	private AuthInterface authenticationInterface;
	private Bimsie1ServiceInterface bimsie1ServiceInterface;
	private Bimsie1LowLevelInterface bimsie1LowLevelInterface;

	public static interface Configuration {
		String getUrl();

		String getUsername();

		String getPassword();
	}

	private final Configuration configuration;

	public BimserverService(final Configuration configuration) {
		this.configuration = configuration;
	}

	@Override
	public void connect() {
		factory = new SoapBimServerClientFactory(configuration.getUrl());
	}

	@Override
	public void login() {
		login(configuration.getUsername(), configuration.getPassword());
	}

	@Override
	public void login(final String username, final String password) {

		final AuthenticationInfo authenticationInfo = new UsernamePasswordAuthenticationInfo(
				username, password);
		try {
			client = factory.create(authenticationInfo);
			bimsie1LowLevelInterface = client.getBimsie1LowLevelInterface();
			serviceInterface = client.getServiceInterface();
			bimsie1ServiceInterface = client.getBimsie1ServiceInterface();
		} catch (final Throwable t) {
			final Exception e = new Exception();
			throw new BimError("error in "
					+ e.getStackTrace()[0].getMethodName(), t);
		}
	}

	@Override
	public void logout() {
		try {
			client.disconnect();
		} catch (final Throwable t) {
			final Exception e = new Exception();
			throw new BimError("error in "
					+ e.getStackTrace()[0].getMethodName(), t);
		}
	}

	@Override
	public void checkin(final String projectId, final File file) {
		checkin(projectId, file, false);

	}

	@Override
	public void checkin(final String projectId, final File file,
			final boolean merge) {
		try {
			final Long poid = new Long(projectId);
			final Deserializer deserializer = new BimserverDeserializer(
					bimsie1ServiceInterface
							.getSuggestedDeserializerForExtension("ifc"));
			final DataSource dataSource = new FileDataSource(file);
			final DataHandler dataHandler = new DataHandler(dataSource);
			checkin(poid, "test", deserializer.getOid(), file.length(),
					file.getName(), dataHandler, merge, true);
		} catch (final Throwable e) {
			final Exception ecc = new Exception();
			throw new BimError("error in "
					+ ecc.getStackTrace()[0].getMethodName(), e);
		}
	}

	private void checkin(final Long poid, final String comment,
			final Long deserializerOid, final Long fileSize,
			final String fileName, final DataHandler ifcFile,
			final boolean merge, final boolean sync) {
		try {
			serviceInterface.checkin(poid, comment, deserializerOid, fileSize,
					fileName, ifcFile, merge, sync);
		} catch (final Throwable e) {
			throw new BimError("error in "
					+ e.getStackTrace()[0].getMethodName(), e);
		}

	}

	@Override
	public void branchToNewProject(final String revisionId,
			final String projectName) {
		try {
			final Long roid = Long.parseLong(revisionId);
			bimsie1ServiceInterface.branchToNewProject(roid, projectName, "",
					true);
		} catch (final Throwable e) {
			throw new BimError("error in "
					+ e.getStackTrace()[0].getMethodName(), e);
		}

	}

	@Override
	public void branchToExistingProject(final String revisionId,
			final String destinationProjectId) {
		try {
			final Long roid = Long.parseLong(revisionId);
			final Long poid = Long.parseLong(destinationProjectId);
			bimsie1ServiceInterface.branchToExistingProject(roid, poid, "",
					true);
		} catch (final Throwable e) {
			throw new BimError("error in "
					+ e.getStackTrace()[0].getMethodName(), e);
		}
	}

	@Override
	public FileOutputStream downloadLastRevisionOfProject(final String projectId) {
		try {
			final Long poid = new Long(projectId);
			if (!getProjectByPoid(projectId).isActive()) {
				throw new BimError("Cannot download disabled projects");
			}
			final Long roid = bimsie1ServiceInterface.getProjectByPoid(poid)
					.getLastRevisionId();
			final Serializer serializer = getSerializerByContentType("application/ifc");
			final long downloadId = bimsie1ServiceInterface.download(new Long(
					roid), serializer.getOid(), true, true);
			final SDownloadResult bimserverResult = bimsie1ServiceInterface
					.getDownloadData(new Long(downloadId));
			final DataHandler dataHandler = bimserverResult.getFile();
			final File file = File.createTempFile("ifc", null);
			final FileOutputStream outputStream = new FileOutputStream(file);
			dataHandler.writeTo(outputStream);
			outputStream.close();
			return outputStream;
		} catch (final Throwable e) {
			final Exception ecc = new Exception();
			throw new BimError("error in "
					+ ecc.getStackTrace()[0].getMethodName(), e);
		}

	}

	@Override
	public FileOutputStream downloadIfc(final String roid) {
		try {
			final Serializer serializer = getSerializerByContentType("application/ifc");
			final long downloadId = bimsie1ServiceInterface.download(new Long(
					roid), serializer.getOid(), true, true);
			final SDownloadResult bimserverResult = bimsie1ServiceInterface
					.getDownloadData(new Long(downloadId));
			final DataHandler dataHandler = bimserverResult.getFile();
			final File file = File.createTempFile("ifc", null);
			final FileOutputStream outputStream = new FileOutputStream(file);
			dataHandler.writeTo(outputStream);
			return outputStream;
		} catch (final Throwable e) {
			final Exception ecc = new Exception();
			throw new BimError("error in "
					+ ecc.getStackTrace()[0].getMethodName(), e);
		}
	}

	private Serializer getSerializerByContentType(final String contentType) {
		try {
			final Serializer serializer = new BimserverSerializer(
					bimsie1ServiceInterface
							.getSerializerByContentType(contentType));
			return serializer;
		} catch (final Throwable e) {
			throw new BimError("error in "
					+ e.getStackTrace()[0].getMethodName(), e);
		}
	}

	@Override
	public FileOutputStream downloadJson(final String roid) {
		final Exception e = new Exception();
		throw new BimError(e.getStackTrace()[0].getMethodName()
				+ " not implemented", e);
	}

	@Override
	public BimProject createProject(final String projectName) {
		try {
			final BimProject project = new BimserverProject(
					bimsie1ServiceInterface.addProject(projectName));
			return project;
		} catch (final Throwable e) {
			final Exception ecc = new Exception();
			throw new BimError("error in "
					+ ecc.getStackTrace()[0].getMethodName(), e);
		}
	}

	@Override
	public BimProject createSubProject(final String projectName,
			final String parentIdentifier) {
		try {
			final Long parentPoid = new Long(parentIdentifier);
			final BimProject project = new BimserverProject(
					bimsie1ServiceInterface.addProjectAsSubProject(projectName,
							parentPoid));
			return project;
		} catch (final Throwable e) {
			final Exception ecc = new Exception();
			throw new BimError("error in "
					+ ecc.getStackTrace()[0].getMethodName(), e);
		}
	}

	@Override
	public List<BimProject> getAllProjects() {
		try {
			final List<SProject> bimserverProjects = serviceInterface
					.getAllReadableProjects();

			final List<BimProject> projects = new ArrayList<BimProject>();

			for (final SProject bimserverProject : bimserverProjects) {
				final BimProject project = new BimserverProject(
						bimserverProject);
				projects.add(project);
			}
			return projects;
		} catch (final Throwable e) {
			throw new BimError("error in "
					+ e.getStackTrace()[0].getMethodName(), e);
		}
	}

	@Override
	public List<BimRevision> getRevisionsOfProject(final BimProject project) {
		try {
			final List<BimRevision> revisions = Lists.newArrayList();
			final Long poid = new Long(project.getIdentifier());
			final List<org.bimserver.interfaces.objects.SRevision> srevisions = bimsie1ServiceInterface
					.getAllRevisionsOfProject(poid);
			for (final SRevision srevision : srevisions) {
				final BimRevision revision = new BimserverRevision(srevision);
				revisions.add(revision);
			}
			return revisions;
		} catch (final Throwable e) {
			throw new BimError("error in "
					+ e.getStackTrace()[0].getMethodName(), e);
		}
	}

	@Override
	public BimRevision getRevision(final String identifier) {
		final Long roid = new Long(identifier);
		BimRevision revision = null;
		try {
			if (roid != -1) {
				revision = new BimserverRevision(
						bimsie1ServiceInterface.getRevision(roid));
			}
			return revision;
		} catch (final Throwable e) {
			throw new BimError("error in "
					+ e.getStackTrace()[0].getMethodName(), e);
		}
	}

	@Override
	public List<BimRevision> getLastRevisionOfAllProjects() {
		final List<BimProject> projects = getAllProjects();
		final List<BimRevision> revisions = Lists.newArrayList();
		try {
			for (final BimProject project : projects) {
				final BimRevision revision = getRevision(new Long(
						project.getLastRevisionId()).toString());
				if (revision != null) {
					revisions.add(revision);
				}
			}
			return revisions;
		} catch (final Throwable e) {
			throw new BimError("error in "
					+ e.getStackTrace()[0].getMethodName(), e);
		}
	}

	@Override
	public BimProject getProjectByName(final String name) {
		BimProject bimProject = null;
		final List<BimProject> projects = new ArrayList<BimProject>();
		List<SProject> bimserverProjects;
		try {
			bimserverProjects = bimsie1ServiceInterface.getProjectsByName(name);
		} catch (final Throwable e) {
			throw new BimError("Error in getProjectByName", e);
		}
		for (final SProject bimserverProject : bimserverProjects) {
			final BimProject project = new BimserverProject(bimserverProject);
			projects.add(project);
		}
		if (projects.size() == 0) {
			return BimProject.NULL_PROJECT;
		} else if (projects.size() == 1) {
			bimProject = projects.get(0);
		} else {
			final Exception e = new Exception();
			throw new BimError("Nome del progetto non univoco o non presente",
					e);
		}
		return bimProject;
	}

	@Override
	public List<Entity> getEntitiesByType(final String revisionId,
			final String className) {
		try {
			final Long roid = new Long(revisionId);
			final List<Entity> entities = new ArrayList<Entity>();
			final List<SDataObject> objects = bimsie1LowLevelInterface
					.getDataObjectsByType(roid, className);
			for (final SDataObject object : objects) {
				final Entity entity = new BimserverEntity(object);
				entities.add(entity);
			}
			return entities;
		} catch (final Throwable e) {
			final Exception ecc = new Exception();
			throw new BimError("error in "
					+ ecc.getStackTrace()[0].getMethodName(), e);
		}
	}

	@Override
	public Entity getEntityByGuid(final String revisionId, final String guid) {
		Entity entity = Entity.NULL_ENTITY;
		try {
			final Long roid = new Long(revisionId);
			final SDataObject object = bimsie1LowLevelInterface
					.getDataObjectByGuid(roid, guid);
			entity = new BimserverEntity(object);
		} catch (final UserException e) {

		} catch (final Throwable e) {
			final Exception ecc = new Exception();
			throw new BimError("error in "
					+ ecc.getStackTrace()[0].getMethodName(), e);
		}
		return entity;
	}

	@Override
	public Entity getEntityByOid(final String revisionId, final String objectId) {
		Entity entity = Entity.NULL_ENTITY;
		try {
			final Long roid = new Long(revisionId);
			final Long oid = new Long(objectId);
			final SDataObject object = bimsie1LowLevelInterface
					.getDataObjectByOid(roid, oid);
			entity = new BimserverEntity(object);
		} catch (final UserException e) {

		} catch (final Throwable e) {
			final Exception ecc = new Exception();
			throw new BimError("error in "
					+ ecc.getStackTrace()[0].getMethodName(), e);
		}
		return entity;
	}

	@Override
	public List<BimRevision> getRevisionsFromGuid(final String guid) {
		try {
			final List<BimRevision> revisions = Lists.newArrayList();
			for (final BimRevision revision : getLastRevisionOfAllProjects()) {
				final Entity entity = getEntityByGuid(revision.getIdentifier(),
						guid);
				if (entity.isValid()) {
					revisions.add(revision);
				}
			}
			return revisions;
		} catch (final Throwable e) {
			final Exception ecc = new Exception();
			throw new BimError("error in "
					+ ecc.getStackTrace()[0].getMethodName(), e);
		}
	}

	@Override
	public List<String> getAvailableClasses() {
		try {
			final List<String> classes = serviceInterface.getAvailableClasses();
			return classes;
		} catch (final Throwable e) {
			throw new BimError("error in "
					+ e.getStackTrace()[0].getMethodName(), e);
		}
	}

	@Override
	public List<String> getAvailableClassesInRevision(final String revisionId) {
		try {
			final Long roid = new Long(revisionId);
			final List<String> classes = serviceInterface
					.getAvailableClassesInRevision(roid);
			return classes;
		} catch (final Throwable e) {
			final Exception ecc = new Exception();
			throw new BimError("error in "
					+ ecc.getStackTrace()[0].getMethodName(), e);
		}
	}

	@Override
	public Entity getReferencedEntity(final ReferenceAttribute reference,
			final String revisionId) {
		Entity entity = Entity.NULL_ENTITY;
		try {
			final Long roid = new Long(revisionId);
			if (reference.getGuid() != null) {
				final String guid = reference.getGuid();
				entity = new BimserverEntity(
						bimsie1LowLevelInterface
								.getDataObjectByGuid(roid, guid));
			} else {
				final Long oid = reference.getOid();
				entity = new BimserverEntity(
						bimsie1LowLevelInterface.getDataObjectByOid(roid, oid));
			}
			return entity;
		} catch (final Throwable e) {
			final Exception ecc = new Exception();
			throw new BimError("error in "
					+ ecc.getStackTrace()[0].getMethodName(), e);
		}
	}

	@Override
	public BimProject getProjectByPoid(final String projectId) {
		try {
			final Long poid = new Long(projectId);
			final SProject bimserverProject = bimsie1ServiceInterface
					.getProjectByPoid(poid);
			final BimProject project = new BimserverProject(bimserverProject);
			return project;
		} catch (final Throwable e) {
			final Exception ecc = new Exception();
			throw new BimError("error in "
					+ ecc.getStackTrace()[0].getMethodName(), e);
		}
	}

	@Override
	public String openTransaction(final String projectId) {
		try {
			final Long poid = new Long(projectId);
			final Long tid = bimsie1LowLevelInterface.startTransaction(poid);
			return tid.toString();
		} catch (final Throwable e) {
			final Exception ecc = new Exception();
			throw new BimError("error in "
					+ ecc.getStackTrace()[0].getMethodName(), e);
		}
	}

	@Override
	public String commitTransaction(final String id) {
		try {
			final Long tid = new Long(id);
			final Long roid = bimsie1LowLevelInterface.commitTransaction(tid,
					"");
			return roid.toString();
		} catch (final Throwable e) {
			final Exception ecc = new Exception();
			throw new BimError("error in "
					+ ecc.getStackTrace()[0].getMethodName(), e);
		}
	}

	@Override
	public void abortTransaction(final String transactionId) {
		try {
			final Long tid = new Long(transactionId);
			bimsie1LowLevelInterface.abortTransaction(tid);
		} catch (final Throwable e) {
			final Exception ecc = new Exception();
			throw new BimError("error in "
					+ ecc.getStackTrace()[0].getMethodName(), e);
		}
	}

	@Override
	public String createObject(final String transactionId,
			final String className) {
		try {
			final Long tid = new Long(transactionId);
			final Long oid = bimsie1LowLevelInterface.createObject(tid,
					className);
			return oid.toString();
		} catch (final Throwable e) {
			final Exception ecc = new Exception();
			throw new BimError("error in "
					+ ecc.getStackTrace()[0].getMethodName(), e);
		}
	}

	@Override
	public void removeObject(final String transactionId, final String objectId) {
		try {
			final Long tid = new Long(transactionId);
			final Long oid = new Long(objectId);
			bimsie1LowLevelInterface.removeObject(tid, oid);
		} catch (final Throwable e) {
			final Exception ecc = new Exception();
			throw new BimError("error in "
					+ ecc.getStackTrace()[0].getMethodName(), e);
		}
	}

	@Override
	public void removeReference(final String transactionId,
			final String objectId, final String attributeName, final int index) {
		try {
			final Long tid = new Long(transactionId);
			final Long oid = new Long(objectId);
			bimsie1LowLevelInterface.removeReference(tid, oid, attributeName,
					index);
		} catch (final Throwable e) {
			final Exception ecc = new Exception();
			throw new BimError("error in "
					+ ecc.getStackTrace()[0].getMethodName(), e);
		}

	}

	@Override
	public void unsetReference(final String transactionId,
			final String objectId, final String referenceName) {
		try {
			final Long tid = new Long(transactionId);
			final Long oid = new Long(objectId);
			bimsie1LowLevelInterface.unsetReference(tid, oid, referenceName);
		} catch (final Throwable e) {
			final Exception ecc = new Exception();
			throw new BimError("error in "
					+ ecc.getStackTrace()[0].getMethodName(), e);
		}
	}

	@Override
	public void setStringAttribute(final String transactionId,
			final String objectId, final String attributeName,
			final String value) {
		try {
			final Long tid = new Long(transactionId);
			final Long oid = new Long(objectId);
			bimsie1LowLevelInterface.setStringAttribute(tid, oid,
					attributeName, value);
		} catch (final Throwable e) {
			final Exception ecc = new Exception();
			throw new BimError("error in "
					+ ecc.getStackTrace()[0].getMethodName(), e);
		}
	}

	@Override
	public void addStringAttribute(final String transactionId,
			final String objectId, final String attributeName,
			final String value) {
		try {
			final Long tid = new Long(transactionId);
			final Long oid = new Long(objectId);
			bimsie1LowLevelInterface.addStringAttribute(tid, oid,
					attributeName, value);
		} catch (final Throwable e) {
			final Exception ecc = new Exception();
			throw new BimError("error in "
					+ ecc.getStackTrace()[0].getMethodName(), e);
		}
	}

	@Override
	public void setReference(final String transactionId, final String objectId,
			final String referenceName, final String relatedObjectId) {
		try {
			final Long tid = new Long(transactionId);
			final Long oid1 = new Long(objectId);
			final Long oid2 = new Long(relatedObjectId);
			bimsie1LowLevelInterface.setReference(tid, oid1, referenceName,
					oid2);
		} catch (final Throwable e) {
			final Exception ecc = new Exception();
			throw new BimError("error in "
					+ ecc.getStackTrace()[0].getMethodName(), e);
		}
	}

	@Override
	public void addReference(final String transactionId, final String objectId,
			final String relationName, final String referenceId) {
		try {
			final Long tid = new Long(transactionId);
			final Long oid = new Long(objectId);
			final Long refid = new Long(referenceId);
			bimsie1LowLevelInterface
					.addReference(tid, oid, relationName, refid);
		} catch (final Throwable e) {
			final Exception ecc = new Exception();
			throw new BimError("error in "
					+ ecc.getStackTrace()[0].getMethodName(), e);
		}
	}

	@Override
	public void addDoubleAttribute(final String transactionId,
			final String objectId, final String attributeName,
			final double value) {
		try {
			final Long tid = new Long(transactionId);
			final Long oid = new Long(objectId);
			bimsie1LowLevelInterface.addDoubleAttribute(tid, oid,
					attributeName, value);
		} catch (final Throwable e) {
			final Exception ecc = new Exception();
			throw new BimError("error in "
					+ ecc.getStackTrace()[0].getMethodName(), e);
		}
	}

	@Override
	public void disableProject(final String projectId) {
		try {
			final Long poid = new Long(projectId);
			bimsie1ServiceInterface.deleteProject(poid);
		} catch (final Throwable e) {
			final Exception ecc = new Exception();
			throw new BimError("error in "
					+ ecc.getStackTrace()[0].getMethodName(), e);
		}
	}

	@Override
	public void enableProject(final String projectId) {
		try {
			final Long poid = new Long(projectId);
			bimsie1ServiceInterface.undeleteProject(poid);
		} catch (final Throwable e) {
			final Exception ecc = new Exception();
			throw new BimError("error in "
					+ ecc.getStackTrace()[0].getMethodName(), e);
		}
	}

	@Override
	public BimUser addUser(final String username, final String name,
			final String type) {
		try {
			final SUser suser = serviceInterface.addUser(username, name,
					SUserType.ADMIN, false, null);
			authenticationInterface.changePassword(suser.getOid(), null, name);
			final BimUser user = new BimserverUser(suser);

			return user;
		} catch (final Throwable e) {
			final Exception ecc = new Exception();
			throw new BimError("error in "
					+ ecc.getStackTrace()[0].getMethodName(), e);
		}
	}

	@Override
	public BimUser getUserFromName(final String username) {
		try {
			final SUser suser = serviceInterface.getUserByUserName(username);
			final BimUser user = new BimserverUser(suser);
			return user;
		} catch (final Throwable e) {
			final Exception ecc = new Exception();
			throw new BimError("error in "
					+ ecc.getStackTrace()[0].getMethodName(), e);
		}
	}

	@Override
	public void changePassword(final String username, final String newPassword,
			final String oldPassword) {
		final Exception e = new Exception();
		throw new BimError("Not implemented", e);

	}

	@Override
	public BimUser getUserFromId(final String userId) {
		try {
			final Long uoid = new Long(userId);
			final SUser user = serviceInterface.getUserByUoid(uoid);
			return new BimserverUser(user);
		} catch (final Throwable e) {
			final Exception ecc = new Exception();
			throw new BimError("error in "
					+ ecc.getStackTrace()[0].getMethodName(), e);
		}
	}

}
