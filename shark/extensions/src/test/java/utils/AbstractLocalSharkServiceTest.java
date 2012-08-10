package utils;

import org.cmdbuild.workflow.service.LocalSharkService;
import org.junit.BeforeClass;

public abstract class AbstractLocalSharkServiceTest extends AbstractWorkflowServiceTest {

	private static final String USERNAME = "admin";

	@BeforeClass
	public static void initWorkflowService() {
		ws = new LocalSharkService(new LocalSharkService.Config() {
			@Override
			public String getUsername() {
				return USERNAME;
			}
		});
	}

}