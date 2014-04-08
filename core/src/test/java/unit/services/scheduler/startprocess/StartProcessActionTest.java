package unit.services.scheduler.startprocess;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.util.Map;

import org.cmdbuild.logic.workflow.WorkflowLogic;
import org.cmdbuild.services.scheduler.startprocess.StartProcess;
import org.junit.Test;

import com.google.common.collect.Maps;

public class StartProcessActionTest {

	@Test(expected = NullPointerException.class)
	public void workflowLogicIsRequired() throws Exception {
		// when
		StartProcess.newInstance() //
				.withClassName("foo") //
				.build();
	}

	@Test(expected = NullPointerException.class)
	public void classNameIsRequired() throws Exception {
		// given
		final WorkflowLogic workflowLogic = mock(WorkflowLogic.class);

		// when
		StartProcess.newInstance() //
				.withWorkflowLogic(workflowLogic) //
				.build();
	}

	@Test
	public void attributesAreNotRequired() throws Exception {
		// given
		final WorkflowLogic workflowLogic = mock(WorkflowLogic.class);

		// when
		StartProcess.newInstance() //
				.withWorkflowLogic(workflowLogic) //
				.withClassName("foo") //
				.build();
	}

	@Test
	public void workflowLogicInvokedWhenActionIsExecuted() throws Exception {
		// given
		final WorkflowLogic workflowLogic = mock(WorkflowLogic.class);
		final String CLASSNAME = "foo";
		final Map<String, String> ATTRIBUTES = Maps.newHashMap();
		ATTRIBUTES.put("bar", "BAR");
		ATTRIBUTES.put("baz", "BAZ");

		// when
		StartProcess.newInstance() //
				.withWorkflowLogic(workflowLogic) //
				.withClassName(CLASSNAME) //
				.withAttributes(ATTRIBUTES) //
				.build() //
				.execute();

		verify(workflowLogic).startProcess(eq(CLASSNAME), eq(ATTRIBUTES), any(Map.class), eq(true));
	}

}
