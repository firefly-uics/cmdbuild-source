package integration;

import static org.cmdbuild.common.collect.Factory.entry;
import static org.cmdbuild.common.collect.Factory.linkedHashMapOf;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;
import static utils.EventManagerMatchers.hasProcessDefinitionId;

import org.cmdbuild.workflow.CMEventManager.ActivityInstance;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import utils.AbstractLocalSharkServiceTest;

public class ExternalSubflowTest extends AbstractLocalSharkServiceTest {

	private static final String PARENT_VARIABLE = "ParentVariable";
	private static final String PARENT_PACKAGE = "parent";
	private static final String PARENT_PROCESS = "Parent";

	private final ArgumentCaptor<ActivityInstance> activityInstanceCaptor = ArgumentCaptor.forClass(ActivityInstance.class);

	@Before
	public void createAndUploadPackage() throws Exception {
		uploadXpdlResource("xpdl/Child.xpdl");
		uploadXpdlResource("xpdl/Parent.xpdl");
	}

	@Test
	@SuppressWarnings("unchecked")
	public void spawnChildProcess() throws Exception {
		ws.startProcess(PARENT_PACKAGE, PARENT_PROCESS);

		verify(eventManager).processStarted(argThat(hasProcessDefinitionId(PARENT_PROCESS)));
		verify(eventManager, atLeastOnce()).activityStarted(activityInstanceCapturer());

		final ActivityInstance ai = capturedActivityInstance();
		final String processInstanceId = ai.getProcessInstanceId();

		ws.setProcessInstanceVariables(processInstanceId, linkedHashMapOf(
					entry(PARENT_VARIABLE, "Something")
				));
		ws.advanceActivityInstance(processInstanceId, ai.getActivityInstanceId());

		assertThat(
				ws.getProcessInstanceVariables(processInstanceId).get(PARENT_VARIABLE),
				is(equalTo((Object) "Copy of Something"))
			);
	}

	protected ActivityInstance activityInstanceCapturer() {
		return activityInstanceCaptor.capture();
	}

	protected ActivityInstance capturedActivityInstance() {
		return activityInstanceCaptor.getValue();
	}
}
