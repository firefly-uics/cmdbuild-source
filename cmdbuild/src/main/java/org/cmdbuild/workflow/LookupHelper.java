package org.cmdbuild.workflow;

import org.cmdbuild.data.store.lookup.LookupDto;
import org.cmdbuild.data.store.lookup.LookupStore;
import org.cmdbuild.data.store.lookup.LookupTypeDto;
import org.cmdbuild.workflow.service.WSProcessInstanceState;
import org.enhydra.shark.api.common.SharkConstants;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

class LookupHelper {

	public static final String FLOW_STATUS_LOOKUP = "FlowStatus";

	private static final LookupTypeDto FLOW_STATUS = LookupTypeDto.newInstance() //
			.withName(FLOW_STATUS_LOOKUP) //
			.build();

	private static final BiMap<String, WSProcessInstanceState> stateByFlowStatusCode;

	static {
		stateByFlowStatusCode = HashBiMap.create();
		stateByFlowStatusCode.put(SharkConstants.STATE_OPEN_RUNNING, WSProcessInstanceState.OPEN);
		stateByFlowStatusCode.put(SharkConstants.STATE_OPEN_NOT_RUNNING_SUSPENDED, WSProcessInstanceState.SUSPENDED);
		stateByFlowStatusCode.put(SharkConstants.STATE_CLOSED_COMPLETED, WSProcessInstanceState.COMPLETED);
		stateByFlowStatusCode.put(SharkConstants.STATE_CLOSED_TERMINATED, WSProcessInstanceState.TERMINATED);
		stateByFlowStatusCode.put(SharkConstants.STATE_CLOSED_ABORTED, WSProcessInstanceState.ABORTED);
	}

	private final LookupStore lookupStore;

	public LookupHelper(final LookupStore lookupStore) {
		this.lookupStore = lookupStore;
	}

	public WSProcessInstanceState stateForLookupCode(final String code) {
		if (code == null) {
			// TODO why not UNSUPPORTED?
			return null;
		}
		final WSProcessInstanceState state = stateByFlowStatusCode.get(code);
		return (state == null) ? WSProcessInstanceState.UNSUPPORTED : state;
	}

	public LookupDto lookupForState(final WSProcessInstanceState state) {
		final String code = stateByFlowStatusCode.inverse().get(state);
		return (code == null) ? null : flowStatusWithCode(code);
	}

	private LookupDto flowStatusWithCode(final String code) {
		for (final LookupDto lookup : lookupStore.listForType(FLOW_STATUS)) {
			if (code.equals(lookup.code)) {
				return lookup;
			}
		}
		return null;
	}

	public WSProcessInstanceState stateForLookupId(final Long id) {
		for (final LookupDto lookup : lookupStore.listForType(FLOW_STATUS)) {
			if (id.equals(lookup.id)) {
				final WSProcessInstanceState state = stateForLookupCode(lookup.code);
				return (state == null) ? WSProcessInstanceState.UNSUPPORTED : state;
			}
		}
		return WSProcessInstanceState.UNSUPPORTED;
	}

}
