package org.cmdbuild.services.email;

import static org.apache.commons.lang.StringUtils.defaultString;
import static org.apache.commons.lang.StringUtils.isNotBlank;
import static org.apache.commons.lang.StringUtils.join;
import static org.cmdbuild.dao.query.clause.AnyAttribute.anyAttribute;
import static org.cmdbuild.dao.query.clause.QueryAliasAttribute.attribute;
import static org.cmdbuild.dao.query.clause.join.Over.over;
import static org.cmdbuild.dao.query.clause.where.EqualsOperatorAndValue.eq;
import static org.cmdbuild.dao.query.clause.where.SimpleWhereClause.condition;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.cmdbuild.dao.entry.CMCard;
import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.dao.entrytype.CMDomain;
import org.cmdbuild.dao.query.CMQueryRow;
import org.cmdbuild.dao.view.CMDataView;
import org.cmdbuild.logger.Log;
import org.cmdbuild.model.email.Email;
import org.slf4j.Logger;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import com.google.common.collect.Lists;

public class DefaultEmailTemplateResolver extends EmailTemplateResolver {

	private static final Logger logger = Log.EMAIL;

	public static class DefaultDataFacade implements DataFacade {

		private static final Marker marker = MarkerFactory.getMarker(DefaultDataFacade.class.getName());

		private final CMDataView dataView;

		public DefaultDataFacade(final CMDataView dataView) {
			this.dataView = dataView;
		}

		@Override
		public String getEmailForUser(final String user) {
			logger.info(marker, "getting all email addresses for user '{}'", user);
			// TODO externalize strings
			final CMClass userClass = dataView.findClass("User");
			Validate.notNull(userClass, "user class not visible");
			final CMCard card = dataView.select(anyAttribute(userClass)) //
					.from(userClass) //
					.where(condition(attribute(userClass, "Username"), eq(user))) //
					.run() //
					.getOnlyRow() //
					.getCard(userClass);
			final String email = card.get("Email", String.class);
			return defaultString(email);
		}

		@Override
		public String getEmailForGroup(final String group) {
			logger.info("getting all email addresses for group '{}'", group);
			// TODO externalize strings
			final CMClass roleClass = dataView.findClass("Role");
			Validate.notNull(roleClass, "role class not visible");
			final CMCard card = dataView.select(anyAttribute(roleClass)) //
					.from(roleClass) //
					.where(condition(attribute(roleClass, "Code"), eq(group))) //
					.run() //
					.getOnlyRow() //
					.getCard(roleClass);
			final String email = card.get("Email", String.class);
			return defaultString(email);
		}

		@Override
		public Iterable<String> getEmailsForGroupUsers(final String group) {
			logger.info("getting all email addresses for users of group '{}'", group);
			// TODO externalize strings
			final List<String> emails = Lists.newArrayList();
			final CMClass userClass = dataView.findClass("User");
			Validate.notNull(userClass, "user class not visible");
			final CMClass roleClass = dataView.findClass("Role");
			Validate.notNull(roleClass, "role class not visible");
			final CMDomain userRoleDomain = dataView.findDomain("UserRole");
			Validate.notNull(userRoleDomain, "user-role domain not visible");
			final Iterable<CMQueryRow> rows = dataView.select(anyAttribute(roleClass), attribute(userClass, "Email")) //
					.from(roleClass) //
					.join(userClass, over(userRoleDomain)) //
					.where(condition(attribute(roleClass, "Code"), eq(group))) //
					.run();
			for (final CMQueryRow row : rows) {
				final CMCard card = row.getCard(userClass);
				final String email = card.get("Email", String.class);
				if (isNotBlank(email)) {
					emails.add(email);
				}
			}
			return emails;
		}

		@Override
		public String getAttributeValue(final String attribute) {
			throw new UnsupportedOperationException();
		}

		@Override
		public String getReferenceAttributeValue(final String attribute, final String subAttribute) {
			throw new UnsupportedOperationException();
		}

	}

	private static final Pattern TEMPLATE_USER_EMAIL = Pattern.compile("(\\{user:(\\w+)\\})");
	private static final Pattern TEMPLATE_GROUP_EMAIL = Pattern.compile("(\\{group:(\\w+)\\})");
	private static final Pattern TEMPLATE_GROUP_USERS_EMAIL = Pattern.compile("(\\{groupUsers:(\\w+)\\})");
	private static final Pattern TEMPLATE_ATTRIBUTE = Pattern.compile("(\\{attribute:(\\w+)(:(\\w+))*\\})");
	private static final Pattern EMAIL_ATTRIBUTE = Pattern.compile("(\\{email:(\\w+)(:(\\w+))*\\})");
	
	private enum EmailValues {
		subject,
		content,
		to,
		from,
		cc,
		bcc,
		date;
	}

	public DefaultEmailTemplateResolver(final Configuration configuration) {
		super(configuration);
	}

	@Override
	public String resolve(final String template, final Email email) {
		logger.debug("resolving '{}'", template);

		String resolved = template;

		Matcher matcher = TEMPLATE_USER_EMAIL.matcher(resolved);
		if (matcher.find()) {
			final String user = matcher.group(2);
			final String replacement = dataFacade().getEmailForUser(user);
			resolved = matcher.replaceAll(replacement);
		}

		matcher = TEMPLATE_GROUP_EMAIL.matcher(resolved);
		if (matcher.find()) {
			final String group = matcher.group(2);
			final String replacement = dataFacade().getEmailForGroup(group);
			resolved = matcher.replaceAll(replacement);
		}

		matcher = TEMPLATE_GROUP_USERS_EMAIL.matcher(resolved);
		if (matcher.find()) {
			final String group = matcher.group(2);
			final Iterable<String> replacements = dataFacade().getEmailsForGroupUsers(group);
			final String replacement = join(replacements.iterator(), defaultString(multiSeparator()));
			resolved = matcher.replaceAll(replacement);
		}

		matcher = TEMPLATE_ATTRIBUTE.matcher(resolved);
		while (matcher.find()) {
			final String attribute = matcher.group(2);
			final String subAttribute = (matcher.groupCount() == 4) ? matcher.group(4) : null;
			String replacement;
			if (subAttribute == null) {
				replacement = dataFacade().getAttributeValue(attribute);
			} else {
				replacement = dataFacade().getReferenceAttributeValue(attribute, subAttribute);
			}
			resolved = matcher.replaceFirst(replacement);
			matcher = TEMPLATE_ATTRIBUTE.matcher(resolved);
		}
		
		matcher = EMAIL_ATTRIBUTE.matcher(resolved);
		while (matcher.find()) {
			final String value = matcher.group(2);
			String replacement;
			
			switch (EmailValues.valueOf(value)) {
			case content:
				replacement = email.getContent();
				break;
			case to:
				replacement = email.getToAddresses();
				break;
			case subject:
				replacement = email.getSubject();
				break;
			case from:
				replacement = email.getFromAddress();
				break;
			case cc:
				replacement = email.getCcAddresses();
				break;
			case bcc:
				replacement = email.getBccAddresses();
				break;
			case date:
				replacement = email.getDate().toString();
				break;
			default:
				replacement = StringUtils.EMPTY;
				break;
			}
			
			resolved = matcher.replaceFirst(replacement);
			matcher = EMAIL_ATTRIBUTE.matcher(resolved);
		}
		return resolved;
	}

}
