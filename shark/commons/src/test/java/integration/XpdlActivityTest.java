package integration;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static utils.XpdlTestUtils.randomName;

import org.cmdbuild.workflow.xpdl.XpdlActivity;
import org.cmdbuild.workflow.xpdl.XpdlProcess;
import org.cmdbuild.workflow.xpdl.XpdlDocument.ScriptLanguage;
import org.junit.Before;
import org.junit.Test;

public class XpdlActivityTest extends AbstractXpdlTest {

	private XpdlActivity xpdlActivity;

	@Before
	public void createActivity() throws Exception {
		final XpdlProcess xpdlProcess = xpdlDocument.createProcess(randomName());
		xpdlActivity = xpdlProcess.createActivity(randomName());
	}

	@Test
	public void defaultActivityIsManualType() throws Exception {
		assertTrue(xpdlActivity.isManualType());
	}

	@Test
	public void performerCanBeSetForActivities() throws Exception {
		assertThat(xpdlActivity.getFirstPerformer(), is(nullValue()));
		xpdlActivity.setPerformer("foo");
		assertThat(xpdlActivity.getFirstPerformer(), is("foo"));
	}

	@Test
	public void activityCanBeChangedToScriptingType() throws Exception {
		final String expression = "greeting = new String(\"hello, world\")";

		xpdlActivity.setScriptingType(ScriptLanguage.JAVA, expression);
		assertThat(xpdlActivity.isScriptingType(), is(true));
		assertThat(xpdlActivity.getScriptLanguage(), equalTo(ScriptLanguage.JAVA));
		assertThat(xpdlActivity.getScriptExpression(), equalTo(expression));

		xpdlActivity.setScriptingType(ScriptLanguage.JAVASCRIPT, expression);
		assertThat(xpdlActivity.isScriptingType(), is(true));
		assertThat(xpdlActivity.getScriptLanguage(), equalTo(ScriptLanguage.JAVASCRIPT));
		assertThat(xpdlActivity.getScriptExpression(), equalTo(expression));
	}

}
