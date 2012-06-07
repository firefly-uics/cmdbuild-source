package org.cmdbuild.dao.entrytype;

import java.util.List;

import org.cmdbuild.dao.function.CMFunction;

public interface CMFunctionCall extends CMEntryType {

	public CMFunction getFunction();
	public List<Object> getParams();
}
