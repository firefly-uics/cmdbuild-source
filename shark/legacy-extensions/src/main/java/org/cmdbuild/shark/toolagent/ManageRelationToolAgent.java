package org.cmdbuild.shark.toolagent;

public class ManageRelationToolAgent extends AbstractConditionalToolAgent {

	private static final String DOMAIN_NAME = "DomainName";
	private static final String CLASS_NAME_1 = "ClassName1";
	private static final String CLASS_NAME_2 = "ClassName2";
	private static final String OBJ_ID_1 = "ObjId1";
	private static final String OBJ_ID_2 = "ObjId2";
	private static final String DONE = "Done";

	private static final boolean RESULT_ALWAYS_TRUE_OR_THROWS = true;

	@Override
	protected void innerInvoke() throws Exception {
		final String domainName = getParameterValue(DOMAIN_NAME);
		final String className1 = getParameterValue(CLASS_NAME_1);
		final String className2 = getParameterValue(CLASS_NAME_2);
		final Long objId1 = getParameterValue(OBJ_ID_1);
		final Long objId2 = getParameterValue(OBJ_ID_2);

		getWorkflowApi().createRelation(domainName, //
				className1, //
				objId1.intValue(), //
				className2, //
				objId2.intValue());

		setParameterValue(DONE, RESULT_ALWAYS_TRUE_OR_THROWS);
	}

}
