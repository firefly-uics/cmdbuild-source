package org.cmdbuild.service.rest.cxf.serialization;

import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.cmdbuild.service.rest.cxf.serialization.FakeId.fakeId;
import static org.cmdbuild.service.rest.model.Builders.newAttributeStatus;
import static org.cmdbuild.service.rest.model.Builders.newProcessActivityWithBasicDetails;

import org.cmdbuild.service.rest.model.ProcessActivityWithBasicDetails;
import org.cmdbuild.service.rest.model.ProcessActivityWithFullDetails.AttributeStatus;
import org.cmdbuild.workflow.CMWorkflowException;
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
					.withId(fakeId(input.getName())) //
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
				.withId(fakeId(input.getId())) //
				.withWritableStatus(input.isWritable()) //
				.withDescription(safeDescriptionOf(input)) //
				.build();
	}

	private String safeDescriptionOf(final UserActivityInstance input) {
		try {
			return input.getDefinition().getDescription();
		} catch (CMWorkflowException e) {
			return EMPTY;
		}
	}

}
