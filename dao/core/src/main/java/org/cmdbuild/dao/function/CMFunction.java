package org.cmdbuild.dao.function;

import org.cmdbuild.dao.CMTypeObject;
import org.cmdbuild.dao.entrytype.attributetype.CMAttributeType;

public interface CMFunction extends CMTypeObject {

	interface CMFunctionParameter {
		String getName();
		CMAttributeType<?> getType();
	}

	Iterable<CMFunctionParameter> getInputParameters();
	Iterable<CMFunctionParameter> getOutputParameters();

	boolean returnsSet();
}
