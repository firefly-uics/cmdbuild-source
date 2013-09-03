package org.cmdbuild.services.email;

import static org.apache.commons.lang.StringUtils.isBlank;
import static org.cmdbuild.dao.query.clause.AnyAttribute.anyAttribute;
import static org.cmdbuild.dao.query.clause.QueryAliasAttribute.attribute;
import static org.cmdbuild.dao.query.clause.join.Over.over;
import static org.cmdbuild.dao.query.clause.where.EqualsOperatorAndValue.eq;
import static org.cmdbuild.dao.query.clause.where.SimpleWhereClause.condition;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
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
import org.slf4j.Logger;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

/**
 * Resolves all recipients according with the following rules:<br>
 * <br>
 * <ul>
 * <li>"{@code [user] foo}": all the e-mail addresses of the user {@code foo}</li>
 * <li>"{@code [group] foo}": all the e-mail addresses of the group {@code foo}</li>
 * <li>"{@code [groupUsers] foo}": all the e-mail addresses of the users of the
 * group {@code foo}</li>
 * <li>the template as is otherwise</li>
 * </ul>
 */
public class EmailRecipientTemplateResolver {

	private static final Logger logger = Log.EMAIL;

	private static final Pattern RECIPIENT_TEMPLATE_USER = Pattern.compile("\\[user\\]\\s*(\\w+)");
	private static final Pattern RECIPIENT_TEMPLATE_GROUP = Pattern.compile("\\[group\\]\\s*(\\w+)");
	private static final Pattern RECIPIENT_TEMPLATE_GROUP_USERS = Pattern.compile("\\[groupUsers\\]\\s*(\\w+)");

	private final CMDataView dataView;

	public EmailRecipientTemplateResolver(final CMDataView dataView) {
		this.dataView = dataView;
	}

	/**
	 * Resolves the specified templates.
	 * 
	 * @param templates
	 * 
	 * @return all the results of the resolution.
	 */
	public Iterable<String> resolve(final Iterable<String> templates) {
		logger.info("resolving '{}'", Iterables.toString(templates));
		final Set<String> resolved = Sets.newHashSet();
		for (final String template : templates) {
			Iterables.addAll(resolved, resolve(template));
		}
		return resolved;
	}

	/**
	 * Resolves the specified template.
	 * 
	 * @param template
	 * 
	 * @return all the results of the resolution.
	 */
	public Iterable<String> resolve(final String template) {
		logger.debug("resolving '{}'", template);
		final Set<String> resolved = Sets.newHashSet();
		do {
			if (isBlank(template)) {
				break;
			}

			Matcher matcher = RECIPIENT_TEMPLATE_USER.matcher(template);
			if (matcher.find()) {
				logger.debug("resolving '{}' as an user", template);
				final String user = matcher.group(1);
				Iterables.addAll(resolved, getEmailsForUser(user));
				break;
			}

			matcher = RECIPIENT_TEMPLATE_GROUP.matcher(template);
			if (matcher.find()) {
				logger.debug("resolving '{}' as a group", template);
				final String group = matcher.group(1);
				Iterables.addAll(resolved, getEmailsForGroup(group));
				break;
			}

			matcher = RECIPIENT_TEMPLATE_GROUP_USERS.matcher(template);
			if (matcher.find()) {
				logger.debug("resolving '{}' as all group's users", template);
				final String group = matcher.group(1);
				Iterables.addAll(resolved, getEmailsForGroupUsers(group));
				break;
			}

			logger.debug("resolving '{}' as is", template);
			resolved.add(template);
		} while (false);
		return resolved;
	}

	private Iterable<String> getEmailsForUser(final String user) {
		logger.info("getting all email addresses for user '{}'", user);
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
		return StringUtils.isNotBlank(email) ? Arrays.asList(email) : Collections.<String> emptyList();
	}

	private Iterable<String> getEmailsForGroup(final String group) {
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
		return StringUtils.isNotBlank(email) ? Arrays.asList(email) : Collections.<String> emptyList();
	}

	private Iterable<String> getEmailsForGroupUsers(final String group) {
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
			if (StringUtils.isNotBlank(email)) {
				emails.add(email);
			}
		}
		return emails;
	}

}
