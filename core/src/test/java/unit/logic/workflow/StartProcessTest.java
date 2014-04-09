package unit.logic.workflow;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.util.HashMap;
import java.util.Map;

import org.cmdbuild.logic.workflow.StartProcess;
import org.cmdbuild.logic.workflow.StartProcess.Builder;
import org.cmdbuild.logic.workflow.WorkflowLogic;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class StartProcessTest {

	@Captor
	private ArgumentCaptor<Map<String, Object>> attributesCaptor;

	@Captor
	private ArgumentCaptor<Map<String, Object>> widgetSubmissionCaptor;

	@Test(expected = NullPointerException.class)
	public void workflowLogicIsRequired() throws Exception {
		// given
		final Builder builder = StartProcess.newInstance() //
				.withClassName("foo");

		// when
		builder.build();
	}

	@Test(expected = NullPointerException.class)
	public void classNameIsRequired() throws Exception {
		// given
		final WorkflowLogic workflowLogic = mock(WorkflowLogic.class);
		final Builder builder = StartProcess.newInstance() //
				.withWorkflowLogic(workflowLogic);

		// when
		builder.build();
	}

	@Test(expected = IllegalArgumentException.class)
	public void classNameMustBeNotBlank() throws Exception {
		// given
		final WorkflowLogic workflowLogic = mock(WorkflowLogic.class);
		final Builder builder = StartProcess.newInstance() //
				.withWorkflowLogic(workflowLogic) //
				.withClassName(" \t");

		// when
		builder.build();
	}

	@Test
	public void processStartedWithoutAttributes() throws Exception {
		// given
		final WorkflowLogic workflowLogic = mock(WorkflowLogic.class);

		// when
		StartProcess.newInstance() //
				.withWorkflowLogic(workflowLogic) //
				.withClassName("foo") //
				.build() //
				.execute();

		// verify
		verify(workflowLogic).startProcess(eq("foo"), attributesCaptor.capture(), widgetSubmissionCaptor.capture(),
				eq(true));

		final Map<String, Object> attributes = attributesCaptor.getValue();
		assertThat(attributes.isEmpty(), is(true));

		final Map<String, Object> widgetSubmission = widgetSubmissionCaptor.getValue();
		assertThat(widgetSubmission.isEmpty(), is(true));
	}

	@Test
	public void processStartedWithAttributes() throws Exception {
		// given
		final WorkflowLogic workflowLogic = mock(WorkflowLogic.class);

		// when
		StartProcess.newInstance() //
				.withWorkflowLogic(workflowLogic) //
				.withClassName("foo") //
				.withAttribute("bar", "BAR") //
				.withAttribute("baz", 42L) //
				.withAttributes(new HashMap<String, Object>() {
					{
						put("a", "A");
						put("b", 123);
					}
				}) //
				.build() //
				.execute();

		// verify
		verify(workflowLogic).startProcess(eq("foo"), attributesCaptor.capture(), widgetSubmissionCaptor.capture(),
				eq(true));

		final Map<String, Object> attributes = attributesCaptor.getValue();
		assertThat(attributes.size(), equalTo(4));
		assertThat(attributes, hasEntry("bar", (Object) "BAR"));
		assertThat(attributes, hasEntry("baz", (Object) 42L));
		assertThat(attributes, hasEntry("a", (Object) "A"));
		assertThat(attributes, hasEntry("b", (Object) 123));

		final Map<String, Object> widgetSubmission = widgetSubmissionCaptor.getValue();
		assertThat(widgetSubmission.isEmpty(), is(true));
	}

	@Test
	public void settingAttributeWithNullNameDoesNothing() throws Exception {
		// given
		final WorkflowLogic workflowLogic = mock(WorkflowLogic.class);

		// when
		StartProcess.newInstance() //
				.withWorkflowLogic(workflowLogic) //
				.withClassName("foo") //
				.withAttribute(null, "bar") //
				.withAttribute("baz", 42L) //
				.withAttributes(new HashMap<String, Object>() {
					{
						put("bar", "BAR");
					}
				}) //
				.build() //
				.execute();

		// verify
		verify(workflowLogic).startProcess(eq("foo"), attributesCaptor.capture(), widgetSubmissionCaptor.capture(),
				eq(true));

		final Map<String, Object> attributes = attributesCaptor.getValue();
		assertThat(attributes.size(), equalTo(2));
		assertThat(attributes, hasEntry("bar", (Object) "BAR"));
		assertThat(attributes, hasEntry("baz", (Object) 42L));

		final Map<String, Object> widgetSubmission = widgetSubmissionCaptor.getValue();
		assertThat(widgetSubmission.isEmpty(), is(true));
	}

	@Test
	public void settingAttributeWithBlankNameDoesNothing() throws Exception {
		// given
		final WorkflowLogic workflowLogic = mock(WorkflowLogic.class);

		// when
		StartProcess.newInstance() //
				.withWorkflowLogic(workflowLogic) //
				.withClassName("foo") //
				.withAttribute(" \t", "should not be added") //
				.withAttribute("baz", 42L) //
				.withAttributes(new HashMap<String, Object>() {
					{
						put("bar", "BAR");
						put("\t ", "should not be added");
					}
				}) //
				.build() //
				.execute();

		// verify
		verify(workflowLogic).startProcess(eq("foo"), attributesCaptor.capture(), widgetSubmissionCaptor.capture(),
				eq(true));

		final Map<String, Object> attributes = attributesCaptor.getValue();
		assertThat(attributes.size(), equalTo(2));
		assertThat(attributes, hasEntry("bar", (Object) "BAR"));
		assertThat(attributes, hasEntry("baz", (Object) 42L));

		final Map<String, Object> widgetSubmission = widgetSubmissionCaptor.getValue();
		assertThat(widgetSubmission.isEmpty(), is(true));
	}

	@Test
	public void settingNullAttributesDoesNothing() throws Exception {
		// given
		final WorkflowLogic workflowLogic = mock(WorkflowLogic.class);

		// when
		StartProcess.newInstance() //
				.withWorkflowLogic(workflowLogic) //
				.withClassName("foo") //
				.withAttributes(null) //
				.build() //
				.execute();

		// verify
		verify(workflowLogic).startProcess(eq("foo"), attributesCaptor.capture(), widgetSubmissionCaptor.capture(),
				eq(true));

		final Map<String, Object> attributes = attributesCaptor.getValue();
		assertThat(attributes.size(), equalTo(0));

		final Map<String, Object> widgetSubmission = widgetSubmissionCaptor.getValue();
		assertThat(widgetSubmission.isEmpty(), is(true));
	}

}
