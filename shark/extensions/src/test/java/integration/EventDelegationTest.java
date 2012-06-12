package integration;

import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static utils.XpdlTestUtils.randomName;

import org.apache.commons.lang.StringUtils;
import org.cmdbuild.workflow.CMEventManager;
import org.cmdbuild.workflow.xpdl.XpdlActivity;
import org.cmdbuild.workflow.xpdl.XpdlDocument.ScriptLanguage;
import org.cmdbuild.workflow.xpdl.XpdlProcess;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mockito;

import utils.AbstractLocalSharkServiceTest;
import utils.MockEventsDelegator;

public class EventDelegationTest extends AbstractLocalSharkServiceTest {

	private XpdlProcess process;
	private CMEventManager eventManager;

	@Before
	public void createAndUploadPackage() throws Exception {
		process = xpdlDocument.createProcess(randomName());
	}

	@Before
	public void initializeEventManager() {
		eventManager = MockEventsDelegator.mock;
	}

	@After
	public void resetEventManagerMock() {
		Mockito.reset(eventManager);
	}

	@Test
	public void startScriptAndStop() throws Exception {
		final XpdlActivity activity = process.createActivity(randomName());
		activity.setScriptingType(ScriptLanguage.JAVA, StringUtils.EMPTY);

		uploadXpdlAndStartProcess(process);

		final InOrder inOrder = inOrder(eventManager);
		inOrder.verify(eventManager).processStarted(process.getId());
		inOrder.verify(eventManager).activityStarted(activity.getId());
		inOrder.verify(eventManager).activityClosed(activity.getId());
		inOrder.verify(eventManager).processClosed(process.getId());
		verifyNoMoreInteractions(eventManager);
	}

	@Test
	public void startStopsAtFirstNoImplementationActivity() throws Exception {
		// order matters for this test
		final XpdlActivity noImplActivity = process.createActivity(randomName());
		final XpdlActivity scriptActivity = process.createActivity(randomName());
		scriptActivity.setScriptingType(ScriptLanguage.JAVA, StringUtils.EMPTY);
		process.createTransition(scriptActivity, noImplActivity);

		uploadXpdlAndStartProcess(process);

		final InOrder inOrder = inOrder(eventManager);
		inOrder.verify(eventManager).processStarted(process.getId());
		inOrder.verify(eventManager).activityStarted(scriptActivity.getId());
		inOrder.verify(eventManager).activityClosed(scriptActivity.getId());
		inOrder.verify(eventManager).activityStarted(noImplActivity.getId());
		verifyNoMoreInteractions(eventManager);
	}

	@Test
	public void subflowStartAndStop() throws Exception {
		final XpdlProcess subprocess = xpdlDocument.createProcess(randomName());
		final XpdlActivity scriptActivity = subprocess.createActivity(randomName());
		scriptActivity.setScriptingType(ScriptLanguage.JAVA, StringUtils.EMPTY);

		final XpdlActivity subflowActivity = process.createActivity(randomName());
		subflowActivity.setSubProcess(subprocess);

		uploadXpdlAndStartProcess(process);

		final InOrder inOrder = inOrder(eventManager);
		inOrder.verify(eventManager).processStarted(process.getId());
		inOrder.verify(eventManager).activityStarted(subflowActivity.getId());
		inOrder.verify(eventManager).processStarted(subprocess.getId());
		inOrder.verify(eventManager).activityStarted(scriptActivity.getId());
		inOrder.verify(eventManager).activityClosed(scriptActivity.getId());
		inOrder.verify(eventManager).processClosed(subprocess.getId());
		inOrder.verify(eventManager).activityClosed(subflowActivity.getId());
		inOrder.verify(eventManager).processClosed(process.getId());
		verifyNoMoreInteractions(eventManager);
	}

}
