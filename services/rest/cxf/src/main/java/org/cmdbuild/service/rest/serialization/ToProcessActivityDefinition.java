package org.cmdbuild.service.rest.serialization;

import static com.google.common.collect.FluentIterable.from;

import org.cmdbuild.service.rest.dto.ProcessActivityDefinition;
import org.cmdbuild.service.rest.dto.ProcessActivityDefinition.Attribute;
import org.cmdbuild.workflow.CMActivity;
import org.cmdbuild.workflow.xpdl.CMActivityVariableToProcess;
import org.cmdbuild.workflow.xpdl.CMActivityVariableToProcess.Type;

import com.google.common.base.Function;

public class ToProcessActivityDefinition implements Function<CMActivity, ProcessActivityDefinition> {

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

	private ToProcessActivityDefinition(final Builder builder) {
		// TODO Auto-generated constructor stub
	}

	@Override
	public ProcessActivityDefinition apply(final CMActivity input) {
		return ProcessActivityDefinition.newInstance() //
				.withId(input.getId()) //
				.withDescription(input.getDescription()) //
				.withInstructions(input.getInstructions()) //
				.withAttributes(from(input.getVariables()) //
						.transform(TO_ATTRIBUTE) //
						.toList() //
				) //
				.build();
	}
}
