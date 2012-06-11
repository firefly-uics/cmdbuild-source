package integration;

import static utils.XpdlTestUtils.randomName;

import org.apache.commons.lang.StringUtils;
import org.cmdbuild.workflow.xpdl.XpdlActivity;
import org.cmdbuild.workflow.xpdl.XpdlDocument.ScriptLanguage;
import org.cmdbuild.workflow.xpdl.XpdlProcess;
import org.junit.Before;
import org.junit.Test;

import utils.AbstractRemoteWorkflowServiceTest;

public class EventDelegationTest extends AbstractRemoteWorkflowServiceTest {

	private XpdlProcess process;

	@Before
	public void createAndUploadPackage() throws Exception {
		process = xpdlDocument.createProcess(randomName());
	}

	@Test
	public void startScriptAndStop() throws Exception {
		final XpdlActivity activity = process.createActivity(randomName());
		activity.setScriptingType(ScriptLanguage.JAVA, StringUtils.EMPTY);

		uploadXpdlAndStartProcess(process);
	}

	@Test
	public void startStopsAtFirstNoImplementationActivity() throws Exception {
		// order matters for this test
		final XpdlActivity noImplActivity = process.createActivity(randomName());
		final XpdlActivity scriptActivity = process.createActivity(randomName());
		scriptActivity.setScriptingType(ScriptLanguage.JAVA, StringUtils.EMPTY);
		process.createTransition(scriptActivity, noImplActivity);

		uploadXpdlAndStartProcess(process);
	}

	@Test
	public void subflowStartAndStop() throws Exception {
		final XpdlProcess subprocess = xpdlDocument.createProcess(randomName());
		final XpdlActivity scriptActivity = subprocess.createActivity(randomName());
		scriptActivity.setScriptingType(ScriptLanguage.JAVA, StringUtils.EMPTY);

		final XpdlActivity subflowActivity = process.createActivity(randomName());
		subflowActivity.setSubProcess(subprocess);

		uploadXpdlAndStartProcess(process);
	}

}
