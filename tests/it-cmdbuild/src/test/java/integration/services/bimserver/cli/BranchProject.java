package integration.services.bimserver.cli;

import java.io.File;
import java.net.URL;
import java.util.List;

import static org.junit.Assert.*;

import org.cmdbuild.bim.model.Entity;
import org.cmdbuild.bim.service.BimProject;
import org.cmdbuild.bim.service.BimService;
import org.cmdbuild.bim.service.bimserver.BimserverClient;
import org.cmdbuild.bim.service.bimserver.BimserverConfiguration;
import org.cmdbuild.bim.service.bimserver.BimserverService;
import org.cmdbuild.bim.service.bimserver.DefaultBimserverClient;
import org.cmdbuild.bim.service.bimserver.SmartBimserverClient;
import org.junit.Before;
import org.junit.Test;

public class BranchProject {
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

			@Override
			public void disable() {
			}
		};
		client = new SmartBimserverClient(new DefaultBimserverClient(configuration));
		service = new BimserverService(client);
	}

	@Test
	public void branchTwiceAProjectDoublesTheObjects() throws Exception {

		String name = "pippo";
		BimProject p = service.createProject(name);
		System.out.println("project " + p.getIdentifier() + " " + p.getName() +" created");
		
		String filename = "CMDB_empty.ifc";
		URL url = ClassLoader.getSystemResource(filename);
		File file = new File(url.toURI());
		System.out.println("Checkin file " + file.getName() + " on project " + p.getIdentifier() + "...");
		service.checkin(p.getIdentifier(), file, false);
		System.out.println("File " + file.getName() + " loaded");
		
		BimProject shapes = service.createProject("shapes-"+name);
		System.out.println("project " + shapes.getIdentifier() + " " + shapes.getName() +" created");
		filename = "Shapes.ifc";
		url = ClassLoader.getSystemResource(filename);
		file = new File(url.toURI());
		System.out.println("Checkin file " + file.getName() + " on project " + shapes.getName() + "...");
		service.checkin(shapes.getIdentifier(), file, false);
		System.out.println("File " + file.getName() + " loaded");
		
		BimProject merged = service.createProject(name+"_merged");
		System.out.println("project " + merged.getIdentifier() + " " + merged.getName() +" created");
		
		BimProject shapesSub = service.createSubProject(name+"_shapes", merged.getIdentifier());
		System.out.println("project " + shapesSub.getIdentifier() + " " + shapesSub.getName() +" created");
		
		BimProject branchedSub = service.createSubProject(name+"_branched", merged.getIdentifier());
		System.out.println("project " + branchedSub.getIdentifier() + " " + branchedSub.getName() +" created");
		
		service.branchRevisionToExistingProject(service.getProjectByName("shapes-"+name).getLastRevisionId(), shapesSub.getIdentifier());
		System.out.println("revision " + service.getProjectByName("shapes-"+name).getLastRevisionId() + " branched to " + shapesSub.getIdentifier());
		
		service.branchRevisionToExistingProject(service.getProjectByName(name).getLastRevisionId(), branchedSub.getIdentifier());
		System.out.println("revision " + service.getProjectByName(name).getLastRevisionId() + " branched to " + branchedSub.getIdentifier());
				
		service.branchRevisionToExistingProject(service.getProjectByName("shapes-"+name).getLastRevisionId(), shapesSub.getIdentifier());
		System.out.println("revision " + service.getProjectByName("shapes-"+name).getLastRevisionId() + " branched again to " + shapesSub.getIdentifier());
		
		service.branchRevisionToExistingProject(service.getProjectByName(name).getLastRevisionId(), branchedSub.getIdentifier());
		System.out.println("revision " + service.getProjectByName(name).getLastRevisionId() + " branched again to " + branchedSub.getIdentifier());
		
	}

}
