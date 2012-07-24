package integration;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasProperty;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static utils.XpdlTestUtils.randomName;

import java.util.HashMap;
import java.util.Map;

import org.cmdbuild.workflow.CMEventManager;
import org.cmdbuild.workflow.TypesConverter;
import org.cmdbuild.workflow.type.ReferenceType;
import org.cmdbuild.workflow.xpdl.XpdlActivity;
import org.cmdbuild.workflow.xpdl.XpdlDocument.ScriptLanguage;
import org.cmdbuild.workflow.xpdl.XpdlDocument.StandardAndCustomTypes;
import org.cmdbuild.workflow.xpdl.XpdlProcess;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import utils.AbstractLocalSharkServiceTest;
import utils.MockEventsDelegator;

public class VariablesTest extends AbstractLocalSharkServiceTest {

	private static final String A_BOOLEAN = "aBoolean";
	private static final String AN_INTEGER = "anInteger";
	private static final String A_STRING = "aString";

	private static final String UNDEFINED = "undefined";

	private static final String A_REFERENCE = "reference";

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
	public void variablesModifiedFromScript() throws Exception {
		final XpdlActivity scriptActivity = process.createActivity(randomName());
		scriptActivity.setScriptingType(ScriptLanguage.JAVA, "aBoolean = true; anInteger = 42; aString = \"foo\";");

		final XpdlActivity noImplActivity = process.createActivity(randomName());

		process.createTransition(scriptActivity, noImplActivity);

		final String procInstId = uploadXpdlAndStartProcess(process).getProcessInstanceId();
		verify(eventManager).activityClosed(scriptActivity.getId());

		final Map<String, Object> variables = ws.getProcessInstanceVariables(procInstId);

		assertThat((Boolean) variables.get(A_BOOLEAN), equalTo(true));
		assertThat((Long) variables.get(AN_INTEGER), equalTo(42L));
		assertThat((String) variables.get(A_STRING), equalTo("foo"));
	}

	@Test
	public void variablesSettedThenRead() throws Exception {
		final TypesConverter typesConverter = identityTypesConverterMock();

		ws.setVariableConverter(typesConverter);

		process.createActivity(randomName());

		final String procInstId = uploadXpdlAndStartProcess(process).getProcessInstanceId();
		verify(eventManager).processStarted(process.getId());

		final Map<String, Object> settedVariables = new HashMap<String, Object>();
		settedVariables.put(A_BOOLEAN, true);
		settedVariables.put(AN_INTEGER, 42);
		settedVariables.put(A_STRING, "foo");
		ws.setProcessInstanceVariables(procInstId, settedVariables);

		verify(typesConverter, times(3)).toWorkflowType(anyObject());

		final Map<String, Object> readVariables = ws.getProcessInstanceVariables(procInstId);
		assertThat((Boolean) readVariables.get(A_BOOLEAN), equalTo(true));
		assertThat((Long) readVariables.get(AN_INTEGER), equalTo(42L));
		assertThat((String) readVariables.get(A_STRING), equalTo("foo"));
	}

	@Test
	public void undefinedVariableSettedThenRead() throws Exception {
		process.createActivity(randomName());

		final String procInstId = uploadXpdlAndStartProcess(process).getProcessInstanceId();
		verify(eventManager).processStarted(process.getId());

		final Map<String, Object> settedVariables = new HashMap<String, Object>();
		settedVariables.put(UNDEFINED, "baz");
		ws.setProcessInstanceVariables(procInstId, settedVariables);

		final Map<String, Object> readVariables = ws.getProcessInstanceVariables(procInstId);
		assertThat((String) readVariables.get(UNDEFINED), equalTo("baz"));
	}

	@Test
	public void declareTypesSettedThenRead() throws Exception {
		process.createActivity(randomName());

		final String procInstId = uploadXpdlAndStartProcess(process).getProcessInstanceId();
		verify(eventManager).processStarted(process.getId());

		final Map<String, Object> settedVariables = new HashMap<String, Object>();
		settedVariables.put(A_REFERENCE, newReference(42));
		ws.setProcessInstanceVariables(procInstId, settedVariables);

		final Map<String, Object> readVariables = ws.getProcessInstanceVariables(procInstId);
		assertThat(readVariables.get(A_REFERENCE), hasProperty("id", equalTo(42)));
	}

	/*
	 * Utils
	 */

	private ReferenceType newReference(final int id) {
		final ReferenceType reference = new ReferenceType();
		reference.setId(id);
		return reference;
	}

	private TypesConverter identityTypesConverterMock() {
		final TypesConverter typesConverter = mock(TypesConverter.class);
		when(typesConverter.toWorkflowType(anyObject())).thenAnswer(new Answer<Object>() {
			@Override
			public Object answer(InvocationOnMock invocation) throws Throwable {
				return invocation.getArguments()[0];
			}
			
		});
		when(typesConverter.fromWorkflowType(anyObject())).thenAnswer(new Answer<Object>() {
			@Override
			public Object answer(InvocationOnMock invocation) throws Throwable {
				return invocation.getArguments()[0];
			}
		});
		return typesConverter;
	}
}