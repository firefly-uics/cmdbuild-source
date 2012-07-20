package utils;

import static org.mockito.Mockito.mock;

import java.util.Map;

import org.cmdbuild.workflow.api.SharkWorkflowApi;
import org.cmdbuild.workflow.api.WorkflowApi;
import org.cmdbuild.workflow.type.LookupType;
import org.cmdbuild.workflow.type.ReferenceType;

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

	@Override
	public String selectAttribute(final String className, final int cardId, final String attributeName) {
		return mock.selectAttribute(className, cardId, attributeName);
	}

	@Override
	public ReferenceType selectReference(String className, String attributeName, String attributeValue) {
		return mock.selectReference(className, attributeName, attributeValue);
	}

	@Override
	public Map<String, String> callFunction(final String functionName, final Map<String, Object> params) {
		return mock.callFunction(functionName, params);
	}

	@Override
	public ClassInfo findClass(final String className) {
		return null;
	}

	@Override
	public ClassInfo findClass(final int classId) {
		return null;
	}

	@Override
	public LookupType selectLookupById(final int id) {
		return null;
	}

	@Override
	public LookupType selectLookupByCode(final String type, final String code) {
		return null;
	}

	@Override
	public LookupType selectLookupByDescription(final String type, final String description) {
		return null;
	}


}
