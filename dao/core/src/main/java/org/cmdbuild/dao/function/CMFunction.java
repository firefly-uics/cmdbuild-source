package org.cmdbuild.dao.function;

import java.util.List;

import org.cmdbuild.dao.CMTypeObject;
import org.cmdbuild.dao.entrytype.attributetype.CMAttributeType;

public interface CMFunction extends CMTypeObject {

	interface CMFunctionParameter {
		String getName();

		CMAttributeType<?> getType();
	}

	List<CMFunctionParameter> getInputParameters();

	List<CMFunctionParameter> getOutputParameters();

	boolean returnsSet();

}
