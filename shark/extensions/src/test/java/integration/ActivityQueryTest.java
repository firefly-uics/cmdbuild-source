package integration;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static utils.XpdlTestUtils.randomName;

import org.apache.commons.lang.StringUtils;
import org.cmdbuild.workflow.CMWorkflowException;
import org.cmdbuild.workflow.service.WSActivityInstInfo;
import org.cmdbuild.workflow.xpdl.XpdlDocument.ScriptLanguage;
import org.cmdbuild.workflow.xpdl.XpdlProcess;
import org.junit.Before;
import org.junit.Test;

import utils.AbstractLocalWorkflowServiceTest;

public class ActivityQueryTest extends AbstractLocalWorkflowServiceTest {

	private XpdlProcess p1;
	private XpdlProcess p2;
	private XpdlProcess p3;

	@Before
	public void createAndUploadProcess() {
		p1 = xpdlDocument.createProcess(randomName());
		p1.createTransition(
				p1.createActivity("A"),
				p1.createActivity("B")
			);
		p1.createActivity("C").setScriptingType(ScriptLanguage.JAVA, StringUtils.EMPTY);

		p2 = xpdlDocument.createProcess(randomName());
		p2.createActivity("X");

		p3 = xpdlDocument.createProcess(randomName());
		p3.createActivity("Y").setScriptingType(ScriptLanguage.JAVA, StringUtils.EMPTY);
	}

	@Test
	public void openActivitiesCanBeQueriedForAProcessInstance() throws CMWorkflowException {
		String pi11 = uploadXpdlAndStartProcess(p1);
		String pi21 = startProcess(p2);
		String pi12 = startProcess(p1);
		String pi31 = startProcess(p3);

		assertThat(openActivitiesForProcessInstance(pi11), is(new String[] {"A"}));
		assertThat(openActivitiesForProcessInstance(pi12), is(new String[] {"A"}));
		assertThat(openActivitiesForProcessInstance(pi21), is(new String[] {"X"}));
		assertThat(openActivitiesForProcessInstance(pi31), is(new String[] {}));
	}

	@Test
	public void openActivitiesCanBeQueriedForAProcess() throws CMWorkflowException {
		uploadXpdlAndStartProcess(p1);
		startProcess(p2);
		startProcess(p1);
		startProcess(p3);

		assertThat(openActivitiesForProcess(p1.getId()), is(new String[] {"A","A"}));
		assertThat(openActivitiesForProcess(p2.getId()), is(new String[] {"X"}));
		assertThat(openActivitiesForProcess(p3.getId()), is(new String[] {}));
	}

	private String[] openActivitiesForProcessInstance(final String processInstanceId) throws CMWorkflowException {
		return defIds(ws.findOpenActivitiesForProcessInstance(processInstanceId));
	}

	private String[] openActivitiesForProcess(final String processDefinitionId) throws CMWorkflowException {
		return defIds(ws.findOpenActivitiesForProcess(processDefinitionId));
	}

	private String[] defIds(final WSActivityInstInfo[] activityInstances) {
		final String[] ids = new String[activityInstances.length];
		for (int i = 0; i < activityInstances.length; ++i) {
			ids[i] = activityInstances[i].getActivityDefinitionId();
		}
		return ids;
	}

}
