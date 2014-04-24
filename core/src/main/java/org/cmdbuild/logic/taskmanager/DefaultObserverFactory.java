package org.cmdbuild.logic.taskmanager;

import static com.google.common.collect.Iterables.contains;
import static com.google.common.collect.Iterables.isEmpty;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.cmdbuild.services.event.Commands.safe;

import org.apache.commons.lang3.Validate;
import org.cmdbuild.api.fluent.FluentApi;
import org.cmdbuild.auth.UserStore;
import org.cmdbuild.common.template.TemplateResolver;
import org.cmdbuild.common.template.engine.EngineBasedTemplateResolver;
import org.cmdbuild.dao.entry.CMCard;
import org.cmdbuild.logic.taskmanager.DefaultLogicAndObserverConverter.ObserverFactory;
import org.cmdbuild.logic.workflow.StartProcess;
import org.cmdbuild.logic.workflow.WorkflowLogic;
import org.cmdbuild.services.event.Command;
import org.cmdbuild.services.event.Context;
import org.cmdbuild.services.event.ContextVisitor;
import org.cmdbuild.services.event.Contexts.AfterCreate;
import org.cmdbuild.services.event.Contexts.AfterUpdate;
import org.cmdbuild.services.event.Contexts.BeforeDelete;
import org.cmdbuild.services.event.Contexts.BeforeUpdate;
import org.cmdbuild.services.event.DefaultObserver;
import org.cmdbuild.services.event.DefaultObserver.Builder;
import org.cmdbuild.services.event.FilteredObserver;
import org.cmdbuild.services.event.Observer;
import org.cmdbuild.services.event.ScriptCommand;
import org.cmdbuild.services.template.engine.CardEngine;

import com.google.common.base.Predicate;

public class DefaultObserverFactory implements ObserverFactory {

	private static class SynchronousEventTaskPredicate implements Predicate<CMCard> {

		public static class Builder implements org.apache.commons.lang3.builder.Builder<SynchronousEventTaskPredicate> {

			private SynchronousEventTask task;
			private UserStore userStore;

			private Builder() {
				// use factory method
			}

			@Override
			public SynchronousEventTaskPredicate build() {
				validate();
				return new SynchronousEventTaskPredicate(this);
			}

			private void validate() {
				Validate.notNull(task, "invalid task");
				Validate.notNull(userStore, "invalid user store");
			}

			public Builder withTask(final SynchronousEventTask task) {
				this.task = task;
				return this;
			}

			public Builder withUserStore(final UserStore userStore) {
				this.userStore = userStore;
				return this;
			}

		}

		public static Builder newInstance() {
			return new Builder();
		}

		private final SynchronousEventTask task;
		private final UserStore userStore;

		private SynchronousEventTaskPredicate(final Builder builder) {
			this.task = builder.task;
			this.userStore = builder.userStore;
		}

		@Override
		public boolean apply(final CMCard input) {
			return matchesGroup() && matchesClass(input);
		}

		private boolean matchesGroup() {
			final String current = userStore.getUser().getPreferredGroup().getName();
			return isEmpty(task.getGroups()) || contains(task.getGroups(), current);
		}

		private boolean matchesClass(final CMCard input) {
			return isBlank(task.getTargetClassname()) || input.getType().getName().equals(task.getTargetClassname());
		}

	}

	private static final String CURRENT_CARD_PREFIX = "current";
	private static final String NEXT_CARD_PREFIX = "next";
	private static final String PREVIOUS_CARD_PREFIX = "previous";

	private final UserStore userStore;
	private final FluentApi fluentApi;
	private final WorkflowLogic workflowLogic;

	public DefaultObserverFactory(final UserStore userStore, final FluentApi fluentApi,
			final WorkflowLogic workflowLogic) {
		this.userStore = userStore;
		this.fluentApi = fluentApi;
		this.workflowLogic = workflowLogic;
	}

