package org.cmdbuild.api.fluent;

import static java.util.Collections.unmodifiableMap;

import java.util.HashMap;
import java.util.Map;

public class Function {

	private final String functionName;
	private final Map<String, String> inputParameters;

	public Function(final String functionName) {
		this.functionName = functionName;
		inputParameters = new HashMap<String, String>();
	}

	public String getFunctionName() {
		return functionName;
	}

	public Map<String, String> getInputs() {
		return unmodifiableMap(inputParameters);
	}

	void set(final String name, final String value) {
		inputParameters.put(name, value);
	}

}