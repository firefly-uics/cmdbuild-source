package org.cmdbuild.servlets.json.email;

import static com.google.common.collect.FluentIterable.from;
import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;
import static org.cmdbuild.servlets.json.CommunicationConstants.BCC;
import static org.cmdbuild.servlets.json.CommunicationConstants.BODY;
import static org.cmdbuild.servlets.json.CommunicationConstants.CC;
import static org.cmdbuild.servlets.json.CommunicationConstants.DEFAULT_ACCOUNT;
import static org.cmdbuild.servlets.json.CommunicationConstants.DESCRIPTION;
import static org.cmdbuild.servlets.json.CommunicationConstants.ELEMENTS;
import static org.cmdbuild.servlets.json.CommunicationConstants.ID;
import static org.cmdbuild.servlets.json.CommunicationConstants.NAME;
import static org.cmdbuild.servlets.json.CommunicationConstants.SUBJECT;
import static org.cmdbuild.servlets.json.CommunicationConstants.TEMPORARY;
import static org.cmdbuild.servlets.json.CommunicationConstants.TO;
import static org.cmdbuild.servlets.json.CommunicationConstants.VARIABLES;
import static org.cmdbuild.servlets.json.schema.Utils.toMap;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.cmdbuild.logic.email.EmailTemplateLogic;
import org.cmdbuild.services.json.dto.JsonResponse;
import org.cmdbuild.servlets.json.JSONBaseWithSpringContext;
import org.cmdbuild.servlets.utils.Parameter;
import org.codehaus.jackson.annotate.JsonProperty;
import org.json.JSONObject;

import com.google.common.base.Function;
import com.google.common.collect.Lists;

public class Template extends JSONBaseWithSpringContext {

	private static class JsonTemplate implements EmailTemplateLogic.Template {

		private static final Map<String, String> NO_VARIABLES = Collections.emptyMap();

		private Long id;
		private String name;
		private String description;
		private String from;
		private String to;
		private String cc;
		private String bcc;
		private String subject;
		private String body;
		private Map<String, String> variables;
		private String account;
		private boolean temporary;

		@Override
		@JsonProperty(ID)
		public Long getId() {
			return id;
		}

		public void setId(final Long id) {
			this.id = id;
		}

		@Override
		@JsonProperty(NAME)
		public String getName() {
			return name;
		}

		public void setName(final String name) {
			this.name = name;
		}

		@Override
		@JsonProperty(DESCRIPTION)
		public String getDescription() {
			return description;
		}

		public void setDescription(final String description) {
			this.description = description;
		}

		@Override
		public String getFrom() {
			return from;
		}

		public void setFrom(final String from) {
			this.from = from;
		}

		@Override
		@JsonProperty(TO)
		public String getTo() {
			return to;
		}

		public void setTo(final String to) {
			this.to = to;
		}

		@Override
		@JsonProperty(CC)
		public String getCc() {
			return cc;
		}

		public void setCc(final String cc) {
			this.cc = cc;
		}

		@Override
		@JsonProperty(BCC)
		public String getBcc() {
			return bcc;
		}

		public void setBcc(final String bcc) {
			this.bcc = bcc;
		}

		@Override
		@JsonProperty(SUBJECT)
		public String getSubject() {
			return subject;
		}

		public void setSubject(final String subject) {
			this.subject = subject;
		}

		@Override
		@JsonProperty(BODY)
		public String getBody() {
			return body;
		}

		public void setBody(final String body) {
			this.body = body;
		}

		@Override
		@JsonProperty(VARIABLES)
		public Map<String, String> getVariables() {
			return defaultIfNull(variables, NO_VARIABLES);
		}

		public void setVariables(final Map<String, String> variables) {
			this.variables = variables;
		}

		@Override
		@JsonProperty(DEFAULT_ACCOUNT)
		public String getAccount() {
			return account;
		}

		public void setAccount(final String account) {
			this.account = account;
		}

		@Override
		@JsonProperty(TEMPORARY)
		public boolean isTemporary() {
			return temporary;
		}

		public void setTemporary(final boolean temporary) {
			this.temporary = temporary;
		}

		@Override
		public String toString() {
			return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
		}

	}

	private static class JsonTemplates {

