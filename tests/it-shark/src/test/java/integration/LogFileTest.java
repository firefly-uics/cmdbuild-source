package integration;

import static java.lang.String.format;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static utils.XpdlTestUtils.randomName;

import java.io.IOException;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.cmdbuild.workflow.CMWorkflowException;
import org.cmdbuild.workflow.xpdl.XpdlActivity;
import org.cmdbuild.workflow.xpdl.XpdlDocument.ScriptLanguage;
import org.cmdbuild.workflow.xpdl.XpdlException;
import org.cmdbuild.workflow.xpdl.XpdlProcess;
import org.junit.Before;
import org.junit.Test;

public class LogFileTest extends AbstractRemoteSharkServiceTest {

	private XpdlProcess process;

	private final String processId = randomName();
	private final String activityId = randomName();

	@Before
	public void createAndUploadPackage() throws Exception {
		process = xpdlDocument.createProcess(processId);
	}

	@Test
	public void emptyAtStartupNotEmptyAfterProcessStart() throws Exception {
		assertTrue(fileIsEmpty());
		startProcess();
		assertFalse(fileIsEmpty());
	}

	@Test
	public void someExpectedLines() throws Exception {
		assertTrue(fileIsEmpty());
		startProcess();
		assertFalse(fileIsEmpty());
		assertTrue(hasLine(entryWithId("processStarted", processId)));
		assertTrue(hasLine(entryWithId("activityStarted", activityId)));
		assertTrue(hasLine(entryWithId("activityClosed", activityId)));
		assertTrue(hasLine(entryWithId("processClosed", processId)));
		assertFalse(hasLine("foo"));
	}

	private boolean fileIsEmpty() throws IOException {
		final List<String> lines = FileUtils.readLines(LOGFILE);
		return lines.isEmpty();
	}

	private void startProcess() throws CMWorkflowException, XpdlException {
		final XpdlActivity activity = process.createActivity(activityId);
		activity.setScriptingType(ScriptLanguage.JAVA, StringUtils.EMPTY);
		uploadXpdlAndStartProcess(process);
	}

	private String entryWithId(final String message, final String id) {
		return format("%s: %s", message, id);
	}

}
