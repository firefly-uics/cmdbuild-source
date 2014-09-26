package org.cmdbuild.service.rest.cxf.serialization;

import static org.cmdbuild.service.rest.dto.Builders.newAttributeStatus;
import static org.cmdbuild.service.rest.dto.Builders.newProcessActivityWithBasicDetails;

import org.cmdbuild.service.rest.dto.ProcessActivityWithBasicDetails;
import org.cmdbuild.service.rest.dto.ProcessActivityWithFullDetails.AttributeStatus;
import org.cmdbuild.workflow.user.UserActivityInstance;
import org.cmdbuild.workflow.xpdl.CMActivityVariableToProcess;
import org.cmdbuild.workflow.xpdl.CMActivityVariableToProcess.Type;

import com.google.common.base.Function;

public class ToProcessActivity implements Function<UserActivityInstance, ProcessActivityWithBasicDetails> {

	public static class Builder implements org.apache.commons.lang3.builder.Builder<ToProcessActivity> {

		private Builder() {
			// use static method
		}

		@Override
		public ToProcessActivity build() {
			validate();
			return new ToProcessActivity(this);
		}

		private void validate() {
			// TODO Auto-generated method stub
		}

	}

	public static Builder newInstance() {
		return new Builder();
	}

	private static class ToAttribute implements Function<CMActivityVariableToProcess, AttributeStatus> {

		@Override
		public AttributeStatus apply(final CMActivityVariableToProcess input) {
			return newAttributeStatus() //
					// TODO fake attribute
					.withId(Long.valueOf(input.getName().hashCode())) //
					.withWritable(input.getType() != Type.READ_ONLY) //
					.withMandatory(input.getType() == Type.READ_WRITE_REQUIRED) //
					.build();
		}

	}

	public static final ToAttribute TO_ATTRIBUTE = new ToAttribute();

	private ToProcessActivity(final Builder builder) {
		// TODO Auto-generated constructor stub
	}

	@Override
	public ProcessActivityWithBasicDetails apply(final UserActivityInstance input) {
		return newProcessActivityWithBasicDetails() //
				// TODO fake id
				.withId(Long.valueOf(input.getId().hashCode())) //
				.withWritableStatus(input.isWritable()) //
				.build();
	}

}
