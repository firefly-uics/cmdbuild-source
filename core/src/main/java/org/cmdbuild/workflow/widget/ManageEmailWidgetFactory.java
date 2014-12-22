package org.cmdbuild.workflow.widget;

import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.apache.commons.lang3.StringUtils.defaultIfEmpty;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

import java.util.Map;
import java.util.Set;

import org.cmdbuild.logger.Log;
import org.cmdbuild.logic.email.EmailLogic;
import org.cmdbuild.logic.email.EmailTemplateLogic;
import org.cmdbuild.model.widget.ManageEmail;
import org.cmdbuild.model.widget.ManageEmail.EmailTemplate;
import org.cmdbuild.model.widget.Widget;
import org.cmdbuild.notification.Notifier;
import org.cmdbuild.services.template.store.TemplateRepository;
import org.slf4j.Logger;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

public class ManageEmailWidgetFactory extends ValuePairWidgetFactory {

	// TODO change logger
	private static final Logger logger = Log.WORKFLOW;
	private static final Marker marker = MarkerFactory.getMarker(ManageEmailWidgetFactory.class.getName());

	private static final String IMPLICIT_TEMPLATE_NAME = "implicitTemplateName";
	private final static String FROM_ADDRESS = "FromAddress";
	private final static String TO_ADDRESSES = "ToAddresses";
	private final static String CC_ADDRESSES = "CCAddresses";
	private final static String SUBJECT = "Subject";
	private final static String CONTENT = "Content";
	private final static String CONDITION = "Condition";
	private final static String READ_ONLY = "ReadOnly";
	private final static String NOTIFY_TEMPLATE_NAME = "NotifyWith";
	private final static String TEMPLATE = "Template";
	private final static String NO_SUBJECT_PREFIX = "NoSubjectPrefix";
	private final static String GLOBAL_NO_SUBJECT_PREFIX = "GlobalNoSubjectPrefix";

	private final static String WIDGET_NAME = "manageEmail";

	private final EmailLogic emailLogic;
	private final EmailTemplateLogic emailTemplateLogic;

	public ManageEmailWidgetFactory(final TemplateRepository templateRespository, final Notifier notifier,
			final EmailLogic emailLogic, final EmailTemplateLogic emailTemplateLogic) {
		super(templateRespository, notifier);
		this.emailLogic = emailLogic;
		this.emailTemplateLogic = emailTemplateLogic;
	}

	@Override
	public String getWidgetName() {
		return WIDGET_NAME;
	}

