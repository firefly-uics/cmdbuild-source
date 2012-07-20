package org.cmdbuild.workflow.extattr;

import java.util.Map;

import org.cmdbuild.cql.compiler.impl.QueryImpl;
import org.cmdbuild.elements.interfaces.ITable;
import org.cmdbuild.services.auth.UserContext;
import org.cmdbuild.utils.CQLFacadeCompiler;
import org.cmdbuild.workflow.operation.ActivityDO;
import org.json.JSONObject;

public abstract class AbstractFilteredExtendedAttribute extends AbstractCmdbuildExtendedAttribute {

	static final String ClassName = "ClassName";
	static final String Filter = "Filter";

	@Override
	protected void addCustomParams(ActivityDO activityDO, JSONObject object,
			ExtendedAttributeConfigParams eacp) throws Exception {
		ITable targetClass = getLinkTargetClass(eacp);
		object.put("ClassName", targetClass.getName());
		object.put("ClassId", targetClass.getId());
	}

	protected final ITable getLinkTargetClass(ExtendedAttributeConfigParams eacp) throws Exception {
		Map<String,Object> params = eacp.getParameters();
		String cName;
		if (params.containsKey(Filter)) {
			String cqlQuery = (String)params.get(Filter);
			QueryImpl q = CQLFacadeCompiler.compileWithTemplateParams(cqlQuery);
			cName = q.getFrom().mainClass().getClassName();
		} else {
			cName = (String)params.get(ClassName);
		}
		return UserContext.systemContext().tables().get(cName);
	}
}