	@Override
	public Observer create(final SynchronousEventTask task) {
		final Builder builder = DefaultObserver.newInstance();
		final DefaultObserver.Phase phase = phaseOf(task);
		if (task.isWorkflowEnabled()) {
			builder.add(workflowOf(task), phase);
		}
		if (task.isScriptingEnabled()) {
			builder.add(scriptingOf(task), phase);
		}
		final DefaultObserver base = builder.build();
		return FilteredObserver.newInstance() //
				.withDelegate(base) //
				.withFilter(filterOf(task)) //
				.build();
	}

	private DefaultObserver.Phase phaseOf(final SynchronousEventTask task) {
		return new SynchronousEventTask.PhaseIdentifier() {

			private DefaultObserver.Phase converted;

			public org.cmdbuild.services.event.DefaultObserver.Phase toObserverPhase() {
				task.getPhase().identify(this);
				Validate.notNull(converted, "conversion error");
				return converted;
			}

			@Override
			public void afterCreate() {
				converted = DefaultObserver.Phase.AFTER_CREATE;
			}

			@Override
			public void beforeUpdate() {
				converted = DefaultObserver.Phase.BEFORE_UPDATE;
			}

			@Override
			public void afterUpdate() {
				converted = DefaultObserver.Phase.AFTER_UPDATE;
			}

			@Override
			public void beforeDelete() {
				converted = DefaultObserver.Phase.BEFORE_DELETE;
			}

		}.toObserverPhase();
	}

	private Command workflowOf(final SynchronousEventTask task) {
		return safe(new Command() {

			@Override
			public void execute(final Context context) {
				StartProcess.newInstance() //
						.withWorkflowLogic(workflowLogic) //
						.withClassName(task.getWorkflowClassName()) //
						.withAttributes(task.getWorkflowAttributes()) //
						.withAdvanceStatus(task.isWorkflowAdvanceable()) //
						.withTemplateResolver(templateResolverOf(context)) //
						.build() //
						.execute();
			}

			private TemplateResolver templateResolverOf(final Context context) {
				final EngineBasedTemplateResolver.Builder builder = EngineBasedTemplateResolver.newInstance();
				context.accept(new ContextVisitor() {
					@Override
					public void visit(final AfterCreate context) {
						builder.withEngine(//
								CardEngine.newInstance() //
										.withCard(context.card) //
										.build(), //
								CURRENT_CARD_PREFIX);
					}

					@Override
					public void visit(final BeforeUpdate context) {
						builder.withEngine(//
								CardEngine.newInstance() //
										.withCard(context.actual) //
										.build(), //
								CURRENT_CARD_PREFIX);
						builder.withEngine(//
								CardEngine.newInstance() //
										.withCard(context.next) //
										.build(), //
								NEXT_CARD_PREFIX);
					}

					@Override
					public void visit(final AfterUpdate context) {
						builder.withEngine(//
								CardEngine.newInstance() //
										.withCard(context.previous) //
										.build(), //
								PREVIOUS_CARD_PREFIX);
						builder.withEngine(//
								CardEngine.newInstance() //
										.withCard(context.actual) //
										.build(), //
								CURRENT_CARD_PREFIX);
					}

					@Override
					public void visit(final BeforeDelete context) {
						builder.withEngine(//
								CardEngine.newInstance() //
										.withCard(context.card) //
										.build(), //
								CURRENT_CARD_PREFIX);
					}

				});
				return builder.build();
			}

		});
	}

	private Command scriptingOf(final SynchronousEventTask task) {
		final Command command = ScriptCommand.newInstance() //
				.withEngine(task.getScriptingEngine()) //
				.withScript(task.getScriptingScript()) //
				.withFluentApi(fluentApi) //
				.build();
		return task.isScriptingSafe() ? safe(command) : command;
	}

	private Predicate<CMCard> filterOf(final SynchronousEventTask task) {
		return SynchronousEventTaskPredicate.newInstance() //
				.withTask(task) //
				.withUserStore(userStore) //
				.build();
	}

}
