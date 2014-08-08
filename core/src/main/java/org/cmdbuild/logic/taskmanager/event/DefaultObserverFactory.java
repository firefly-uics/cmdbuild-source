package org.cmdbuild.logic.taskmanager.event;

import static com.google.common.collect.Iterables.contains;
import static com.google.common.collect.Iterables.isEmpty;
import static java.lang.String.format;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.cmdbuild.common.template.engine.Engines.emptyStringOnNull;
import static org.cmdbuild.common.template.engine.Engines.nullOnError;
import static org.cmdbuild.services.email.Predicates.named;
import static org.cmdbuild.services.event.Commands.safe;
import static org.cmdbuild.services.template.engine.EngineNames.CURRENT_CARD_PREFIX;
import static org.cmdbuild.services.template.engine.EngineNames.GROUP_PREFIX;
import static org.cmdbuild.services.template.engine.EngineNames.GROUP_USERS_PREFIX;
import static org.cmdbuild.services.template.engine.EngineNames.NEXT_CARD_PREFIX;
import static org.cmdbuild.services.template.engine.EngineNames.PREVIOUS_CARD_PREFIX;
import static org.cmdbuild.services.template.engine.EngineNames.USER_PREFIX;

import org.apache.commons.lang3.Validate;
import org.cmdbuild.api.fluent.FluentApi;
import org.cmdbuild.auth.UserStore;
import org.cmdbuild.common.template.TemplateResolver;
import org.cmdbuild.common.template.engine.EngineBasedTemplateResolver;
import org.cmdbuild.dao.entry.CMCard;
import org.cmdbuild.dao.logging.LoggingSupport;
import org.cmdbuild.dao.query.CMQueryResult;
import org.cmdbuild.dao.view.CMDataView;
import org.cmdbuild.data.store.Store;
import org.cmdbuild.data.store.email.EmailConstants;
import org.cmdbuild.logic.data.QueryOptions;
import org.cmdbuild.logic.data.access.QuerySpecsBuilderFiller;
import org.cmdbuild.logic.email.EmailTemplateLogic;
import org.cmdbuild.logic.email.EmailTemplateLogic.Template;
import org.cmdbuild.logic.email.SendTemplateEmail;
import org.cmdbuild.logic.mapping.json.JsonFilterHelper;
import org.cmdbuild.logic.taskmanager.task.event.synchronous.SynchronousEventTask;
import org.cmdbuild.logic.taskmanager.util.CardIdFilterElementGetter;
import org.cmdbuild.logic.workflow.StartProcess;
import org.cmdbuild.logic.workflow.WorkflowLogic;
import org.cmdbuild.services.email.EmailAccount;
import org.cmdbuild.services.email.EmailServiceFactory;
import org.cmdbuild.services.email.PredicateEmailAccountSupplier;
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
import org.cmdbuild.services.template.engine.GroupEmailEngine;
import org.cmdbuild.services.template.engine.GroupUsersEmailEngine;
import org.cmdbuild.services.template.engine.UserEmailEngine;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import com.google.common.base.Predicate;
import com.google.common.base.Supplier;

public class DefaultObserverFactory implements ObserverFactory {

	private static final Logger logger = LoggingSupport.logger;
	private static final Marker marker = MarkerFactory.getMarker(DefaultObserverFactory.class.getName());

	private static class SynchronousEventTaskPredicate implements Predicate<CMCard> {

		public static class Builder implements org.apache.commons.lang3.builder.Builder<SynchronousEventTaskPredicate> {

			private SynchronousEventTask task;
			private UserStore userStore;
			private Supplier<CMDataView> dataView;

			private Builder() {
				// use factory method
			}

			@Override
			public SynchronousEventTaskPredicate build() {
				validate();
				return new SynchronousEventTaskPredicate(this);
			}

			private void validate() {
				Validate.notNull(task, "invalid '%s'", SynchronousEventTask.class);
				Validate.notNull(userStore, "invalid '%s'", UserStore.class);
				Validate.notNull(dataView, "invalid '%s'", CMDataView.class);
			}

			public Builder withTask(final SynchronousEventTask task) {
				this.task = task;
				return this;
			}

			public Builder withUserStore(final UserStore userStore) {
				this.userStore = userStore;
				return this;
			}

			public Builder withDataView(final Supplier<CMDataView> dataView) {
				this.dataView = dataView;
				return this;
			}

		}

		public static Builder newInstance() {
			return new Builder();
		}

		private final SynchronousEventTask task;
		private final UserStore userStore;
		private final Supplier<CMDataView> dataView;

