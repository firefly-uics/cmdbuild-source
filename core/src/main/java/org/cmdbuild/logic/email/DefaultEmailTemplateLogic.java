package org.cmdbuild.logic.email;

import static com.google.common.base.Predicates.equalTo;
import static com.google.common.collect.FluentIterable.from;
import static com.google.common.collect.Iterables.concat;
import static java.util.Arrays.asList;
import static org.cmdbuild.data.store.Storables.storableOf;
import static org.cmdbuild.data.store.Stores.nullOnNotFoundRead;

import java.util.Map;
import java.util.NoSuchElementException;
import java.util.UUID;

import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.cmdbuild.data.store.InMemoryStore;
import org.cmdbuild.data.store.Storable;
import org.cmdbuild.data.store.Store;
import org.cmdbuild.data.store.email.DefaultEmailTemplate;
import org.cmdbuild.data.store.email.DefaultExtendedEmailTemplate;
import org.cmdbuild.data.store.email.EmailTemplate;
import org.cmdbuild.data.store.email.ExtendedEmailTemplate;
import org.cmdbuild.services.email.EmailAccount;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.collect.ForwardingObject;

public class DefaultEmailTemplateLogic implements EmailTemplateLogic {

	private static final Marker marker = MarkerFactory.getMarker(DefaultEmailTemplateLogic.class.getName());

	private static abstract class ForwardingTemplate extends ForwardingObject implements Template {

		@Override
		protected abstract Template delegate();

		@Override
		public Long getId() {
			return delegate().getId();
		}

		@Override
		public String getName() {
			return delegate().getName();
		}

		@Override
		public String getDescription() {
			return delegate().getDescription();
		}

		@Override
		public String getFrom() {
			return delegate().getFrom();
		}

		@Override
		public String getTo() {
			return delegate().getFrom();
		}

		@Override
		public String getCc() {
			return delegate().getCc();
		}

		@Override
		public String getBcc() {
			return delegate().getBcc();
		}

		@Override
		public String getSubject() {
			return delegate().getSubject();
		}

		@Override
		public String getBody() {
			return delegate().getBody();
		}

		@Override
		public Map<String, String> getVariables() {
			return delegate().getVariables();
		}

		@Override
		public String getAccount() {
			return delegate().getAccount();
		}

		@Override
		public boolean isTemporary() {
			return delegate().isTemporary();
		}

	}

	private static class TemplateWrapper implements Template {

		private final ExtendedEmailTemplate delegate;
		private final Store<EmailAccount> store;

		public TemplateWrapper(final ExtendedEmailTemplate delegate, final Store<EmailAccount> store) {
			this.delegate = delegate;
			this.store = store;
		}

		@Override
		public Long getId() {
			return delegate.getId();
		}

		@Override
		public String getName() {
			return delegate.getName();
		}

		@Override
		public String getDescription() {
			return delegate.getDescription();
		}

		@Override
		public String getFrom() {
			return delegate.getFrom();
		}

		@Override
		public String getTo() {
			return delegate.getTo();
		}

		@Override
		public String getCc() {
			return delegate.getCc();
		}

		@Override
		public String getBcc() {
			return delegate.getBcc();
		}

		@Override
		public String getSubject() {
			return delegate.getSubject();
		}

		@Override
		public String getBody() {
			return delegate.getBody();
		}

		@Override
		public Map<String, String> getVariables() {
			return delegate.getVariables();
		}

		@Override
		public String getAccount() {
			return accountOf(delegate);
		}

		private String accountOf(final ExtendedEmailTemplate input) {
			if (input.getAccount() != null) {
				for (final EmailAccount account : store.readAll()) {
					if (input.getAccount().equals(account.getId())) {
						return account.getName();
					}
				}
			}
			return null;
		};

		@Override
		public boolean isTemporary() {
			return false;
		}

		@Override
		public String toString() {
			return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
		}

	}

	private static class EmailTemplate_to_Template implements Function<ExtendedEmailTemplate, Template> {

		private final Store<EmailAccount> store;

		public EmailTemplate_to_Template(final Store<EmailAccount> store) {
			this.store = store;
		}

		@Override
		public Template apply(final ExtendedEmailTemplate input) {
			return new TemplateWrapper(input, store);
		};

	};

	private static class Template_To_EmailTemplate implements Function<Template, ExtendedEmailTemplate> {

		private final Store<EmailAccount> nullOnNotFoundStore;

		public Template_To_EmailTemplate(final Store<EmailAccount> store) {
			this.nullOnNotFoundStore = nullOnNotFoundRead(store);
		}

