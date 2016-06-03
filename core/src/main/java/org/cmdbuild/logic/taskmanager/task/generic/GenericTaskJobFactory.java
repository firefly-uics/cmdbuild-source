package org.cmdbuild.logic.taskmanager.task.generic;

import static com.google.common.base.Suppliers.memoize;
import static com.google.common.collect.FluentIterable.from;
import static java.util.Arrays.asList;
import static org.cmdbuild.scheduler.command.Commands.conditional;

import org.cmdbuild.data.store.email.EmailAccount;
import org.cmdbuild.data.store.email.EmailAccountFacade;
import org.cmdbuild.logic.email.EmailTemplateLogic;
import org.cmdbuild.logic.email.EmailTemplateLogic.Template;
import org.cmdbuild.logic.email.EmailTemplateSenderFactory;
import org.cmdbuild.logic.taskmanager.scheduler.AbstractJobFactory;
import org.cmdbuild.scheduler.command.Command;

import com.google.common.base.Predicate;
import com.google.common.base.Supplier;

public class GenericTaskJobFactory extends AbstractJobFactory<GenericTask> {

	private final EmailAccountFacade emailAccountFacade;
	private final EmailTemplateLogic emailTemplateLogic;
	private final EmailTemplateSenderFactory emailTemplateSenderFactory;

	public GenericTaskJobFactory(final EmailAccountFacade emailAccountFacade,
			final EmailTemplateLogic emailTemplateLogic, final EmailTemplateSenderFactory emailTemplateSenderFactory) {
		this.emailAccountFacade = emailAccountFacade;
		this.emailTemplateLogic = emailTemplateLogic;
		this.emailTemplateSenderFactory = emailTemplateSenderFactory;
	}

	@Override
	protected Class<GenericTask> getType() {
		return GenericTask.class;
	}

	@Override
	protected Command command(final GenericTask task) {
		return conditional(sendEmail(task), emailActive(task));
	}

	private Command sendEmail(final GenericTask task) {
		return new Command() {

			@Override
			public void execute() {
				final Supplier<Template> emailTemplateSupplier = memoize(new Supplier<Template>() {

					@Override
					public Template get() {
						logger.debug(marker, "getting email template for '{}'", task);
						final String value = task.getEmailTemplate();
						logger.debug(marker, "template name is '{}'", value);
						return emailTemplateLogic.read(value);
					}

				});
				final Supplier<EmailAccount> account = new Supplier<EmailAccount>() {

					@Override
					public EmailAccount get() {
						logger.debug(marker, "getting email account for '{}'", task);
						final Iterable<String> eligibleAccounts = from(
								asList(emailTemplateSupplier.get().getAccount(), task.getEmailAccount()))
										.filter(String.class);
						logger.debug(marker, "eligible accounts are '{}'", eligibleAccounts);
						return emailAccountFacade.firstOfOrDefault(eligibleAccounts).get();
					}

				};
				emailTemplateSenderFactory.queued() //
						.withEmailAccountSupplier(account) //
						.withEmailTemplateSupplier(emailTemplateSupplier) //
						.withReference(task.getId()) //
						.build() //
						.execute();
			}

		};
	}

	private Predicate<Void> emailActive(final GenericTask task) {
		return new Predicate<Void>() {

			@Override
			public boolean apply(final Void input) {
				return task.isEmailActive();
			}

		};
	}

}
