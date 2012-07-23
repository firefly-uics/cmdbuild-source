package utils;

import static org.mockito.Mockito.mock;

import org.cmdbuild.api.fluent.FluentApi;
import org.cmdbuild.api.fluent.FluentApiExecutor;
import org.cmdbuild.workflow.api.SchemaApi;
import org.cmdbuild.workflow.api.SharkWorkflowApi;
import org.cmdbuild.workflow.api.WorkflowApi;
import org.cmdbuild.workflow.type.LookupType;
import org.cmdbuild.workflow.type.ReferenceType;

public class MockSharkWorkflowApi extends SharkWorkflowApi {

	public static final FluentApiExecutor fluentApiExecutor;
	public static final WorkflowApi mock;

	static {
		fluentApiExecutor = mock(FluentApiExecutor.class);
		mock = mock(WorkflowApi.class);
	}

	@Override
	public FluentApi fluentApi() {
		return new FluentApi(fluentApiExecutor);
	}

	@Override
	public SchemaApi schemaApi() {
		return new SchemaApi() {

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

		};
	}

	@Override
	public WorkflowApi workflowApi() {
		return new WorkflowApi() {

			@Override
			public String selectAttribute(final String className, final int cardId, final String attributeName) {
				return mock.selectAttribute(className, cardId, attributeName);
			}

			@Override
			public ReferenceType selectReference(final String className, final String attributeName,
					final String attributeValue) {
				return mock.selectReference(className, attributeName, attributeValue);
			}

		};
	}

}
