package org.cmdbuild.logic.taskmanager;

import static org.apache.commons.lang3.StringUtils.defaultString;
import static org.apache.commons.lang3.StringUtils.isEmpty;
import static org.apache.commons.lang3.SystemUtils.LINE_SEPARATOR;

import java.util.Collections;
import java.util.Map;

import org.apache.commons.lang3.Validate;
import org.cmdbuild.data.store.task.ReadEmailTaskDefinition;
import org.cmdbuild.data.store.task.StartWorkflowTaskDefinition;
import org.cmdbuild.data.store.task.SynchronousEventTaskDefinition;
import org.cmdbuild.data.store.task.TaskVisitor;
import org.cmdbuild.logic.taskmanager.SynchronousEventTask.Phase;
import org.slf4j.Logger;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;

public class DefaultLogicAndStoreConverter implements LogicAndStoreConverter {

	/**
	 * Container for all {@link ReadEmailTaskDefinition} parameter names.
	 */
	public static class ReadEmail {

		private ReadEmail() {
			// prevents instantiation
		}

		public static final String ACCOUNT_NAME = "email.account.name";

		private static final String FILTER = "email.filter";
		public static final String FILTER_FROM_REGEX = FILTER + ".from.regex";
		public static final String FILTER_SUBJECT_REGEX = FILTER + ".subject.regex";

		private static final String RULE = "email.rule";

		public static final String RULE_ATTACHMENTS_ACTIVE = RULE + ".attachments.active";
		public static final String RULE_NOTIFICATION_ACTIVE = RULE + ".notification.active";

		private static final String RULE_WORKFLOW = "email.rule.workflow";
		public static final String RULE_WORKFLOW_ACTIVE = RULE_WORKFLOW + ".active";
		public static final String RULE_WORKFLOW_ADVANCE = RULE_WORKFLOW + ".advance";
		public static final String RULE_WORKFLOW_CLASS_NAME = RULE_WORKFLOW + ".class.name";
		public static final String RULE_WORKFLOW_FIELDS_MAPPING = RULE_WORKFLOW + ".fields.mapping";
		public static final String RULE_WORKFLOW_ATTACHMENTS_SAVE = RULE_WORKFLOW + ".attachments.save";

	}

	/**
	 * Container for all {@link StartWorkflowTaskDefinition} parameter names.
	 */
	public static class StartWorkflow {

		private StartWorkflow() {
			// prevents instantiation
		}

		public static final String CLASSNAME = "classname";
		public static final String ATTRIBUTES = "attributes";

	}

	/**
	 * Container for all {@link SynchronousEventTaskDefinition} parameter names.
	 */
	public static class SynchronousEvent {

		private SynchronousEvent() {
			// prevents instantiation
		}

		public static final String PHASE = "phase";

		public static final String PHASE_AFTER_CREATE = "afterCreate";
		public static final String PHASE_BEFORE_UPDATE = "beforeUpdate";
		public static final String PHASE_AFTER_UPDATE = "afterUpdate";
		public static final String PHASE_BEFORE_DELETE = "beforeDelete";

		private static final String ACTION_SCRIPT = "action.scripting";
		public static final String ACTION_SCRIPT_ACTIVE = ACTION_SCRIPT + ".active";
		public static final String ACTION_SCRIPT_ENGINE = ACTION_SCRIPT + ".engine";
		public static final String ACTION_SCRIPT_SCRIPT = ACTION_SCRIPT + ".script";

	}

	private static final String KEY_VALUE_SEPARATOR = "=";

	private static final Logger logger = TaskManagerLogic.logger;
	private static final Marker marker = MarkerFactory.getMarker(DefaultLogicAndStoreConverter.class.getName());

	private static class PhaseToStoreConverter implements SynchronousEventTask.PhaseIdentifier {

		private final SynchronousEventTask task;
		private String converted;

		public PhaseToStoreConverter(final SynchronousEventTask task) {
			this.task = task;
		}

		public String toStore() {
			task.getPhase().identify(this);
			Validate.notNull(converted, "conversion error");
			return converted;
		}

		@Override
		public void afterCreate() {
			converted = SynchronousEvent.PHASE_AFTER_CREATE;
		}

		@Override
		public void beforeUpdate() {
			converted = SynchronousEvent.PHASE_BEFORE_UPDATE;
		}

		@Override
		public void afterUpdate() {
			converted = SynchronousEvent.PHASE_AFTER_UPDATE;
		}

		@Override
		public void beforeDelete() {
			converted = SynchronousEvent.PHASE_BEFORE_DELETE;
		}

	}

