package org.cmdbuild.shark.toolagent;

import java.util.Map;

import org.enhydra.shark.api.internal.toolagent.AppParameter;

public class ExecuteStoredProcedureToolAgent extends AbstractConditionalToolAgent {

	private final String PROCEDURE = "Procedure";
	private final String CURSOR_PROCEDURE = "CursorProcedure";

	@Override
	protected void innerInvoke() throws Exception {
		final String functionName = getFunctionName();
		final Map<String, Object> input = getInputParameterValues();

		final Map<String, String> output = getWorkflowApi().callFunction(functionName, input);

		for (final AppParameter parmOut : getReturnParameters()) {
			final String stringValue = output.get(parmOut.the_formal_name);
			parmOut.the_value = convertToProcessValue(stringValue, parmOut.the_class);
		}
	}

	private String getFunctionName() {
		String functionName = getExtendedAttribute(PROCEDURE);
		if (functionName == null) {
			functionName = getExtendedAttribute(CURSOR_PROCEDURE);
		}
		return functionName;
	}

	private Object convertToProcessValue(final String stringValue, final Class<?> clazz) {
		// TODO
		if (clazz.equals(String.class)) {
			return stringValue;
		} else {
			return null;
		}
	}

}
