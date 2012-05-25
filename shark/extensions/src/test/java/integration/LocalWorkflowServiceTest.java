package integration;

import java.util.UUID;

import org.cmdbuild.workflow.service.CMWorkflowService;
import org.cmdbuild.workflow.service.LocalSharkService;
import org.cmdbuild.workflow.xpdl.XpdlDocument;
import org.cmdbuild.workflow.xpdl.XpdlException;
import org.cmdbuild.workflow.xpdl.XpdlPackageFactory;
import org.cmdbuild.workflow.xpdl.XpdlDocument.ScriptLanguages;
import org.junit.Before;
import org.junit.BeforeClass;

public abstract class LocalWorkflowServiceTest {

	private static String USERNAME = "admin";

	protected static CMWorkflowService ws;
	protected String pkgId;

	@BeforeClass
	public static void initWorkflowService() {
		ws = new LocalSharkService(new LocalSharkService.Config() {
			public String getUsername() {
				return USERNAME;
			}
		});
	}

	@Before
	public void createRandomPackageName() {
		pkgId = randomPackageName();
	}

	protected final String randomPackageName() {
		return UUID.randomUUID().toString();
	}

	protected final byte[] createXpdl(final String packageId) throws XpdlException {
		XpdlDocument xpdl = new XpdlDocument(packageId);
		xpdl.setDefaultScriptingLanguage(ScriptLanguages.JAVA);
		return XpdlPackageFactory.xpdlByteArray(xpdl.getPkg());
	}

}