package org.cmdbuild.workflow.api;

import java.util.Map;

public interface WorkflowApi {

	int createCard(String classname, Map<String, String> attributes);

}
