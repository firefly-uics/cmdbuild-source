package org.cmdbuild.services.scheduler.startprocess;

import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;

import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.Validate;
import org.cmdbuild.logger.Log;
import org.cmdbuild.logic.workflow.WorkflowLogic;
import org.cmdbuild.services.scheduler.Command;
import org.cmdbuild.workflow.CMWorkflowException;
import org.slf4j.Logger;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import com.google.common.collect.Maps;

public class StartProcess implements Command {

	private static Logger logger = Log.WORKFLOW;
	private static Marker marker = MarkerFactory.getMarker(StartProcess.class.getName());

	private static final Map<String, String> EMPTY_ATTRIBUTES = Collections.emptyMap();
	private static final Map<String, Object> NO_WIDGETS = Collections.emptyMap();
	private static final boolean ALWAYS_ADVANCE = true;

	public static class Builder implements org.apache.commons.lang3.builder.Builder<StartProcess> {

		private WorkflowLogic workflowLogic;
		private String className;
		private final Map<String, String> attributes = Maps.newHashMap();

		private Builder() {
			// use factory method
		}

		@Override
		public StartProcess build() {
			validate();
			return new StartProcess(this);
		}

		private void validate() {
			Validate.notNull(workflowLogic, "invalid workflow logic");
			Validate.notNull(className, "invalid class name");
		}

		public Builder withWorkflowLogic(final WorkflowLogic workflowLogic) {
			this.workflowLogic = workflowLogic;
			return this;
		}

		public Builder withClassName(final String className) {
			this.className = className;
			return this;
		}

		public Builder withAttributes(final Map<String, String> attributes) {
			this.attributes.putAll(defaultIfNull(attributes, EMPTY_ATTRIBUTES));
			return this;
		}

	}

	public static Builder newInstance() {
		return new Builder();
	}

	private final WorkflowLogic workflowLogic;
	private final String className;
	private final Map<String, String> attributes;

	private StartProcess(final Builder builder) {
		this.workflowLogic = builder.workflowLogic;
		this.className = builder.className;
		this.attributes = builder.attributes;
	}

	@Override
	public void execute() {
		logger.info(marker, "starting scheduled process '{}'", className);
		for (final Entry<String, String> entry : attributes.entrySet()) {
			logger.debug(marker, "\t'{}' -> '{}'", entry.getKey(), entry.getValue());
		}
		try {
			workflowLogic.startProcess(className, attributes, NO_WIDGETS, ALWAYS_ADVANCE);
		} catch (final CMWorkflowException e) {
			logger.warn(marker, "error starting scheduled process", e);
		}
	}

}
