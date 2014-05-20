package integration.services.bimserver.cli;

import java.io.File;
import java.net.URL;
import java.util.List;

import org.apache.commons.lang3.RandomStringUtils;
import org.cmdbuild.bim.model.Entity;
import org.cmdbuild.bim.service.BimProject;
import org.cmdbuild.bim.service.BimRevision;
import org.cmdbuild.bim.service.BimService;
import org.cmdbuild.bim.service.bimserver.BimserverClient;
import org.cmdbuild.bim.service.bimserver.BimserverConfiguration;
import org.cmdbuild.bim.service.bimserver.BimserverService;
import org.cmdbuild.bim.service.bimserver.DefaultBimserverClient;
import org.cmdbuild.bim.service.bimserver.SmartBimserverClient;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.Iterables;

public class BimserverCli {

	private BimService service;
	private BimserverClient client;
	private final String url = "http://localhost:10080";
	private final String username = "admin@tecnoteca.com";
	private final String password = "admin";

	@Before
	public void setUp() {
		final BimserverConfiguration configuration = new BimserverConfiguration() {

			@Override
			public boolean isEnabled() {
				return true;
			}

			@Override
			public String getUsername() {
				return username;
			}

			@Override
			public String getUrl() {
				return url;
			}

			@Override
			public String getPassword() {
				return password;
			}

			@Override
			public void addListener(final ChangeListener listener) {
			}

			@Override
			public void disable() {
			}
		};
		client = new SmartBimserverClient(new DefaultBimserverClient(configuration));
		service = new BimserverService(client);
	}

	@Test
	public void ping() throws Exception {

	}

	@Test
	public void checkinOnProject() throws Exception {
		final String project = "262145";
		final String filename = "DuplexApartment_MEP_Optimized.ifc";
		final URL url = ClassLoader.getSystemResource(filename);
		final File file = new File(url.toURI());
		System.out.println("Checkin file " + file.getName() + " on project " + project + "...");
		service.checkin(project, file, false);
		System.out.println("File " + file.getName() + " loaded");
	}

	@Test
	public void createProject() throws Exception {
		final String projectName = "CasoDiStudio_export_65539";
		System.out.println("Creating project " + projectName + "...");
		service.createProject(projectName);
		System.out.println("Project " + service.getProjectByName(projectName) + " created");
	}

	@Test
	public void createProjectAsSubproject() throws Exception {
		final String projectName = "CasoDiStudio_65539_shapes";
		final String parentId = "327681";
		System.out.println("Creating project " + projectName + " as subproject of" + parentId + "...");
		service.createSubProject(projectName, parentId);
		System.out.println("Project " + service.getProjectByName(projectName) + " created");
	}

	@Test
	public void branchToNew() throws Exception {
		final String projectName = "CasoDiStudio_merged_65539";
		final String projectId = "131073";
		System.out.println("Branching last revision of project " + projectId + " into new project " + projectName
				+ "...");
		final String revisionId = service.getLastRevisionOfProject(projectId);
		service.branchRevisionToNewProject(revisionId, projectName);
	}

	@Test
	public void branchToExisting() throws Exception {
		final String destinationProjectId = "524289";
		final String projectId = "327681";
		System.out.println("Branching last revision of project " + projectId + " into project " + destinationProjectId
				+ "...");
		System.out.println("Start branching at " + new DateTime());
		final String revisionId = service.getLastRevisionOfProject(projectId);
		System.out.println("Branch created at " + new DateTime());
		service.branchRevisionToExistingProject(revisionId, destinationProjectId);
	}

	@Test
	public void downloadLastRevisionOfProject() throws Exception {
		final String projectId = "786433";
		System.out.println("Download last revision of project " + projectId + "...");
		service.downloadLastRevisionOfProject(projectId);
	}

	@Test
	public void downloadRevision() throws Exception {
		final String revisionId = "262147";
		System.out.println("Download revision " + revisionId + "...");
		service.downloadIfc(revisionId);
		System.out.println("Revision " + revisionId + " downloaded");
	}

	@Test
	public void getObjectsFromRevision() throws Exception {
		final String revisionId = "393219";
		final String ifcType = "IfcBuilding";

		final Iterable<Entity> entityList = service.getEntitiesByType(revisionId, ifcType);
		if (Iterables.size(entityList) == 0) {
			System.out.println("No objects of type " + ifcType + " found");
		}
		for (final Entity e : entityList) {
			System.out.println(e.getKey());
		}

	}

	@Test
	public void getObjectsFromLastRevisionOfProject() throws Exception {
		final String projectId = "327681";
		final String ifcType = "IfcProductDefinitionShape";

		final String revisionId = service.getLastRevisionOfProject(projectId);
		final Iterable<Entity> entityList = service.getEntitiesByType(revisionId, ifcType);
		if (Iterables.size(entityList) == 0) {
			System.out.println("No objects of type " + ifcType + " found");
		} else {
			for (final Entity e : entityList) {
				System.out.println(e.getKey());
			}
		}
	}

