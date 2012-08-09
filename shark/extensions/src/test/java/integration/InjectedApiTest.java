package integration;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static utils.XpdlTestUtils.randomName;

import org.cmdbuild.api.fluent.Card;
import org.cmdbuild.workflow.CMEventManager;
import org.cmdbuild.workflow.xpdl.XpdlActivity;
import org.cmdbuild.workflow.xpdl.XpdlDocument.ScriptLanguage;
import org.cmdbuild.workflow.xpdl.XpdlProcess;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import utils.AbstractLocalSharkServiceTest;
import utils.MockEventsDelegator;
import utils.MockSharkWorkflowApi;

public class InjectedApiTest extends AbstractLocalSharkServiceTest {

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
	public void apiSuccessfullyCalled() throws Exception {
		final XpdlActivity scriptActivity = process.createActivity(randomName());
		scriptActivity.setScriptingType(ScriptLanguage.JAVA, //
				"cmdb.newCard(\"Funny\")" //
						+ ".with(\"Code\", \"code\")" //
						+ ".create();");

		final XpdlActivity noImplActivity = process.createActivity(randomName());

		process.createTransition(scriptActivity, noImplActivity);

		uploadXpdlAndStartProcess(process).getProcessInstanceId();
		verify(eventManager).activityClosed(scriptActivity.getId());

		verify(MockSharkWorkflowApi.fluentApiExecutor).create(any(Card.class));
		verifyNoMoreInteractions(MockSharkWorkflowApi.fluentApiExecutor);
	}

}
