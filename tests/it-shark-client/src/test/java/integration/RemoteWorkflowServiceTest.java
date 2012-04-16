package integration;

import static org.junit.Assert.assertEquals;

import java.util.UUID;

import org.cmdbuild.workflow.CMWorkflowException;
import org.cmdbuild.workflow.service.CMWorkflowService;
import org.cmdbuild.workflow.service.RemoteSharkService;
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

	@Test
	public void packageVersionsAreRetrieved() throws CMWorkflowException {
		final String packageName = randomPackageName();
		CMWorkflowService ws = newRemoteWorkflowService();
		String[] versions = ws.getPackageVersions(packageName);
		assertEquals(0, versions.length);
	}

	private CMWorkflowService newRemoteWorkflowService() {
		return new RemoteSharkService(new RemoteSharkService.Config() {
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

	private String randomPackageName() {
		return UUID.randomUUID().toString();
	}
}