		private SynchronousEventTaskPredicate(final Builder builder) {
			this.task = builder.task;
			this.userStore = builder.userStore;
			this.dataView = builder.dataView;
		}

		@Override
		public boolean apply(final CMCard input) {
			return matchesGroup() && matchesClass(input) && matchesCards(input);
		}

		private boolean matchesGroup() {
			final String current = userStore.getUser().getPreferredGroup().getName();
			return isEmpty(task.getGroups()) || contains(task.getGroups(), current);
		}

		private boolean matchesClass(final CMCard input) {
			return isBlank(task.getTargetClassname()) || input.getType().getName().equals(task.getTargetClassname());
		}

		private boolean matchesCards(final CMCard input) {
			return (isBlank(task.getTargetClassname()) && isBlank(task.getFilter())) || matchesFilter(input);
		}

		private boolean matchesFilter(final CMCard input) {
			final String classname = task.getTargetClassname();
			final String filter = task.getFilter();
			try {
				final JSONObject jsonFilter = (filter == null) ? new JSONObject() : new JSONObject(filter);
				final QueryOptions queryOptions = QueryOptions.newQueryOption() //
						.filter(new JsonFilterHelper(jsonFilter) //
								.merge(CardIdFilterElementGetter.of(input))) //
						.build();
				final CMQueryResult result = new QuerySpecsBuilderFiller(dataView.get(), queryOptions, classname) //
						.create() //
						.run();
				return !isEmpty(result);
			} catch (final JSONException e) {
				final String message = format("malformed filter: '%s'", filter);
				logger.error(marker, message, e);
				return false;
			}
		}

	}

	private final UserStore userStore;
	private final FluentApi fluentApi;
	private final WorkflowLogic workflowLogic;
	private final Store<EmailAccount> emailAccountStore;
	private final EmailServiceFactory emailServiceFactory;
	private final EmailTemplateLogic emailTemplateLogic;
	private final CMDataView dataView;
	private final Supplier<CMDataView> privilegedDataView;

	public DefaultObserverFactory( //
			final UserStore userStore, //
			final FluentApi fluentApi, //
			final WorkflowLogic workflowLogic, //
			final Store<EmailAccount> emailAccountStore, //
			final EmailServiceFactory emailServiceFactory, //
			final EmailTemplateLogic emailTemplateLogic, //
			final CMDataView dataView, //
			final Supplier<CMDataView> privilegedDataView //
	) {
		this.userStore = userStore;
		this.fluentApi = fluentApi;
		this.workflowLogic = workflowLogic;
		this.emailAccountStore = emailAccountStore;
		this.emailServiceFactory = emailServiceFactory;
		this.emailTemplateLogic = emailTemplateLogic;
		this.dataView = dataView;
		this.privilegedDataView = privilegedDataView;
	}

	@Override
	public Observer create(final SynchronousEventTask task) {
		final Builder builder = DefaultObserver.newInstance();
		final DefaultObserver.Phase phase = phaseOf(task);
		if (task.isWorkflowEnabled()) {
			builder.add(workflowOf(task), phase);
		}
		if (task.isEmailEnabled()) {
			builder.add(emailOf(task), phase);
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

	private Command emailOf(final SynchronousEventTask task) {
		return safe(new Command() {

			@Override
			public void execute(final Context context) {
				final Supplier<EmailAccount> emailAccountSupplier = PredicateEmailAccountSupplier.of(emailAccountStore,
						named(task.getEmailAccount()));
				SendTemplateEmail.newInstance() //
						.withEmailAccountSupplier(emailAccountSupplier) //
						.withEmailServiceFactory(emailServiceFactory) //
						.withEmailTemplateSupplier(new Supplier<Template>() {

							@Override
							public Template get() {
								final String name = task.getEmailTemplate();
								return emailTemplateLogic.read(name);
							}

						}) //
						.withTemplateResolver(EngineBasedTemplateResolver.newInstance() //
								.withEngine(emptyStringOnNull(nullOnError( //
										UserEmailEngine.newInstance() //
												.withDataView(dataView) //
												.build())), //
										USER_PREFIX) //
								.withEngine(emptyStringOnNull(nullOnError( //
										GroupEmailEngine.newInstance() //
												.withDataView(dataView) //
												.build())), //
										GROUP_PREFIX) //
								.withEngine(emptyStringOnNull(nullOnError( //
										GroupUsersEmailEngine.newInstance() //
												.withDataView(dataView) //
												.withSeparator(EmailConstants.ADDRESSES_SEPARATOR) //
												.build() //
										)), //
										GROUP_USERS_PREFIX) //
								.build()) //
						.build() //
						.execute();
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
				.withDataView(privilegedDataView) //
				.build();
	}

}
