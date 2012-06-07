package integration;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.verify;
import static utils.XpdlTestUtils.randomName;

import java.util.HashMap;
import java.util.Map;

import org.cmdbuild.workflow.CMEventManager;
import org.cmdbuild.workflow.xpdl.XpdlActivity;
import org.cmdbuild.workflow.xpdl.XpdlProcess;
import org.cmdbuild.workflow.xpdl.XpdlDocument.ScriptLanguage;
import org.cmdbuild.workflow.xpdl.XpdlDocument.StandardAndCustomTypes;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import utils.AbstractLocalWorkflowServiceTest;
import utils.MockEventsDelegator;

public class VariablesTest extends AbstractLocalWorkflowServiceTest {

	private static final String A_BOOLEAN = "aBoolean";
	private static final String AN_INTEGER = "anInteger";
	private static final String A_STRING = "aString";

	private XpdlProcess process;
	private CMEventManager eventManager;

	@Before
	public void createAndUploadPackage() throws Exception {
		process = xpdlDocument.createProcess(randomName());

		process.addField(A_BOOLEAN, StandardAndCustomTypes.BOOLEAN);
		process.addField(AN_INTEGER, StandardAndCustomTypes.INTEGER);
		process.addField(A_STRING, StandardAndCustomTypes.STRING);
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
	public void variableModifiedFromScript() throws Exception {
		final XpdlActivity activity = process.createActivity(randomName());
		activity.setScriptingType(ScriptLanguage.JAVA, "aBoolean = true; anInteger = 42; aString = \"foo\";");

		final String procInstId = uploadXpdlAndStartProcess(process);
		verify(eventManager).processClosed(process.getId());

		final Map<String, Object> variables = ws.getProcessInstanceVariables(procInstId);

		assertThat((Boolean) variables.get(A_BOOLEAN), equalTo(true));
		assertThat((Long) variables.get(AN_INTEGER), equalTo(42L));
		assertThat((String) variables.get(A_STRING), equalTo("foo"));
	}

	@Test
	public void variableSettedThenRead() throws Exception {
		process.createActivity(randomName());

		final String procInstId = uploadXpdlAndStartProcess(process);
		verify(eventManager).processStarted(process.getId());

		final Map<String, Object> settedVariables = new HashMap<String, Object>();
		settedVariables.put(A_BOOLEAN, true);
		settedVariables.put(AN_INTEGER, 42);
		settedVariables.put(A_STRING, "foo");
		ws.setProcessInstanceVariables(procInstId, settedVariables);

		final Map<String, Object> readedVariables = ws.getProcessInstanceVariables(procInstId);
		assertThat((Boolean) readedVariables.get(A_BOOLEAN), equalTo(true));
		assertThat((Long) readedVariables.get(AN_INTEGER), equalTo(42L));
		assertThat((String) readedVariables.get(A_STRING), equalTo("foo"));
	}

}
