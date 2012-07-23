package org.cmdbuild.workflow.api;

import java.util.Map;

import org.cmdbuild.workflow.type.ReferenceType;

/**
 * Legacy API that should be updated to be fluent.
 */
public interface WorkflowApi {

	int createCard(String className, Map<String, Object> attributes);

	void createRelation(String domainName, String className1, int id1, String className2, int id2);

	String selectAttribute(String className, int cardId, String attributeName);

	ReferenceType selectReference(String className, String attributeName, String attributeValue);

	Map<String, String> callFunction(String functionName, Map<String, Object> params);
}
