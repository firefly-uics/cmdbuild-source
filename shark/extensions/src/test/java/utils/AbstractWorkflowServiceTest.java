package utils;

import static utils.XpdlTestUtils.randomName;

import org.cmdbuild.workflow.CMWorkflowException;
import org.cmdbuild.workflow.service.CMWorkflowService;
import org.cmdbuild.workflow.xpdl.XpdlDocument;
import org.cmdbuild.workflow.xpdl.XpdlDocument.ScriptLanguage;
import org.cmdbuild.workflow.xpdl.XpdlException;
import org.cmdbuild.workflow.xpdl.XpdlPackageFactory;
import org.cmdbuild.workflow.xpdl.XpdlProcess;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.TestRule;

public class AbstractWorkflowServiceTest implements XpdlTest {

	protected static CMWorkflowService ws;
	protected XpdlDocument xpdlDocument;

	@Rule
	public TestRule testWatcher = new WriteXpdlOnFailure(this);

	public AbstractWorkflowServiceTest() {
		super();
	}

	@Before
	public void createXpdlDocument() throws Exception {
		xpdlDocument = newXpdl(randomName());
	}

	@Override
	public XpdlDocument getXpdlDocument() {
		return xpdlDocument;
	}

	/*
	 * Utils
	 */

	/**
	 * Creates a new {@link XpdlDocument} with default scripting language
	 * {@code ScriptLanguages.JAVA}.
	 */
	protected final XpdlDocument newXpdl(final String packageId) throws XpdlException {
		final XpdlDocument xpdl = newXpdlNoScriptingLanguage(packageId);
		xpdl.setDefaultScriptingLanguage(ScriptLanguage.JAVA);
		return xpdl;
	}

	/**
	 * Creates a new {@link XpdlDocument} with no default scripting language.
	 */
	protected final XpdlDocument newXpdlNoScriptingLanguage(final String packageId) throws XpdlException {
		return new XpdlDocument(packageId);
	}

	/**
	 * Uploads the {@link XpdlDocument} and starts the specified
	 * {@link XpdlProcess}.
	 * 
	 * @return the process instance's id
	 */
	protected final String uploadXpdlAndStartProcess(final XpdlProcess xpdlProcess) throws CMWorkflowException,
			XpdlException {
		upload(xpdlDocument);
		return ws.startProcess(xpdlDocument.getPackageId(), xpdlProcess.getId());
	}

	/**
	 * Uploads an {@link XpdlDocument}.
	 * 
	 * @throws CMWorkflowException
	 * @throws XpdlException
	 */
	protected final void upload(final XpdlDocument xpdlDocument) throws XpdlException, CMWorkflowException {
		ws.uploadPackage(xpdlDocument.getPackageId(), serialize(xpdlDocument));
	}

	/**
	 * Serializes an {@link XpdlDocument} in a byte array.
	 */
	protected final byte[] serialize(final XpdlDocument xpdl) throws XpdlException {
		return XpdlPackageFactory.xpdlByteArray(xpdl.getPkg());
	}

}