	@Test
	public void list() throws Exception {
		System.out.println("\n\n\n");
		for (final BimProject project : service.getAllProjects()) {
			System.out.println("\n" + project);
			final List<BimRevision> revisions = service.getRevisionsOfProject(project);
			if (revisions == null || revisions.size() == 0) {
				System.out.println("- no revisions");
			}
			for (final BimRevision revision : revisions) {
				System.out.println("* revisionId " + revision.getIdentifier() + "   date " + revision.getDate());
			}
		}
	}

	@Test
	public void setUpForExport() throws Exception {
		for (final BimProject project : service.getAllProjects()) {
			if (project.getName().equals("INT-Store") || project.getName().startsWith("_cm")) {
				continue;
			}
			final String wipProjectId = service.createProject("_cm_" + project.getName()).getIdentifier();
			final String shapeProjectId = service.createSubProject("shapes", wipProjectId).getIdentifier();
			final String shape1ProjectId = service.createSubProject("shape1", shapeProjectId).getIdentifier();
			final String filename = "cuboShape.ifc";
			final URL url = ClassLoader.getSystemResource(filename);
			final File file = new File(url.toURI());
			System.out.println("Checkin file " + file.getName() + " on project " + project + "...");
			service.checkin(shape1ProjectId, file);
		}
	}

	@Test
	public void mergeTwoProjectsGenerateTheMergedRevisionOnTheParentProject() throws Exception {
		final BimProject masterPj = service.createProject("Parent-" + RandomStringUtils.randomAlphanumeric(4));
		final String masterId = masterPj.getIdentifier();
		System.out.println("master id " + masterId);
		final BimProject son1Pj = service.createSubProject("Son1", masterPj.getIdentifier());
		final BimProject son2Pj = service.createSubProject("Son2", masterPj.getIdentifier());

		final String filename1 = "_pc.ifc";
		final URL url1 = ClassLoader.getSystemResource(filename1);
		final File file1 = new File(url1.toURI());
		service.checkin(son2Pj.getIdentifier(), file1);
		// service.downloadIfc(service.getProjectByPoid(son2Pj.getIdentifier()).getLastRevisionId());

		final String filename = "CMDB_empty.ifc";
		final URL url = ClassLoader.getSystemResource(filename);
		final File file = new File(url.toURI());
		service.checkin(son1Pj.getIdentifier(), file);
		// service.downloadIfc(service.getProjectByPoid(son1Pj.getIdentifier()).getLastRevisionId());

		service.downloadIfc(service.getLastRevisionOfProject(masterId));
	}

	@Test
	public void mergeTwoProjectsAndAddObjectsToTheMergedRevision() throws Exception {
		final String suffix = RandomStringUtils.randomAlphanumeric(8);

		final BimProject masterPj = service.createProject("Parent-" + suffix);
		final String masterId = masterPj.getIdentifier();
		System.out.println("master id " + masterId);
		final BimProject son1Pj = service.createSubProject("Son1-" + suffix, masterPj.getIdentifier());
		final BimProject son2Pj = service.createSubProject("Son2" + suffix, masterPj.getIdentifier());

		final String filename1 = "_pc.ifc";
		final URL url1 = ClassLoader.getSystemResource(filename1);
		final File file1 = new File(url1.toURI());
		service.checkin(son2Pj.getIdentifier(), file1);

		final String filename = "CMDB_empty.ifc";
		final URL url = ClassLoader.getSystemResource(filename);
		final File file = new File(url.toURI());
		service.checkin(son1Pj.getIdentifier(), file);

		service.branchRevisionToNewProject(service.getLastRevisionOfProject(masterId), "Merged-" + suffix);
		final String mergedProjectId = service.getProjectByName("Merged-" + suffix).getIdentifier();

		service.downloadIfc(service.getLastRevisionOfProject(mergedProjectId));

		final String transactionId = service.openTransaction(mergedProjectId);
		final String objectId = service.createObject(transactionId, "IfcBuildingElementProxy");
		service.setStringAttribute(transactionId, objectId, "Name", "Anna");
		service.commitTransaction(transactionId);

		service.downloadIfc(service.getLastRevisionOfProject(mergedProjectId));
	}

	@Test
	public void customSetup() throws Exception {
		final BimProject masterPj = service.createProject("Cmdb");
		String filename = "CMDB_empty.ifc";
		URL url = ClassLoader.getSystemResource(filename);
		File file = new File(url.toURI());
		service.checkin(masterPj.getIdentifier(), file);

		final BimProject wipPj = service.createProject("_cm_Cmdb");
		final BimProject shapesPj = service.createSubProject("_cm_Cmdb_shapes", wipPj.getIdentifier());
		final BimProject tmpPj = service.createSubProject("_cm_Cmdb_tmp", wipPj.getIdentifier());
		final BimProject pcshapePj = service.createSubProject("_cm_Cmdb_shapes_pc", shapesPj.getIdentifier());

		filename = "_pc.ifc";
		url = ClassLoader.getSystemResource(filename);
		file = new File(url.toURI());
		service.checkin(pcshapePj.getIdentifier(), file);
	}

	@Test
	public void loadShapes() throws Exception {
		final String shapeProjectId = service.createProject("_shapes").getIdentifier();
		final String filename = "Shapes.ifc";
		final URL url = ClassLoader.getSystemResource(filename);
		final File file = new File(url.toURI());
		service.checkin(shapeProjectId, file);
	}

}
