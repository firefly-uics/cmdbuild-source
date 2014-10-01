package org.cmdbuild.service.rest.cxf.serialization;

import static com.google.common.collect.FluentIterable.from;
import static org.cmdbuild.service.rest.cxf.serialization.FakeId.fakeId;
import static org.cmdbuild.service.rest.model.Builders.newAttributeStatus;
import static org.cmdbuild.service.rest.model.Builders.newProcessActivityWithFullDetails;

import org.cmdbuild.service.rest.model.ProcessActivityWithFullDetails;
import org.cmdbuild.service.rest.model.ProcessActivityWithFullDetails.AttributeStatus;
import org.cmdbuild.workflow.CMActivity;
import org.cmdbuild.workflow.xpdl.CMActivityVariableToProcess;
import org.cmdbuild.workflow.xpdl.CMActivityVariableToProcess.Type;

import com.google.common.base.Function;

public class ToProcessActivityDefinition implements Function<CMActivity, ProcessActivityWithFullDetails> {

	public static class Builder implements org.apache.commons.lang3.builder.Builder<ToProcessActivityDefinition> {

		private Builder() {
			// use static method
		}

		@Override
		public ToProcessActivityDefinition build() {
			validate();
			return new ToProcessActivityDefinition(this);
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

	private ToProcessActivityDefinition(final Builder builder) {
		// TODO Auto-generated constructor stub
	}

	@Override
	public ProcessActivityWithFullDetails apply(final CMActivity input) {
		return newProcessActivityWithFullDetails() //
				.withId(fakeId(input.getId())) //
				.withDescription(input.getDescription()) //
				.withInstructions(input.getInstructions()) //
				.withAttributes(from(input.getVariables()) //
						.transform(TO_ATTRIBUTE) //
						.toList() //
				) //
				.build();
	}
}
