package org.cmdbuild.service.rest.serialization;

import org.cmdbuild.service.rest.dto.ProcessActivity.Attribute;
import org.cmdbuild.service.rest.dto.ProcessActivityShort;
import org.cmdbuild.workflow.user.UserActivityInstance;
import org.cmdbuild.workflow.xpdl.CMActivityVariableToProcess;
import org.cmdbuild.workflow.xpdl.CMActivityVariableToProcess.Type;

import com.google.common.base.Function;

public class ToProcessActivityShort implements Function<UserActivityInstance, ProcessActivityShort> {

	public static class Builder implements org.apache.commons.lang3.builder.Builder<ToProcessActivityShort> {

		private Builder() {
			// use static method
		}

		@Override
		public ToProcessActivityShort build() {
			validate();
			return new ToProcessActivityShort(this);
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

	private ToProcessActivityShort(final Builder builder) {
		// TODO Auto-generated constructor stub
	}

	@Override
	public ProcessActivityShort apply(final UserActivityInstance input) {
		return ProcessActivityShort.newInstance() //
				.withId(input.getId()) //
				.withWritableStatus(input.isWritable()) //
				.build();
	}
}