		@Override
		public ExtendedEmailTemplate apply(final Template input) {
			return DefaultExtendedEmailTemplate.newInstance() //
					.withDelegate(DefaultEmailTemplate.newInstance() //
							.withId(input.getId()) //
							.withName(input.getName()) //
							.withDescription(input.getDescription()) //
							.withTo(input.getTo()) //
							.withCc(input.getCc()) //
							.withBcc(input.getBcc()) //
							.withSubject(input.getSubject()) //
							.withBody(input.getBody()) //
							.withAccount(accountIdOf(input)) //
							.build()) //
					.withVariables(input.getVariables()) //
					.build();
		}

		private Long accountIdOf(final Template input) {
			final EmailAccount account = nullOnNotFoundStore.read(storableOf(input.getAccount()));
			return (account == null) ? null : account.getId();
		};

	};

	private static Function<? super ExtendedEmailTemplate, String> TO_NAME = new Function<ExtendedEmailTemplate, String>() {

		@Override
		public String apply(final ExtendedEmailTemplate input) {
			return input.getName();
		}

	};

	private final Store<ExtendedEmailTemplate> store;
	private final Store<ExtendedEmailTemplate> temporaryStore;
	private final EmailTemplate_to_Template emailTemplate_to_Template;
	private final Template_To_EmailTemplate template_To_EmailTemplate;

	public DefaultEmailTemplateLogic(final Store<ExtendedEmailTemplate> store,
			final Store<ExtendedEmailTemplate> temporaryStore, final Store<EmailAccount> accountStore) {
		this.store = store;
		this.temporaryStore = temporaryStore;
		this.emailTemplate_to_Template = new EmailTemplate_to_Template(accountStore);
		this.template_To_EmailTemplate = new Template_To_EmailTemplate(accountStore);
	}

	@Override
	public Long create(final Template template) {
		logger.info(marker, "creating template '{}'", template);
		final ExtendedEmailTemplate emailTemplate = template_To_EmailTemplate.apply(new ForwardingTemplate() {

			@Override
			protected Template delegate() {
				return template;
			}

			@Override
			public Long getId() {
				return isTemporary() ? generateId() : super.getId();
			}

			private Long generateId() {
				return Long.valueOf(UUID.randomUUID().hashCode());
			}

		});
		if (!template.isTemporary()) {
			assureNoOneWithName(template.getName());
		}
		final Store<ExtendedEmailTemplate> _store = template.isTemporary() ? temporaryStore : store;
		final Storable created = _store.create(emailTemplate);
		final EmailTemplate read = _store.read(created);
		return read.getId();
	}

	@Override
	public Iterable<Template> readAll() {
		logger.info(marker, "reading all templates");
		return from(concat(//
				store.readAll(), //
				temporaryStore.readAll() //
				)) //
				.transform(emailTemplate_to_Template);
	}

	@Override
	public Template read(final String name) {
		logger.info(marker, "reading template '{}'", name);
		final ExtendedEmailTemplate template = DefaultExtendedEmailTemplate.newInstance() //
				.withDelegate(DefaultEmailTemplate.newInstance() //
						.withName(name) //
						.build()) //
				.build();
		final Optional<ExtendedEmailTemplate> found = from(asList( //
				nullOnNotFoundRead(store).read(template), //
				nullOnNotFoundRead(temporaryStore).read(template) //
				)) //
				.filter(ExtendedEmailTemplate.class) //
				.first();
		if (!found.isPresent()) {
			throw new NoSuchElementException(name);
		}
		return emailTemplate_to_Template.apply(found.get());
	}

	@Override
	public void update(final Template template) {
		logger.info(marker, "updating template '{}'", template);
		if (!template.isTemporary()) {
			assureOneOnlyWithName(template.getName());
		}
		final Store<ExtendedEmailTemplate> _store = template.isTemporary() ? temporaryStore : store;
		_store.update(template_To_EmailTemplate.apply(template));
	}

	@Override
	public void delete(final Template template) {
		logger.info(marker, "deleting template '{}'", template);
		if (!template.isTemporary()) {
			assureOneOnlyWithName(template.getName());
		}
		final Store<ExtendedEmailTemplate> _store = template.isTemporary() ? temporaryStore : store;
		_store.delete(template_To_EmailTemplate.apply(template));
	}

	private void assureNoOneWithName(final String name) {
		final boolean existing = from(store.readAll()) //
				.transform(TO_NAME) //
				.contains(name);
		Validate.isTrue(!existing, "already existing element for name '%s'", name);
	}

	private void assureOneOnlyWithName(final String name) {
		final int count = from(store.readAll()) //
				.transform(TO_NAME) //
				.filter(equalTo(name)) //
				.size();
		Validate.isTrue(!(count == 0), "element not found for name '%s'", name);
		Validate.isTrue(!(count > 1), "multiple elements found for name '%s'", name);
	}

}
