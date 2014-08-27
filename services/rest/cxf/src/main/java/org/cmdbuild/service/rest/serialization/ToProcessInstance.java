package org.cmdbuild.service.rest.serialization;

import org.cmdbuild.service.rest.dto.ProcessInstance;
import org.cmdbuild.workflow.user.UserProcessInstance;

import com.google.common.base.Function;

public class ToProcessInstance implements Function<UserProcessInstance, ProcessInstance> {

	public static class Builder implements org.apache.commons.lang3.builder.Builder<ToProcessInstance> {

		private Builder() {
			// use static method
		}

		@Override
		public ToProcessInstance build() {
			return new ToProcessInstance(this);
		}

	}

	public static Builder newInstance() {
		return new Builder();
	}

	private ToProcessInstance(final Builder builder) {
		// nothing to do
	}

	@Override
	public ProcessInstance apply(final UserProcessInstance input) {
		return ProcessInstance.newInstance() //
				.withName(input.getProcessInstanceId()) //
				.build();
	}

}
