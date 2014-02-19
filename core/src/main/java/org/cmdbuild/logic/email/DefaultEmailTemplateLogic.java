package org.cmdbuild.logic.email;

import static com.google.common.collect.FluentIterable.from;

import org.apache.commons.lang.Validate;
import org.cmdbuild.data.store.Store;
import org.cmdbuild.data.store.email.EmailTemplate;

import com.google.common.base.Function;

public class DefaultEmailTemplateLogic implements EmailTemplateLogic {

	private static class TemplateWrapper implements Template {

		private final EmailTemplate delegate;

		public TemplateWrapper(final EmailTemplate delegate) {
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

	}

	private static final Function<EmailTemplate, Template> EMAIL_TEMPLATE_TO_TEMPLATE = new Function<EmailTemplate, Template>() {

		@Override
		public Template apply(final EmailTemplate input) {
			return new TemplateWrapper(input);
		};

	};

	private static final Function<Template, EmailTemplate> TEMPLATE_TO_EMAIL_TEMPLATE = new Function<Template, EmailTemplate>() {

		@Override
		public EmailTemplate apply(final Template input) {
			return EmailTemplate.newInstance() //
					.withId(input.getId()) //
					.withName(input.getName()) //
					.withDescription(input.getDescription()) //
					.withTo(input.getTo()) //
					.withCc(input.getCc()) //
					.withBcc(input.getBcc()) //
					.withSubject(input.getSubject()) //
					.withBody(input.getBody()) //
					.build();
		};

	};

	private static Function<? super EmailTemplate, String> TO_NAME = new Function<EmailTemplate, String>() {

		@Override
		public String apply(final EmailTemplate input) {
			return input.getName();
		}

	};

	private final Store<EmailTemplate> store;

	public DefaultEmailTemplateLogic(final Store<EmailTemplate> store) {
		this.store = store;
	}

	@Override
	public Iterable<Template> readAll() {
		return from(store.list()) //
				.transform(EMAIL_TEMPLATE_TO_TEMPLATE);
	}

	@Override
	public Template read(String name) {
		final boolean existing = from(store.list()) //
				.transform(TO_NAME) //
				.contains(name);
		Validate.isTrue(existing, "element not existing");
		final EmailTemplate template = EmailTemplate.newInstance() //
				.withName(name) //
				.build();
		final EmailTemplate readed = store.read(template);
		return EMAIL_TEMPLATE_TO_TEMPLATE.apply(readed);
	}

	@Override
	public void create(final Template template) {
		final boolean existing = from(store.list()) //
				.transform(TO_NAME) //
				.contains(template.getName());
		Validate.isTrue(!existing, "already existing element");
		store.create(TEMPLATE_TO_EMAIL_TEMPLATE.apply(template));
	}

	@Override
	public void update(final Template template) {
		final int count = from(store.list()) //
				.transform(TO_NAME) //
				.size();
		Validate.isTrue(!(count == 0), "element not found");
		Validate.isTrue(!(count > 1), "multiple elements found");
		store.update(TEMPLATE_TO_EMAIL_TEMPLATE.apply(template));
	}

	@Override
	public void delete(final String name) {
		final int count = from(store.list()) //
				.transform(TO_NAME) //
				.size();
		Validate.isTrue(!(count == 0), "element not found");
		Validate.isTrue(!(count > 1), "multiple elements found");
		final EmailTemplate emailTemplate = EmailTemplate.newInstance() //
				.withName(name) //
				.build();
		store.delete(emailTemplate);
	}

}
