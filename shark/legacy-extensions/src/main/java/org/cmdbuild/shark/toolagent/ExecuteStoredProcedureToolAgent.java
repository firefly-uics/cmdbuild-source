package org.cmdbuild.shark.toolagent;

import java.util.Map;
import java.util.Map.Entry;

import org.cmdbuild.api.fluent.CallFunction;
import org.enhydra.shark.api.internal.toolagent.AppParameter;

public class ExecuteStoredProcedureToolAgent extends AbstractConditionalToolAgent {

	private final String PROCEDURE = "Procedure";
	private final String CURSOR_PROCEDURE = "CursorProcedure";

	@Override
	protected void innerInvoke() throws Exception {
		final String functionName = getFunctionName();
		final Map<String, Object> input = getInputParameterValues();
		final Map<String, String> output = callFunction(functionName, input);

		for (final AppParameter parmOut : getReturnParameters()) {
			final String stringValue = output.get(parmOut.the_formal_name);
			parmOut.the_value = convertToProcessValue(stringValue, parmOut.the_class);
		}
	}

	private Map<String, String> callFunction(final String functionName, final Map<String, Object> input) {
		final CallFunction callFunction = getFluentApi().callFunction(functionName);
		for (final Entry<String, Object> entry : input.entrySet()) {
			final Object normalizedValue = convertFromProcessValue(entry.getValue());
			callFunction.with(entry.getKey(), normalizedValue);
		}
		final Map<String, String> output = callFunction.execute();
		return output;
	}

	private String getFunctionName() {
		String functionName = getExtendedAttribute(PROCEDURE);
		if (functionName == null) {
			functionName = getExtendedAttribute(CURSOR_PROCEDURE);
		}
		return functionName;
	}

}