	/*
	 * naive but fast to write solution ...first do it works...
	 */
	@Override
	protected Widget createWidget(final Map<String, Object> valueMap) {
		// I want to preserve the order
		final Map<String, EmailTemplate> emailTemplatesByName = Maps.newLinkedHashMap();
		final Set<String> managedParameters = Sets.newHashSet();
		managedParameters.add(READ_ONLY);
		managedParameters.add(BUTTON_LABEL);
		managedParameters.add(GLOBAL_NO_SUBJECT_PREFIX);

		final Map<String, String> templates = getAttributesStartingWith(valueMap, TEMPLATE);
		for (final String key : templates.keySet()) {
			final EmailTemplate template = getTemplateForKey(emailTemplatesByName, key, TEMPLATE);
			final String name = readString(valueMap.get(key));
			if (isNotBlank(name)) {
				try {
					final EmailTemplateLogic.Template _template = emailTemplateLogic.read(name);
					template.setFromAddress(_template.getFrom());
					template.setToAddresses(_template.getTo());
					template.setCcAddresses(_template.getCc());
					template.setSubject(_template.getSubject());
					template.setContent(_template.getBody());
					template.setVariables(_template.getVariables());
					template.setAccount(_template.getAccount());
				} catch (final Exception e) {
					logger.warn(marker, "error getting template, skipping", e);
				}
			}
		}
		managedParameters.addAll(templates.keySet());

		final Map<String, String> fromAddresses = getAttributesStartingWith(valueMap, FROM_ADDRESS);
		for (final String key : fromAddresses.keySet()) {
			final EmailTemplate template = getTemplateForKey(emailTemplatesByName, key, FROM_ADDRESS);
			template.setFromAddress(readString(valueMap.get(key)));
		}
		managedParameters.addAll(fromAddresses.keySet());

		final Map<String, String> toAddresses = getAttributesStartingWith(valueMap, TO_ADDRESSES);
		for (final String key : toAddresses.keySet()) {
			final EmailTemplate template = getTemplateForKey(emailTemplatesByName, key, TO_ADDRESSES);
			template.setToAddresses(readString(valueMap.get(key)));
		}
		managedParameters.addAll(toAddresses.keySet());

		final Map<String, String> ccAddresses = getAttributesStartingWith(valueMap, CC_ADDRESSES);
		for (final String key : ccAddresses.keySet()) {
			final EmailTemplate template = getTemplateForKey(emailTemplatesByName, key, CC_ADDRESSES);
			template.setCcAddresses(readString(valueMap.get(key)));
		}
		managedParameters.addAll(ccAddresses.keySet());

		final Map<String, String> subjects = getAttributesStartingWith(valueMap, SUBJECT);
		for (final String key : subjects.keySet()) {
			final EmailTemplate template = getTemplateForKey(emailTemplatesByName, key, SUBJECT);
			template.setSubject(readString(valueMap.get(key)));
		}
		managedParameters.addAll(subjects.keySet());

		final Map<String, String> notifyWithThemplate = getAttributesStartingWith(valueMap, NOTIFY_TEMPLATE_NAME);
		for (final String key : notifyWithThemplate.keySet()) {
			final EmailTemplate template = getTemplateForKey(emailTemplatesByName, key, NOTIFY_TEMPLATE_NAME);
			template.setNotifyWith(readString(valueMap.get(key)));
		}
		managedParameters.addAll(subjects.keySet());

		final Map<String, String> contents = getAttributesStartingWith(valueMap, CONTENT);
		for (final String key : contents.keySet()) {
			final EmailTemplate template = getTemplateForKey(emailTemplatesByName, key, CONTENT);
			template.setContent(readString(valueMap.get(key)));
		}
		managedParameters.addAll(contents.keySet());

		final Map<String, String> conditions = getAttributesStartingWith(valueMap, CONDITION);
		for (final String key : conditions.keySet()) {
			final EmailTemplate template = getTemplateForKey(emailTemplatesByName, key, CONDITION);
			template.setCondition(readString(valueMap.get(key)));
		}
		managedParameters.addAll(conditions.keySet());

		final Map<String, String> noSubjectPrexifes = getAttributesStartingWith(valueMap, NO_SUBJECT_PREFIX);
		for (final String key : noSubjectPrexifes.keySet()) {
			final EmailTemplate template = getTemplateForKey(emailTemplatesByName, key, NO_SUBJECT_PREFIX);
			template.setNoSubjectPrefix(readBooleanTrueIfTrue(valueMap.get(key)));
		}
		managedParameters.addAll(noSubjectPrexifes.keySet());

		final ManageEmail widget = new ManageEmail(emailLogic);
		widget.setEmailTemplates(emailTemplatesByName.values());
		widget.setTemplates(extractUnmanagedStringParameters(valueMap, managedParameters));
		widget.setReadOnly(readBooleanTrueIfPresent(valueMap.get(READ_ONLY)));
		widget.setNoSubjectPrefix(readBooleanTrueIfTrue(valueMap.get(GLOBAL_NO_SUBJECT_PREFIX)));

		return widget;
	}

	private Map<String, String> getAttributesStartingWith(final Map<String, Object> valueMap, final String prefix) {
		final Map<String, String> out = Maps.newHashMap();
		for (final String key : valueMap.keySet()) {
			if (key.startsWith(prefix)) {
				out.put(key, readString(valueMap.get(key)));
			}
		}
		return out;
	}

	private EmailTemplate getTemplateForKey(final Map<String, EmailTemplate> templates, final String key,
			final String prefix) {
		final String id = defaultIfEmpty(key.replaceFirst(prefix, EMPTY), IMPLICIT_TEMPLATE_NAME);
		if (templates.containsKey(id)) {
			return templates.get(id);
		} else {
			final EmailTemplate template = new EmailTemplate();
			templates.put(id, template);
			return template;
		}
	}
}
