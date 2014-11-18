package org.cmdbuild.service.rest.cxf.serialization;

import static com.google.common.collect.Lists.newArrayList;
import static org.cmdbuild.service.rest.cxf.serialization.ToAttribute.toAttribute;
import static org.cmdbuild.service.rest.model.Models.newProcessActivityWithFullDetails;

import java.util.List;

import org.cmdbuild.service.rest.model.ProcessActivityWithFullDetails;
import org.cmdbuild.service.rest.model.ProcessActivityWithFullDetails.AttributeStatus;
import org.cmdbuild.workflow.CMActivity;
import org.cmdbuild.workflow.xpdl.CMActivityVariableToProcess;

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

	private ToProcessActivityDefinition(final Builder builder) {
		// TODO Auto-generated constructor stub
	}

	@Override
	public ProcessActivityWithFullDetails apply(final CMActivity input) {
		long index = 0;
		final List<AttributeStatus> attributes = newArrayList();
		for (final CMActivityVariableToProcess element : input.getVariables()) {
			attributes.add(toAttribute(index++).apply(element));
		}
		return newProcessActivityWithFullDetails() //
				.withId(input.getId()) //
				.withDescription(input.getDescription()) //
				.withInstructions(input.getInstructions()) //
				.withAttributes(attributes) //
				.build();
	}
}
