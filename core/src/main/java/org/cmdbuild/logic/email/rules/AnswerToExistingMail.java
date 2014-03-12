package org.cmdbuild.logic.email.rules;

import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.apache.commons.lang3.StringUtils.join;
import static org.cmdbuild.common.Constants.BASE_CLASS_NAME;
import static org.cmdbuild.common.Constants.CODE_ATTRIBUTE;
import static org.cmdbuild.common.Constants.DESCRIPTION_ATTRIBUTE;
import static org.cmdbuild.common.Constants.ID_ATTRIBUTE;
import static org.cmdbuild.dao.query.clause.AnyAttribute.anyAttribute;
import static org.cmdbuild.dao.query.clause.QueryAliasAttribute.attribute;
import static org.cmdbuild.dao.query.clause.where.EqualsOperatorAndValue.eq;
import static org.cmdbuild.dao.query.clause.where.SimpleWhereClause.condition;

import java.util.List;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.cmdbuild.common.Holder;
import org.cmdbuild.common.SingletonHolder;
import org.cmdbuild.dao.entry.CMCard;
import org.cmdbuild.dao.entry.IdAndDescription;
import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.dao.entrytype.attributetype.CMAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.ForeignKeyAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.LookupAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.ReferenceAttributeType;
import org.cmdbuild.dao.view.CMDataView;
import org.cmdbuild.data.store.email.EmailTemplate;
import org.cmdbuild.data.store.lookup.Lookup;
import org.cmdbuild.data.store.lookup.LookupStore;
import org.cmdbuild.data.store.lookup.LookupType;
import org.cmdbuild.logic.Logic;
import org.cmdbuild.model.email.Email;
import org.cmdbuild.model.email.EmailConstants;
import org.cmdbuild.services.email.DefaultEmailTemplateResolver;
import org.cmdbuild.services.email.EmailCallbackHandler.Rule;
import org.cmdbuild.services.email.EmailCallbackHandler.RuleAction;
import org.cmdbuild.services.email.EmailPersistence;
import org.cmdbuild.services.email.EmailService;
import org.cmdbuild.services.email.EmailTemplateResolver;
import org.cmdbuild.services.email.EmailTemplateResolver.Configuration;
import org.cmdbuild.services.email.EmailTemplateResolver.DataFacade;
import org.cmdbuild.services.email.ForwardingDataFacade;
import org.cmdbuild.services.email.SubjectHandler;
import org.cmdbuild.services.email.SubjectHandler.ParsedSubject;
import org.slf4j.Logger;

import com.google.common.collect.Lists;

public class AnswerToExistingMail implements Rule {

	private static final Logger logger = Logic.logger;

	private static class CardHolder extends SingletonHolder<CMCard> {

		private final CMDataView dataView;
		private final Long id;

		public CardHolder(final CMDataView dataView, final Long id) {
			this.dataView = dataView;
			this.id = id;
		}

		@Override
		protected CMCard doGet() {
			final CMClass baseClass = dataView.findClass(BASE_CLASS_NAME);
			final CMCard genericCard = dataView.select(attribute(baseClass, CODE_ATTRIBUTE)) //
					.from(baseClass) //
					.where(condition(attribute(baseClass, ID_ATTRIBUTE), eq(id))) //
					.run() //
					.getOnlyRow() //
					.getCard(baseClass);
			final CMClass realClass = genericCard.getType();
			return dataView.select(anyAttribute(realClass)) //
					.from(realClass) //
					.where(condition(attribute(realClass, ID_ATTRIBUTE), eq(id))) //
					.run() //
					.getOnlyRow() //
					.getCard(realClass);
		}

	}

	private static class DataFacadeForReferencedCard extends ForwardingDataFacade {

		private final Holder<CMCard> cardHolder;
		private final CMDataView dataView;
		private final LookupStore lookupStore;

		public DataFacadeForReferencedCard( //
				final DataFacade dataFacade, //
				final Email email, //
				final CMDataView dataView, //
				final LookupStore lookupStore //
		) {
			super(dataFacade);
			this.cardHolder = new CardHolder(dataView, email.getActivityId());
			this.dataView = dataView;
			this.lookupStore = lookupStore;
		}

		@Override
		public String getAttributeValue(final String attribute) {
			logger.debug("getting value for attribute '{}'", attribute);
			return cardHolder.get().get(attribute).toString();
		}

