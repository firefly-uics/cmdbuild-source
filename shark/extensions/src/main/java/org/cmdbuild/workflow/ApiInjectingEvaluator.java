package org.cmdbuild.workflow;

import java.util.Map;

import org.cmdbuild.api.fluent.FluentApi;
import org.enhydra.shark.api.client.wfmc.wapi.WMSessionHandle;
import org.enhydra.shark.api.internal.scripting.Evaluator;
import org.enhydra.shark.api.internal.working.CallbackUtilities;

@SuppressWarnings({ "rawtypes", "unchecked" })
public class ApiInjectingEvaluator implements Evaluator {

	private final Evaluator inner;
	private final FluentApi fluentApi;

	private ApiInjectingEvaluator(final Evaluator evaluator, final FluentApi fluentApi) {
		this.inner = evaluator;
		this.fluentApi = fluentApi;
	}

	@Override
	public void configure(final CallbackUtilities cus) throws Exception {
		inner.configure(cus);
	}

	@Override
	public boolean evaluateCondition(final WMSessionHandle sHandle, final String procId, final String actId,
			final String condition, final Map context) throws Exception {
		return inner.evaluateCondition(sHandle, procId, actId, condition, pimpMyContext(context));
	}

	@Override
	public Object evaluateExpression(final WMSessionHandle sHandle, final String procId, final String actId,
			final String expr, final Map context, final Class resultClass) throws Exception {
		return inner.evaluateExpression(sHandle, procId, actId, expr, pimpMyContext(context), resultClass);
	}

	private Map pimpMyContext(final Map context) {
		context.put(Constants.API_VARIABLE, fluentApi);
		return context;
	}

	public static Evaluator from(final Evaluator evaluator, final FluentApi fluentApi) {
		return (evaluator == null) ? null : new ApiInjectingEvaluator(evaluator, fluentApi);
	}

}
