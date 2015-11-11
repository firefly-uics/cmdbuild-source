package org.cmdbuild.workflow.xpdl;

import org.apache.commons.lang3.Validate;

public class CMActivityVariableToProcess {

	private final String name;
	private final String type;
	private final boolean writable;
	private final boolean mandatory;

	public CMActivityVariableToProcess(final String name, final String type, final boolean writable,
			final boolean mandatory) {
		Validate.notEmpty(name, "Variable names must be non-empty");
		Validate.notNull(type, "Variable type must be specified");
		this.name = name;
		this.type = type;
		this.writable = writable;
		this.mandatory = mandatory;
	}

	public String getName() {
		return name;
	}

	@Deprecated
	public String getType() {
		return type;
	}

	public boolean isWritable() {
		return writable;
	}

	public boolean isMandatory() {
		return mandatory;
	}

}
