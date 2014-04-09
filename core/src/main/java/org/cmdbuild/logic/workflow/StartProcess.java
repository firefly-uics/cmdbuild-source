package org.cmdbuild.logic.workflow;

import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.Validate;
import org.cmdbuild.logic.Action;
import org.cmdbuild.workflow.CMWorkflowException;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import com.google.common.collect.Maps;

public class StartProcess implements Action {

	private static final Marker marker = MarkerFactory.getMarker(StartProcess.class.getName());

	public static class Builder implements org.apache.commons.lang3.builder.Builder<StartProcess> {

		private static final Map<String, Object> EMPTY_ATTRIBUTES = Collections.emptyMap();

		private WorkflowLogic workflowLogic;
		private String className;
		private final Map<String, Object> attributes;

		private Builder() {
			// use factory method
			attributes = Maps.newHashMap();
		}

		@Override
		public StartProcess build() {
			validate();
			return new StartProcess(this);
		}

		private void validate() {
			Validate.notNull(workflowLogic, "missing workflow logic");
			Validate.notBlank(className, "missing class name");
		}

		public Builder withWorkflowLogic(final WorkflowLogic workflowLogic) {
			this.workflowLogic = workflowLogic;
			return this;
		}

		public Builder withClassName(final String classname) {
			this.className = classname;
			return this;
		}

		public Builder withAttribute(final String name, final Object value) {
			if (isNotBlank(name)) {
				attributes.put(name, value);
			}
			return this;
		}

		public Builder withAttributes(final Map<String, Object> attributes) {
			for (final Entry<String, Object> entry : defaultIfNull(attributes, EMPTY_ATTRIBUTES).entrySet()) {
				withAttribute(entry.getKey(), entry.getValue());
			}
			return this;
		}

	}

	private static final Map<String, Object> NO_WIDGETS = Collections.emptyMap();	
	private static final boolean ALWAYS_ADVANCE = true;

	public static Builder newInstance() {
		return new Builder();
	}

	private final WorkflowLogic workflowLogic;
	private final String className;
	private final Map<String, Object> attributes;

	private StartProcess(final Builder builder) {
		this.workflowLogic = builder.workflowLogic;
		this.className = builder.className;
		this.attributes = builder.attributes;
	}

	@Override
	public void execute() {
		try {
			workflowLogic.startProcess(className, attributes, NO_WIDGETS, ALWAYS_ADVANCE);
		} catch (final CMWorkflowException e) {
			logger.error(marker, "error starting process", e);
			throw new RuntimeException(e);
		}
	}

}
