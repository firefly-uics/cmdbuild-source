package org.cmdbuild.workflow;

import java.util.Map;

import org.cmdbuild.common.annotations.OldDao;
import org.cmdbuild.dao.backend.CMBackend;
import org.cmdbuild.elements.Lookup;
import org.cmdbuild.workflow.service.WSProcessInstanceState;
import org.enhydra.shark.api.common.SharkConstants;

import com.google.common.collect.BiMap;
import com.google.common.collect.Maps;

@OldDao
class Utils {

	private Utils() {
		// prevents instantiation
	}

	public static final String FLOW_STATUS_LOOKUP = "FlowStatus";

	private static final Map<String, WSProcessInstanceState> stateCodeToEnumMap;

	static {
		stateCodeToEnumMap = Maps.newHashMap();
		stateCodeToEnumMap.put(SharkConstants.STATE_OPEN_RUNNING, WSProcessInstanceState.OPEN);
		stateCodeToEnumMap.put(SharkConstants.STATE_OPEN_NOT_RUNNING_SUSPENDED, WSProcessInstanceState.SUSPENDED);
		stateCodeToEnumMap.put(SharkConstants.STATE_CLOSED_COMPLETED, WSProcessInstanceState.COMPLETED);
		stateCodeToEnumMap.put(SharkConstants.STATE_CLOSED_TERMINATED, WSProcessInstanceState.TERMINATED);
		stateCodeToEnumMap.put(SharkConstants.STATE_CLOSED_ABORTED, WSProcessInstanceState.ABORTED);
	}

	public static WSProcessInstanceState getFlowStatusForLookup(final String flowStatusLookupCode) {
		if (flowStatusLookupCode == null) {
			return null;
		}
		final WSProcessInstanceState state = stateCodeToEnumMap.get(flowStatusLookupCode);
		return (state == null) ? WSProcessInstanceState.UNSUPPORTED : state;
	}

	/*
	 * From the Proterozoic Eon
	 */
	public static Lookup lookupForFlowStatus(final WSProcessInstanceState state) {
		final String flowStatusLookupCode;
		switch (state) {
		case OPEN:
			flowStatusLookupCode = SharkConstants.STATE_OPEN_RUNNING;
			break;
		case SUSPENDED:
			flowStatusLookupCode = SharkConstants.STATE_OPEN_NOT_RUNNING_SUSPENDED;
			break;
		case COMPLETED:
			flowStatusLookupCode = SharkConstants.STATE_CLOSED_COMPLETED;
			break;
		case TERMINATED:
			flowStatusLookupCode = SharkConstants.STATE_CLOSED_TERMINATED;
			break;
		case ABORTED:
			flowStatusLookupCode = SharkConstants.STATE_CLOSED_ABORTED;
			break;
		default:
			flowStatusLookupCode = null;
		}
		return lookupForFlowStatusCode(flowStatusLookupCode);
	}

	public static Lookup lookupForFlowStatusCode(final String flowStatusLookupCode) {
		return CMBackend.INSTANCE.getFirstLookupByCode(FLOW_STATUS_LOOKUP, flowStatusLookupCode);
	}

}