	private static class PhaseToLogicConverter {

		private final String stored;

		public PhaseToLogicConverter(final String stored) {
			this.stored = stored;
		}

		public Phase toLogic() {
			final Phase converted;
			if (SynchronousEvent.PHASE_AFTER_CREATE.equals(stored)) {
				converted = Phase.AFTER_CREATE;
			} else if (SynchronousEvent.PHASE_BEFORE_UPDATE.equals(stored)) {
				converted = Phase.BEFORE_UPDATE;
			} else if (SynchronousEvent.PHASE_AFTER_UPDATE.equals(stored)) {
				converted = Phase.AFTER_UPDATE;
			} else if (SynchronousEvent.PHASE_BEFORE_DELETE.equals(stored)) {
				converted = Phase.BEFORE_DELETE;
			} else {
				converted = null;
			}
			Validate.notNull(converted, "conversion error");
			return converted;
		}

	}

	private static class DefaultLogicAsSourceConverter implements LogicAsSourceConverter, TaskVistor {

		private final Task source;

		private org.cmdbuild.data.store.task.Task target;

		public DefaultLogicAsSourceConverter(final Task source) {
			this.source = source;
		}

		@Override
		public org.cmdbuild.data.store.task.Task toStore() {
			logger.info(marker, "converting task '{}' to scheduler job", source);
			source.accept(this);
			Validate.notNull(target, "conversion error");
			return target;
		}

		@Override
		public void visit(final ReadEmailTask task) {
			this.target = org.cmdbuild.data.store.task.ReadEmailTask.newInstance() //
					.withId(task.getId()) //
					.withDescription(task.getDescription()) //
					.withRunningStatus(task.isActive()) //
					.withCronExpression(task.getCronExpression()) //
					.withParameter(ReadEmail.ACCOUNT_NAME, task.getEmailAccount()) //
					.withParameter(ReadEmail.FILTER_FROM_REGEX, task.getRegexFromFilter()) //
					.withParameter(ReadEmail.FILTER_SUBJECT_REGEX, task.getRegexSubjectFilter()) //
					.withParameter(ReadEmail.RULE_NOTIFICATION_ACTIVE, //
							Boolean.toString(task.isNotificationRuleActive())) //
					.withParameter(ReadEmail.RULE_ATTACHMENTS_ACTIVE, //
							Boolean.toString(task.isAttachmentsRuleActive())) //
					.withParameter(ReadEmail.RULE_WORKFLOW_ACTIVE, //
							Boolean.toString(task.isWorkflowRuleActive())) //
					.withParameter(ReadEmail.RULE_WORKFLOW_CLASS_NAME, task.getWorkflowClassName()) //
					.withParameter(ReadEmail.RULE_WORKFLOW_FIELDS_MAPPING, task.getWorkflowFieldsMapping()) //
					.withParameter(ReadEmail.RULE_WORKFLOW_ADVANCE, //
							Boolean.toString(task.isWorkflowAdvanceable())) //
					.withParameter(ReadEmail.RULE_WORKFLOW_ATTACHMENTS_SAVE, //
							Boolean.toString(task.isWorkflowAttachments())) //
					.build();
		}

		@Override
		public void visit(final StartWorkflowTask task) {
			this.target = org.cmdbuild.data.store.task.StartWorkflowTask.newInstance() //
					.withId(task.getId()) //
					.withDescription(task.getDescription()) //
					.withRunningStatus(task.isActive()) //
					.withCronExpression(task.getCronExpression()) //
					.withParameter(StartWorkflow.CLASSNAME, task.getProcessClass()) //
					.withParameter(StartWorkflow.ATTRIBUTES, Joiner.on(LINE_SEPARATOR) //
							.withKeyValueSeparator(KEY_VALUE_SEPARATOR) //
							.join(task.getAttributes())) //
					.build();
		}

		@Override
		public void visit(final SynchronousEventTask task) {
			this.target = org.cmdbuild.data.store.task.SynchronousEventTask.newInstance() //
					.withId(task.getId()) //
					.withDescription(task.getDescription()) //
					.withRunningStatus(task.isActive()) //
					.withParameter(SynchronousEvent.PHASE, new PhaseToStoreConverter(task).toStore()) //
					.withParameter(SynchronousEvent.ACTION_SCRIPT_ACTIVE, Boolean.toString(task.isScriptingEnabled())) //
					.withParameter(SynchronousEvent.ACTION_SCRIPT_ENGINE, task.getScriptingEngine()) //
					.withParameter(SynchronousEvent.ACTION_SCRIPT_SCRIPT, task.getScriptingScript()) //
					.build();
		}
	}

