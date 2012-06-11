package utils;

import static org.apache.commons.lang.StringUtils.EMPTY;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.SystemUtils;
import org.cmdbuild.workflow.service.RemoteSharkService;
import org.junit.Before;
import org.junit.BeforeClass;

public abstract class AbstractRemoteWorkflowServiceTest extends AbstractWorkflowServiceTest {

	private static String USERNAME = "admin";
	private static String PASSWORD = "enhydra";
	private static String SERVER_HOST = "localhost";
	private static int SERVER_PORT = 8080;
	/**
	 * The Tomcat plugin deploys the webapp as the artifact id. It has to change
	 * when the artifact id changes.
	 */
	private static String WEBAPP_NAME = "it-shark";

	/**
	 * This file is defined in the {@code Shark.conf} configuration file.
	 */
	protected static File LOGFILE = new File(SystemUtils.getJavaIoTmpDir(), "it-shark-4.4.log");

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

	@Before
	public void cleanLogFile() throws Exception {
		FileUtils.writeStringToFile(LOGFILE, EMPTY);
	}

	protected final boolean hasLine(final String match) throws IOException {
		final List<String> lines = FileUtils.readLines(LOGFILE);
		for (final String line : lines) {
			if (line.equals(match)) {
				return true;
			}
		}
		return false;
	}
}
