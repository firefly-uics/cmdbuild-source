package org.cmdbuild.service.rest.cxf.serialization;

import static com.google.common.collect.FluentIterable.from;
import static com.google.common.collect.Lists.newArrayList;
import static org.cmdbuild.service.rest.cxf.serialization.ToAttribute.toAttribute;
import static org.cmdbuild.service.rest.model.Models.newProcessActivityWithFullDetails;
import static org.cmdbuild.service.rest.model.Models.newValues;
import static org.cmdbuild.service.rest.model.Models.newWidget;

import java.util.List;
import java.util.Map;

import org.cmdbuild.service.rest.model.ProcessActivityWithFullDetails;
import org.cmdbuild.service.rest.model.ProcessActivityWithFullDetails.AttributeStatus;
import org.cmdbuild.service.rest.model.Widget;
import org.cmdbuild.workflow.CMActivity;
import org.cmdbuild.workflow.CMWorkflowException;
import org.cmdbuild.workflow.xpdl.CMActivityVariableToProcess;
import org.codehaus.jackson.map.ObjectMapper;

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
				.withWidgets(safeWidgetsOf(input)) //
				.build();
	}

	private Iterable<Widget> safeWidgetsOf(final CMActivity input) {
		try {
			return from(input.getWidgets()) //
					.filter(org.cmdbuild.model.widget.Widget.class) //
					.transform(new Function<org.cmdbuild.model.widget.Widget, Widget>() {

						@Override
						public Widget apply(final org.cmdbuild.model.widget.Widget input) {
							/*
							 * TODO do in a better way
							 */
							final ObjectMapper objectMapper = new ObjectMapper();
							@SuppressWarnings("unchecked")
							final Map<String, Object> objectAsMap = objectMapper.convertValue(input, Map.class);
							return newWidget() //
									.withId(input.getIdentifier()) //
									.withType(input.getType()) //
									.withActive(input.isActive()) //
									// .withRequired(...) //
									.withLabel(input.getLabel()) //
									.withData(newValues() //
											.withValues(objectAsMap) //
											.build()) //
									.build();
						}

					});
		} catch (final CMWorkflowException e) {
			throw new RuntimeException(e);
		}
	}
}
