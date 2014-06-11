package org.cmdbuild.logic.taskmanager.task.connector;

import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.apache.commons.lang3.StringUtils.defaultIfBlank;
import static org.cmdbuild.common.utils.BuilderUtils.a;
import static org.cmdbuild.scheduler.command.Commands.composeOnExeption;
import static org.cmdbuild.scheduler.command.Commands.conditional;
import static org.cmdbuild.services.email.Predicates.named;

import org.cmdbuild.common.java.sql.DataSourceHelper;
import org.cmdbuild.dao.view.CMDataView;
import org.cmdbuild.data.store.email.StorableEmailAccount;
import org.cmdbuild.logic.email.EmailTemplateLogic;
import org.cmdbuild.logic.email.EmailTemplateLogic.Template;
import org.cmdbuild.logic.email.SendTemplateEmail;
import org.cmdbuild.logic.taskmanager.commons.SchedulerCommandWrapper;
import org.cmdbuild.logic.taskmanager.scheduler.AbstractJobFactory;
import org.cmdbuild.scheduler.command.Command;
import org.cmdbuild.services.email.EmailAccount;
import org.cmdbuild.services.email.EmailServiceFactory;
import org.cmdbuild.services.email.PredicateEmailAccountSupplier;
import org.cmdbuild.services.sync.store.internal.AttributeValueAdapter;

import com.google.common.base.Supplier;

public class ConnectorTaskJobFactory extends AbstractJobFactory<ConnectorTask> {

	private final CMDataView dataView;
	private final DataSourceHelper jdbcService;
	private final AttributeValueAdapter attributeValueAdapter;
	private final org.cmdbuild.data.store.Store<StorableEmailAccount> emailAccountStore;
	private final EmailServiceFactory emailServiceFactory;
	private final EmailTemplateLogic emailTemplateLogic;

	public ConnectorTaskJobFactory(final CMDataView dataView, final DataSourceHelper jdbcService,
			final AttributeValueAdapter attributeValueAdapter,
			final org.cmdbuild.data.store.Store<StorableEmailAccount> emailAccountStore,
			final EmailServiceFactory emailServiceFactory, final EmailTemplateLogic emailTemplateLogic) {
		this.dataView = dataView;
		this.jdbcService = jdbcService;
		this.attributeValueAdapter = attributeValueAdapter;
		this.emailServiceFactory = emailServiceFactory;
		this.emailAccountStore = emailAccountStore;
		this.emailTemplateLogic = emailTemplateLogic;
	}

	@Override
	protected Class<ConnectorTask> getType() {
		return ConnectorTask.class;
	}

	@Override
	protected Command command(final ConnectorTask task) {
		return composeOnExeption(connector(task), sendEmail(task));

	}

	private ConnectorTaskCommandWrapper connector(final ConnectorTask task) {
		return new ConnectorTaskCommandWrapper(dataView, jdbcService, attributeValueAdapter, task);
	}

	private Command sendEmail(final ConnectorTask task) {
		final Supplier<EmailAccount> emailAccountSupplier = PredicateEmailAccountSupplier.of(emailAccountStore,
				named(task.getNotificationAccount()));
		final Supplier<Template> emailTemplateSupplier = new Supplier<Template>() {

			@Override
			public Template get() {
				final String name = defaultIfBlank(task.getNotificationErrorTemplate(), EMPTY);
				return emailTemplateLogic.read(name);
			}

		};
		final Command command = SchedulerCommandWrapper.of(a(SendTemplateEmail.newInstance() //
				.withEmailAccountSupplier(emailAccountSupplier) //
				.withEmailServiceFactory(emailServiceFactory) //
				.withEmailTemplateSupplier(emailTemplateSupplier)));
		return conditional(command, new NotificationEnabled(task));
	}

}
