package integration.services.bimserver.cli;

import java.io.File;
import java.net.URL;
import java.util.List;

import org.cmdbuild.bim.model.Attribute;
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

public class MergingShapes {

	private BimService service;
	private BimserverClient client;
	private final String url = "http://localhost:10080";
	private final String username = "admin@tecnoteca.com";
	private final String password = "admin";

	@Before
	public void setUp() {
		BimserverConfiguration configuration = new BimserverConfiguration() {

			@Override
			public boolean isEnabled() {
				return true;
			}
			
			@Override
			public void disable() {
				throw new UnsupportedOperationException("TODO");			
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
			public void addListener(ChangeListener listener) {
			}
		};
		client = new SmartBimserverClient(new DefaultBimserverClient(configuration));
		service = new BimserverService(client);
		System.out.println("Connection established\n");
	}

	@Test
	public void mergeRevisions() throws Exception {
		String projectId = service.createProject("ParentNew-" + new DateTime()).getIdentifier();
		String childId1 = service.createSubProject("child1", projectId).getIdentifier();
		String childId2 = service.createSubProject("child2", projectId).getIdentifier();

		String fileName1 = "10.ifc";
		checkinOnProject(childId1, fileName1);
		service.downloadLastRevisionOfProject(childId1);

		String fileName2 = "_pc.ifc";
		checkinOnProject(childId2, fileName2);
		service.downloadLastRevisionOfProject(childId2);

		service.downloadLastRevisionOfProject(projectId);

	}
	
	@Test	
	public void findShapeWithName() throws Exception {
		String shapeName = "_pc"; 
		String revisionId = "262147";
		List<Entity> shapeList = service.getEntitiesByType(revisionId, "IfcProductDefinitionShape");
		for(Entity shape : shapeList){
			Attribute shapeNameAttribute = shape.getAttributeByName("Name");
			if(shapeNameAttribute.getValue() != null && shapeNameAttribute.getValue().equals(shapeName)){
				System.out.println("Shape found with id " + shape.getKey());
				break;
			}
		}
	}
	

	private void checkinOnProject(String project, String filename) throws Exception {
		final URL url = ClassLoader.getSystemResource(filename);
		File file = new File(url.toURI());
		System.out.println("Checkin file " + file.getName() + " on project " + project + "...");
		service.checkin(project, file, false);
	}
	
	
	
	
	

	public void list() throws Exception {
		System.out.println("\n\n\n");
		for (BimProject project : service.getAllProjects()) {
			System.out.println("\n" + project);
			List<BimRevision> revisions = service.getRevisionsOfProject(project);
			if (revisions == null || revisions.size() == 0) {
				System.out.println("- no revisions");
			}
			for (BimRevision revision : revisions) {
				System.out.println("* revisionId " + revision.getIdentifier() + "   date " + revision.getDate());
			}
		}
	}

}