		private List<? super JsonTemplate> elements;

		@JsonProperty(ELEMENTS)
		public List<? super JsonTemplate> getElements() {
			return elements;
		}

		public void setElements(final Iterable<? extends JsonTemplate> elements) {
			this.elements = Lists.newArrayList(elements);
		}

		@Override
		public String toString() {
			return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
		}

	}

	private static Function<EmailTemplateLogic.Template, JsonTemplate> TEMPLATE_TO_JSON_TEMPLATE = new Function<EmailTemplateLogic.Template, JsonTemplate>() {

		@Override
		public JsonTemplate apply(final EmailTemplateLogic.Template input) {
			final JsonTemplate template = new JsonTemplate();
			template.setId(input.getId());
			template.setName(input.getName());
			template.setDescription(input.getDescription());
			template.setFrom(input.getFrom());
			template.setTo(input.getTo());
			template.setCc(input.getCc());
			template.setBcc(input.getBcc());
			template.setSubject(input.getSubject());
			template.setBody(input.getBody());
			template.setVariables(input.getVariables());
			template.setAccount(input.getAccount());
			return template;
		}

	};

	@JSONExported
	@Admin
	public JsonResponse create( //
			@Parameter(NAME) final String name, //
			@Parameter(DESCRIPTION) final String description, //
			@Parameter(TO) final String to, //
			@Parameter(CC) final String cc, //
			@Parameter(BCC) final String bcc, //
			@Parameter(SUBJECT) final String subject, //
			@Parameter(BODY) final String body, //
			@Parameter(value = VARIABLES, required = false) final JSONObject jsonVariables, //
			@Parameter(value = DEFAULT_ACCOUNT, required = false) final String accountName, //
			@Parameter(value = TEMPORARY, required = false) final boolean temporary //
	) {
		final JsonTemplate template = new JsonTemplate();
		template.setName(name);
		template.setDescription(description);
		template.setTo(to);
		template.setCc(cc);
		template.setBcc(bcc);
		template.setSubject(subject);
		template.setBody(body);
		template.setVariables(toMap(jsonVariables));
		template.setAccount(accountName);
		template.setTemporary(temporary);
		final Long id = emailTemplateLogic().create(template);
		return JsonResponse.success(id);
	}

	@JSONExported
	public JsonResponse readAll() {
		final Iterable<EmailTemplateLogic.Template> elements = emailTemplateLogic().readAll();
		final JsonTemplates templates = new JsonTemplates();
		templates.setElements(from(elements) //
				.transform(TEMPLATE_TO_JSON_TEMPLATE));
		return JsonResponse.success(templates);
	}

	@JSONExported
	public JsonResponse read( //
			@Parameter(NAME) final String name //
	) {
		final EmailTemplateLogic.Template element = emailTemplateLogic().read(name);
		return JsonResponse.success(TEMPLATE_TO_JSON_TEMPLATE.apply(element));
	}

	@JSONExported
	@Admin
	public void update( //
			@Parameter(NAME) final String name, //
			@Parameter(DESCRIPTION) final String description, //
			@Parameter(TO) final String to, //
			@Parameter(CC) final String cc, //
			@Parameter(BCC) final String bcc, //
			@Parameter(SUBJECT) final String subject, //
			@Parameter(BODY) final String body, //
			@Parameter(value = VARIABLES, required = false) final JSONObject jsonVariables, //
			@Parameter(value = DEFAULT_ACCOUNT, required = false) final String accountName, //
			@Parameter(value = TEMPORARY, required = false) final boolean temporary //
	) {
		final JsonTemplate template = new JsonTemplate();
		template.setName(name);
		template.setDescription(description);
		template.setTo(to);
		template.setCc(cc);
		template.setBcc(bcc);
		template.setSubject(subject);
		template.setBody(body);
		template.setVariables(toMap(jsonVariables));
		template.setAccount(accountName);
		template.setTemporary(temporary);
		emailTemplateLogic().update(template);
	}

	@JSONExported
	@Admin
	public void delete( //
			@Parameter(NAME) final String name //
	) {
		final JsonTemplate template = new JsonTemplate();
		template.setName(name);
		emailTemplateLogic().delete(template);
	}

}