	private static class DefaultStoreAsSourceConverter implements StoreAsSourceConverter, TaskVisitor {

		private static final Map<String, String> EMPTY_PARAMETERS = Collections.emptyMap();

		private final org.cmdbuild.data.store.task.Task source;

		private Task target;

		public DefaultStoreAsSourceConverter(final org.cmdbuild.data.store.task.Task source) {
			this.source = source;
		}

		@Override
		public Task toLogic() {
			logger.info(marker, "converting scheduler job '{}' to scheduled task");
			source.accept(this);
			Validate.notNull(target, "conversion error");
			return target;
		}

		@Override
		public void visit(final org.cmdbuild.data.store.task.ReadEmailTask task) {
			target = ReadEmailTask.newInstance() //
					.withId(task.getId()) //
					.withDescription(task.getDescription()) //
					.withActiveStatus(task.isRunning()) //
					.withCronExpression(task.getCronExpression()) //
					.withEmailAccount(task.getParameter(ReadEmail.ACCOUNT_NAME)) //
					.withRegexFromFilter(task.getParameter(ReadEmail.FILTER_FROM_REGEX)) //
					.withRegexSubjectFilter(task.getParameter(ReadEmail.FILTER_SUBJECT_REGEX)) //
					.withNotificationStatus( //
							Boolean.valueOf(task.getParameter(ReadEmail.RULE_NOTIFICATION_ACTIVE))) //
					.withAttachmentsRuleActive( //
							Boolean.valueOf(task.getParameter(ReadEmail.RULE_ATTACHMENTS_ACTIVE))) //
					.withWorkflowRuleActive( //
							Boolean.valueOf(task.getParameter(ReadEmail.RULE_WORKFLOW_ACTIVE))) //
					.withWorkflowClassName(task.getParameter(ReadEmail.RULE_WORKFLOW_CLASS_NAME)) //
					.withWorkflowFieldsMapping(task.getParameter(ReadEmail.RULE_WORKFLOW_FIELDS_MAPPING)) //
					.withWorkflowAdvanceableStatus( //
							Boolean.valueOf(task.getParameter(ReadEmail.RULE_WORKFLOW_ADVANCE))) //
					.withWorkflowAttachmentsStatus( //
							Boolean.valueOf(task.getParameter(ReadEmail.RULE_WORKFLOW_ATTACHMENTS_SAVE))) //
					.build();
		}

		@Override
		public void visit(final org.cmdbuild.data.store.task.StartWorkflowTask task) {
			final String attributesAsString = defaultString(task.getParameter(StartWorkflow.ATTRIBUTES));
			target = StartWorkflowTask.newInstance() //
					.withId(task.getId()) //
					.withDescription(task.getDescription()) //
					.withActiveStatus(task.isRunning()) //
					.withCronExpression(task.getCronExpression()) //
					.withProcessClass(task.getParameter(StartWorkflow.CLASSNAME)) //
					.withAttributes(isEmpty(attributesAsString) ? EMPTY_PARAMETERS : Splitter.on(LINE_SEPARATOR) //
							.withKeyValueSeparator(KEY_VALUE_SEPARATOR) //
							.split(attributesAsString)) //
					.build();
		}

		@Override
		public void visit(final org.cmdbuild.data.store.task.SynchronousEventTask task) {
			target = SynchronousEventTask.newInstance() //
					.withId(task.getId()) //
					.withDescription(task.getDescription()) //
					.withActiveStatus(task.isRunning()) //
					.withPhase( //
							new PhaseToLogicConverter(task.getParameter(SynchronousEvent.PHASE)) //
									.toLogic()) //
					.withScriptingEnableStatus( //
							Boolean.valueOf(task.getParameter(SynchronousEvent.ACTION_SCRIPT_ACTIVE))) //
					.withScriptingEngine(task.getParameter(SynchronousEvent.ACTION_SCRIPT_ENGINE)) //
					.withScript(task.getParameter(SynchronousEvent.ACTION_SCRIPT_SCRIPT)) //
					.build();
		}
	}

	@Override
	public LogicAsSourceConverter from(final Task source) {
		return new DefaultLogicAsSourceConverter(source);
	}

	@Override
	public StoreAsSourceConverter from(final org.cmdbuild.data.store.task.Task source) {
		return new DefaultStoreAsSourceConverter(source);
	}

}
