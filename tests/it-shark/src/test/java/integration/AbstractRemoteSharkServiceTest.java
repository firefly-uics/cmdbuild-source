package integration;

import java.io.File;

import org.apache.commons.lang.SystemUtils;
import org.cmdbuild.workflow.service.RemoteSharkService;
import org.junit.BeforeClass;

import utils.AbstractRemoteWorkflowServiceTest;

public abstract class AbstractRemoteSharkServiceTest extends AbstractRemoteWorkflowServiceTest {

	/**
	 * The Tomcat plugin deploys the webapp as the artifact id. It has to change
	 * when the artifact id changes.
	 */
	private static String WEBAPP_NAME = "it-shark";

	/**
	 * Defined in the {@code Shark.conf} configuration file.
	 */
	protected static File LOGFILE = new File(SystemUtils.getJavaIoTmpDir(), "it-shark-4.4.log");

	@Override
	protected File getLogFile() {
		return LOGFILE;
	}

	@BeforeClass
	public static void initWorkflowService() {
		ws = new RemoteSharkService(new RemoteSharkService.Config() {
			@Override
			public String getServerUrl() {
				return String.format("http://%s:%d/%s", SERVER_HOST, SERVER_PORT, WEBAPP_NAME);
			}

			@Override
			public String getUsername() {
				return USERNAME;
			}

			@Override
			public String getPassword() {
				return PASSWORD;
			}
		});
	}

}
