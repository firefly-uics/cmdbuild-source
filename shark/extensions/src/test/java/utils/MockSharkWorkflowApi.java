package utils;

import static org.mockito.Mockito.mock;

import java.util.Map;

import org.cmdbuild.workflow.api.SharkWorkflowApi;
import org.cmdbuild.workflow.api.WorkflowApi;

public class MockSharkWorkflowApi extends SharkWorkflowApi {

	public static final WorkflowApi mock;

	static {
		mock = mock(WorkflowApi.class);
	}

	@Override
	public int createCard(final String className, final Map<String, Object> attributes) {
		return mock.createCard(className, attributes);
	}

	@Override
	public void createRelation(final String domainName, final String className1, final int id1,
			final String className2, final int id2) {
		mock.createRelation(domainName, className1, id1, className2, id2);
	}

}
