package org.cmdbuild.api.fluent;

import java.util.Map;

public class CallFunction extends ActiveFunction {

	CallFunction(final FluentApi api, final String functionName) {
		super(api, functionName);
	}

	public CallFunction with(final String name, final Object value) {
		super.set(name, value);
		return this;
	}

	public Map<String, Object> execute() {
		return getApi().getExecutor().execute(this);
	}

}