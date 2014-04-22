package org.cmdbuild.services.template;

import static org.cmdbuild.dao.query.clause.AnyAttribute.anyAttribute;
import static org.cmdbuild.dao.query.clause.QueryAliasAttribute.attribute;
import static org.cmdbuild.dao.query.clause.where.EqualsOperatorAndValue.eq;
import static org.cmdbuild.dao.query.clause.where.SimpleWhereClause.condition;

import org.apache.commons.lang3.Validate;
import org.cmdbuild.common.template.TemplateResolverEngine;
import org.cmdbuild.dao.entry.CMCard;
import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.dao.view.CMDataView;

public class UserEmailTemplateEngine implements TemplateResolverEngine {

	private static final String USER_CLASSNAME = "User";
	private static final String USERNAME_ATTRIBUTE = "Username";
	private static final String EMAIL_ATTRIBUTE = "Email";

	public static class Builder implements org.apache.commons.lang3.builder.Builder<UserEmailTemplateEngine> {

		private CMDataView dataView;

		@Override
		public UserEmailTemplateEngine build() {
			validate();
			return new UserEmailTemplateEngine(this);
		}

		private void validate() {
			Validate.notNull(dataView, "missing data view");
		}

		public Builder withDataView(final CMDataView dataView) {
			this.dataView = dataView;
			return this;
		}

	}

	public static Builder newInstance() {
		return new Builder();
	}

	private final CMDataView dataView;

	private UserEmailTemplateEngine(final Builder builder) {
		this.dataView = builder.dataView;
	}

	@Override
	public Object eval(final String expression) {
		final CMClass userClass = dataView.findClass(USER_CLASSNAME);
		Validate.notNull(userClass, "user class not visible");
		final CMCard card = dataView.select(anyAttribute(userClass)) //
				.from(userClass) //
				.where(condition(attribute(userClass, USERNAME_ATTRIBUTE), eq(expression))) //
				.run() //
				.getOnlyRow() //
				.getCard(userClass);
		return card.get(EMAIL_ATTRIBUTE, String.class);
	}

}