package org.cmdbuild.workflow.api;

import org.cmdbuild.workflow.type.ReferenceType;

/**
 * Legacy API that should be updated to be fluent.
 */
public interface WorkflowApi {

	ReferenceType selectReference(String className, String attributeName, String attributeValue);

}
