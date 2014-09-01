package org.cmdbuild.service.rest.serialization;

import org.cmdbuild.service.rest.dto.ProcessActivity;
import org.cmdbuild.service.rest.dto.ProcessActivityDefinition.Attribute;
import org.cmdbuild.workflow.user.UserActivityInstance;
import org.cmdbuild.workflow.xpdl.CMActivityVariableToProcess;
import org.cmdbuild.workflow.xpdl.CMActivityVariableToProcess.Type;

import com.google.common.base.Function;

public class ToProcessActivity implements Function<UserActivityInstance, ProcessActivity> {

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

	private static class ToAttribute implements Function<CMActivityVariableToProcess, Attribute> {

		@Override
		public Attribute apply(final CMActivityVariableToProcess input) {
			return Attribute.newInstance() //
					.withId(input.getName()) //
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
	public ProcessActivity apply(final UserActivityInstance input) {
		return ProcessActivity.newInstance() //
				.withId(input.getId()) //
				.withWritableStatus(input.isWritable()) //
				.build();
	}
}
