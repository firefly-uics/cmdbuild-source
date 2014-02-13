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

public class AddObjects {

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
		
		BimProject pjParent = service.createProject("Pj1-Parent");
		BimProject pjSon = service.createSubProject("Pj1-Son",pjParent.getIdentifier());
		URL url = ClassLoader.getSystemResource("CMDB_empty.ifc");
		File file = new File(url.toURI());
		service.checkin(pjSon.getIdentifier(), file);
		
		String transactionId = service.openTransaction(pjParent.getIdentifier());
		String objectId = service.createObject(transactionId, "IfcBuildingElementProxy");
		service.setStringAttribute(transactionId, objectId, "Description", "Pippo!");
		String revisionId = service.commitTransaction(transactionId);
		service.downloadIfc(revisionId);
	}
	
	

}
