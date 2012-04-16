package integration;

import static org.junit.Assert.assertEquals;

import java.util.UUID;

import org.cmdbuild.workflow.CMWorkflowException;
import org.cmdbuild.workflow.service.CMWorkflowService;
import org.cmdbuild.workflow.service.LocalSharkService;
import org.junit.Test;

public class LocalWorkflowServiceTest {

	private static String USERNAME = "admin";

	@Test
	public void packageVersionsAreRetrieved() throws CMWorkflowException {
		final String packageName = randomPackageName();
		CMWorkflowService ws = newLocalWorkflowService();
		String[] versions = ws.getPackageVersions(packageName);
		assertEquals(0, versions.length);
	}

	private CMWorkflowService newLocalWorkflowService() {
		return new LocalSharkService(new LocalSharkService.Config() {
			public String getUsername() {
				return USERNAME;
			}
		});
	}

	private String randomPackageName() {
		return UUID.randomUUID().toString();
	}
}
