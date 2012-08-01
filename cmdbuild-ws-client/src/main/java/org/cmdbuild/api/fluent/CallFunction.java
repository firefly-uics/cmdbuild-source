package org.cmdbuild.api.fluent;

import java.util.Map;

public class CallFunction extends ActiveFunction {

	public CallFunction(final FluentApiExecutor executor, final String functionName) {
		super(executor, functionName);
	}

	public CallFunction with(final String name, final Object value) {
		super.set(name, value);
		return this;
	}

	public Map<String, String> execute() {
		return executor().execute(this);
	}

}