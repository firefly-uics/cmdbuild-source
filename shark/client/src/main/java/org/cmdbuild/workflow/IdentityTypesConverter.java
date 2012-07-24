package org.cmdbuild.workflow;

public class IdentityTypesConverter implements TypesConverter {

	@Override
	public Object toWorkflowType(final Object obj) {
		return obj;
	}

	@Override
	public Object fromWorkflowType(final Object obj) {
		return obj;
	}
}
