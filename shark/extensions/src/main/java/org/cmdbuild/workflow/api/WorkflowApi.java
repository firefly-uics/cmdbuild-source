package org.cmdbuild.workflow.api;

import java.util.Map;

public interface WorkflowApi {

	int createCard(String className, Map<String, Object> attributes);

	void createRelation(String domainName, String className1, int id1, String className2, int id2);

}
