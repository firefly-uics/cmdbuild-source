package org.cmdbuild.dao.function;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.cmdbuild.dao.DBTypeObject;
import org.cmdbuild.dao.entrytype.attributetype.CMAttributeType;

public class DBFunction extends DBTypeObject implements CMFunction {

	private static class DBFunctionParameter implements CMFunctionParameter {

		private final String name;
		private final CMAttributeType<?> type;

		DBFunctionParameter(final String name, final CMAttributeType<?> type) {
			Validate.notEmpty(name);
			Validate.notNull(type);
			this.name = name;
			this.type = type;
		}

		@Override
		public String getName() {
			return name;
		}

		@Override
		public CMAttributeType<?> getType() {
			return type;
		}
		
	}

	private final List<CMFunctionParameter> inputParameters;
	private final List<CMFunctionParameter> outputParameters;

	private final List<CMFunctionParameter> unmodifiableInputParameters;
	private final List<CMFunctionParameter> unmodifiableOutputParameters;

	private boolean returnsSet;

	public DBFunction(final String name, final boolean returnsSet) {
		super(name, name);
		this.inputParameters = new ArrayList<CMFunctionParameter>();
		this.unmodifiableInputParameters = Collections.unmodifiableList(inputParameters);
		this.outputParameters = new ArrayList<CMFunctionParameter>();
		this.unmodifiableOutputParameters = Collections.unmodifiableList(outputParameters);
		this.returnsSet = returnsSet;
	}

	@Override
	public boolean returnsSet() {
		return returnsSet;
	}

	@Override
	public Iterable<CMFunctionParameter> getInputParameters() {
		return unmodifiableInputParameters;
	}

	@Override
	public Iterable<CMFunctionParameter> getOutputParameters() {
		return unmodifiableOutputParameters;
	}

	public void addInputParameter(String name, final CMAttributeType<?> type) {
		if (StringUtils.isBlank(name)) {
			name = String.format("_%d", inputParameters.size()+1);
		}
		inputParameters.add(new DBFunctionParameter(name, type));
	}

	public void addOutputParameter(String name, final CMAttributeType<?> type) {
		if (StringUtils.isBlank(name)) {
			name = String.format("_%d", outputParameters.size()+1);
		}
		outputParameters.add(new DBFunctionParameter(name, type));
	}

}