		@Override
		public String getReferenceAttributeValue(final String attribute, final String subAttribute) {
			logger.debug("getting value for referenced card's attribute '{}'", attribute);
			final String value;
			final CMCard card = cardHolder.get();
			final CMAttributeType<?> attributeType = card.getType().getAttribute(attribute).getType();
			if (attributeType instanceof ReferenceAttributeType) {
				final IdAndDescription cardReference = card.get(attribute, IdAndDescription.class);
				final Long id = cardReference.getId();
				value = new CardHolder(dataView, id).get().get(subAttribute).toString();
			} else if (attributeType instanceof LookupAttributeType) {
				final IdAndDescription cardReference = card.get(attribute, IdAndDescription.class);
				final String lookupTypeName = LookupAttributeType.class.cast(attributeType).getLookupTypeName();
				final LookupType lookupType = LookupType.newInstance().withName(lookupTypeName).build();
				String lookupValue = EMPTY;
				for (final Lookup lookup : lookupStore.listForType(lookupType)) {
					if (lookup.getId().equals(cardReference.getId())) {
						if (CODE_ATTRIBUTE.equals(subAttribute)) {
							lookupValue = lookup.code;
						} else if (DESCRIPTION_ATTRIBUTE.equals(subAttribute)) {
							lookupValue = lookup.description;
						}
						break;
					}
				}
				value = lookupValue;
			} else if (attributeType instanceof ForeignKeyAttributeType) {
				final IdAndDescription cardReference = card.get(attribute, IdAndDescription.class);
				final Long id = cardReference.getId();
				value = new CardHolder(dataView, id).get().get(subAttribute).toString();
			} else {
				logger.warn("referenced attribute not supported for type '{}'", attributeType);
				value = card.get(attribute).toString();
			}
			return value;
		}

	}

	private final EmailService service;
	private final EmailPersistence persistence;
	private final SubjectHandler subjectHandler;
	private final EmailTemplateResolver.DataFacade dataFacade;
	private final CMDataView dataView;
	private final LookupStore lookupStore;

	private ParsedSubject parsedSubject;
	private Email parentEmail;

	public AnswerToExistingMail( //
			final EmailService service, //
			final EmailPersistence persistence, //
			final SubjectHandler subjectHandler, //
			final EmailTemplateResolver.DataFacade dataFacade, //
			final CMDataView dataView, //
			final LookupStore lookupStore //
	) {
		this.service = service;
		this.persistence = persistence;
		this.subjectHandler = subjectHandler;
		this.dataFacade = dataFacade;
		this.dataView = dataView;
		this.lookupStore = lookupStore;
	}

	@Override
	public boolean applies(final Email email) {
		parsedSubject = subjectHandler.parse(email.getSubject());
		if (!parsedSubject.hasExpectedFormat()) {
			return false;
		}

		try {
			parentEmail = persistence.getEmail(parsedSubject.getEmailId());
		} catch (final Exception e) {
			return false;
		}

		return true;
	}

	@Override
	public Email adapt(final Email email) {
		email.setSubject(parsedSubject.getRealSubject());
		email.setActivityId(parentEmail.getActivityId());
		email.setNotifyWith(parentEmail.getNotifyWith());
		return email;
	}

	@Override
	public RuleAction action(final Email email) {
		return new RuleAction() {

			private final DataFacade dataFacade = new DataFacadeForReferencedCard( //
					AnswerToExistingMail.this.dataFacade, //
					email, dataView, lookupStore);

			@Override
			public void execute() {
				sendNotificationFor(email);
			}

			private void sendNotificationFor(final Email email) {
				logger.debug("sending notification for email with id '{}'", email.getId());
				try {
					for (final EmailTemplate emailTemplate : service.getEmailTemplates(email)) {
						final Email notification = new Email();
						notification.setToAddresses(resolveRecipients(emailTemplate.getToAddresses(), email));
						notification.setCcAddresses(resolveRecipients(emailTemplate.getCCAddresses(), email));
						notification.setBccAddresses(resolveRecipients(emailTemplate.getBCCAddresses(), email));
						notification.setSubject(resolveText(emailTemplate.getSubject(), email));
						notification.setContent(resolveText(emailTemplate.getBody(), email));
						service.send(notification);
					}
				} catch (final Exception e) {
					logger.warn("error sending notification", e);
				}
			}

			private String resolveRecipients(final Iterable<String> recipients, final Email email) {
				final List<String> resolvedRecipients = Lists.newArrayList();
				for (final String recipient : recipients) {
					final EmailTemplateResolver resolver = new DefaultEmailTemplateResolver(
							configuration(EmailConstants.ADDRESSES_SEPARATOR));
					final String resolvedRecipient = resolver.resolve(recipient, email);
					resolvedRecipients.add(resolvedRecipient);
				}
				return join(resolvedRecipients.iterator(), EmailConstants.ADDRESSES_SEPARATOR);
			}

			private String resolveText(final String text, final Email email) {
				final EmailTemplateResolver resolver = new DefaultEmailTemplateResolver(configuration());
				return resolver.resolve(text, email);
			}

			private Configuration configuration() {
				return configuration(null);
			}

			private Configuration configuration(final String separator) {
				return new Configuration() {

					@Override
					public DataFacade dataFacade() {
						return dataFacade;
					}

					@Override
					public String multiSeparator() {
						return separator;
					}

				};
			}

		};
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE) //
				.toString();
	}

}