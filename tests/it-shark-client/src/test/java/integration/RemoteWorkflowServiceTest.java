package integration;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import java.util.UUID;

import org.cmdbuild.workflow.CMWorkflowException;
import org.cmdbuild.workflow.service.CMWorkflowService;
import org.cmdbuild.workflow.service.RemoteSharkService;
import org.cmdbuild.workflow.xpdl.XPDLPackageFactory;
import org.enhydra.jxpdl.elements.Package;
import org.enhydra.shark.api.common.SharkConstants;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Smoke tests to be reasonably sure that the web connection works just like
 * the local one. This is not tested throughly because we assume that it
 * is going to work just like the embedded Shark instance.
 */
public class RemoteWorkflowServiceTest {

	private static String USERNAME = "admin";
	private static String PASSWORD = "enhydra";
	private static String SERVER_HOST = "localhost";
	private static int SERVER_PORT = 8080;
	/**
	 * The Tomcat plugin deploys the webapp as the artifact id.
	 * It has to change when the artifact id changes.
	 */
	private static String WEBAPP_NAME = "it-shark-client";

	private static CMWorkflowService ws;
	private String pkgId;

	@BeforeClass
	public static void initWorkflowService() {
		ws = new RemoteSharkService(new RemoteSharkService.Config() {
			public String getServerUrl() {
				return String.format("http://%s:%d/%s", SERVER_HOST, SERVER_PORT, WEBAPP_NAME);
			}
			public String getUsername() {
				return USERNAME;
			}
			public String getPassword() {
				return PASSWORD;
			}
		});
	}

	@Before
	public void createRandomPackageName() {
		pkgId = UUID.randomUUID().toString();
	}

	@Test
	public void packagesCanBeUploadedAndDownloaded() throws CMWorkflowException {
		assertEquals(0, ws.getPackageVersions(pkgId).length);

		Package pkg = new Package();
		pkg.setId(pkgId);
		pkg.getScript().setType(SharkConstants.GRAMMAR_JAVA);
		pkg.getPackageHeader().setXPDLVersion("2.1");
		pkg.setName("n1");
		ws.uploadPackage(pkgId, XPDLPackageFactory.xpdlByteArray(pkg));

		pkg.getPackageHeader().setXPDLVersion("1.0");
		pkg.setName("n2");
		ws.uploadPackage(pkgId, XPDLPackageFactory.xpdlByteArray(pkg));

		assertEquals(2, ws.getPackageVersions(pkgId).length);

		pkg = XPDLPackageFactory.readXpdl(ws.downloadPackage(pkgId, "1"));
		assertThat(pkg.getName(), is("n1"));
	}

}
