package org.cmdbuild.logic.email;

import static com.google.common.base.Predicates.equalTo;
import static com.google.common.collect.FluentIterable.from;

import java.util.Map;

import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.cmdbuild.data.store.Storable;
import org.cmdbuild.data.store.Store;
import org.cmdbuild.data.store.email.DefaultEmailTemplate;
import org.cmdbuild.data.store.email.DefaultExtendedEmailTemplate;
import org.cmdbuild.data.store.email.EmailTemplate;
import org.cmdbuild.data.store.email.ExtendedEmailTemplate;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import com.google.common.base.Function;

public class DefaultEmailTemplateLogic implements EmailTemplateLogic {

	private static final Marker marker = MarkerFactory.getMarker(DefaultEmailTemplateLogic.class.getName());

	private static class TemplateWrapper implements Template {

		private final ExtendedEmailTemplate delegate;

		public TemplateWrapper(final ExtendedEmailTemplate delegate) {
			this.delegate = delegate;
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
		public Long getAccount() {
			return delegate.getAccount();
		}

		@Override
		public String toString() {
			return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
		}

	}

	private static final Function<ExtendedEmailTemplate, Template> EMAIL_TEMPLATE_TO_TEMPLATE = new Function<ExtendedEmailTemplate, Template>() {

		@Override
		public Template apply(final ExtendedEmailTemplate input) {
			return new TemplateWrapper(input);
		};

	};

	private static final Function<Template, ExtendedEmailTemplate> TEMPLATE_TO_EMAIL_TEMPLATE = new Function<Template, ExtendedEmailTemplate>() {

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
							.withAccount(input.getAccount()) //
							.build()) //
					.withVariables(input.getVariables()) //
					.build();
		};

	};

	private static Function<? super ExtendedEmailTemplate, String> TO_NAME = new Function<ExtendedEmailTemplate, String>() {

		@Override
		public String apply(final ExtendedEmailTemplate input) {
			return input.getName();
		}

	};

	private final Store<ExtendedEmailTemplate> store;

	public DefaultEmailTemplateLogic(final Store<ExtendedEmailTemplate> store) {
		this.store = store;
	}

	@Override
	public Iterable<Template> readAll() {
		logger.info(marker, "reading all templates");
		return from(store.readAll()) //
				.transform(EMAIL_TEMPLATE_TO_TEMPLATE);
	}

	@Override
	public Template read(final String name) {
		logger.info(marker, "reading template '{}'", name);
		assureOneOnlyWithName(name);
		final ExtendedEmailTemplate template = DefaultExtendedEmailTemplate.newInstance() //
				.withDelegate(DefaultEmailTemplate.newInstance() //
						.withName(name) //
						.build()) //
				.build();
		final ExtendedEmailTemplate readed = store.read(template);
		return EMAIL_TEMPLATE_TO_TEMPLATE.apply(readed);
	}

	@Override
	public Long create(final Template template) {
		logger.info(marker, "creating template '{}'", template);
		assureNoOneWithName(template.getName());
		final Storable created = store.create(TEMPLATE_TO_EMAIL_TEMPLATE.apply(template));
		final EmailTemplate readed = store.read(created);
		return readed.getId();
	}

	@Override
	public void update(final Template template) {
		logger.info(marker, "updating template '{}'", template);
		assureOneOnlyWithName(template.getName());
		store.update(TEMPLATE_TO_EMAIL_TEMPLATE.apply(template));
	}

	@Override
	public void delete(final String name) {
		logger.info(marker, "deleting template '{}'", name);
		assureOneOnlyWithName(name);
		final EmailTemplate emailTemplate = DefaultEmailTemplate.newInstance() //
				.withName(name) //
				.build();
		store.delete(emailTemplate);
	}

	private void assureNoOneWithName(final String name) {
		final boolean existing = from(store.readAll()) //
				.transform(TO_NAME) //
				.contains(name);
		Validate.isTrue(!existing, "already existing element");
	}

	private void assureOneOnlyWithName(final String name) {
		final int count = from(store.readAll()) //
				.transform(TO_NAME) //
				.filter(equalTo(name)) //
				.size();
		Validate.isTrue(!(count == 0), "element not found");
		Validate.isTrue(!(count > 1), "multiple elements found");
	}

}
