package integration;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static utils.XpdlTestUtils.randomName;

import org.cmdbuild.workflow.CMEventManager;
import org.cmdbuild.workflow.xpdl.XpdlActivity;
import org.cmdbuild.workflow.xpdl.XpdlProcess;
import org.cmdbuild.workflow.xpdl.XpdlDocument.ScriptLanguage;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import utils.MockEventsDelegator;

public class EventDelegationTest extends LocalWorkflowServiceTest {

	private final String wpId = randomName();
	private final String activityId = randomName();

	private CMEventManager eventManager;

	@Before
	public void createAndUploadPackage() throws Exception {
		final XpdlProcess process = xpdlDocument.createProcess(wpId);

		final XpdlActivity activity = process.createActivity(activityId);
		final String expression = "greeting = \"hello, world\";";
		activity.setScriptingType(ScriptLanguage.JAVA, expression);

		ws.uploadPackage(xpdlDocument.getPackageId(), serialize(xpdlDocument));
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
	public void startProcessExecutesImplementationActivities() throws Exception {
		ws.startProcess(packageId, wpId);
		verify(eventManager).processStarted(wpId);
		verify(eventManager).activityStarted(activityId);
		verify(eventManager).activityClosed(activityId);
		verify(eventManager).processClosed(wpId);
		verifyNoMoreInteractions(eventManager);
	}

}
