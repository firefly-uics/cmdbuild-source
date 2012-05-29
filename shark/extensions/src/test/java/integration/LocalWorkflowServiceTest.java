package integration;

import static utils.XpdlTestUtils.randomName;

import org.cmdbuild.workflow.service.CMWorkflowService;
import org.cmdbuild.workflow.service.LocalSharkService;
import org.cmdbuild.workflow.xpdl.XpdlDocument;
import org.cmdbuild.workflow.xpdl.XpdlException;
import org.cmdbuild.workflow.xpdl.XpdlPackageFactory;
import org.cmdbuild.workflow.xpdl.XpdlDocument.ScriptLanguage;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.rules.TestRule;

import utils.WriteXpdlOnFailure;
import utils.XpdlTest;

public abstract class LocalWorkflowServiceTest implements XpdlTest {

	private static final String USERNAME = "admin";

	protected static CMWorkflowService ws;

	protected String packageId;
	protected XpdlDocument xpdlDocument;

	@Rule
	public TestRule testWatcher = new WriteXpdlOnFailure(this);

	@BeforeClass
	public static void initWorkflowService() {
		ws = new LocalSharkService(new LocalSharkService.Config() {
			public String getUsername() {
				return USERNAME;
			}
		});
	}

	@Before
	public void createRandomPackageName() throws Exception {
		packageId = randomName();
		xpdlDocument = newXpdl(packageId);
	}

	@Override
	public XpdlDocument getXpdlDocument() {
		return xpdlDocument;
	}

	/**
	 * Creates a new {@link XpdlDocument} with default scripting language
	 * {@code ScriptLanguages.JAVA}.
	 */
	protected final XpdlDocument newXpdl(final String packageId) throws XpdlException {
		final XpdlDocument xpdl = new XpdlDocument(packageId);
		xpdl.setDefaultScriptingLanguage(ScriptLanguage.JAVA);
		return xpdl;
	}

	/**
	 * Serializes an {@link XpdlDocument} in a byte array.
	 */
	protected final byte[] serialize(final XpdlDocument xpdl) throws XpdlException {
		return XpdlPackageFactory.xpdlByteArray(xpdl.getPkg());
	}

}