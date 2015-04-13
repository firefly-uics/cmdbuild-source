package org.cmdbuild.logic.email;

import static com.google.common.base.Optional.absent;
import static com.google.common.base.Splitter.on;
import static com.google.common.collect.FluentIterable.from;
import static com.google.common.collect.Multimaps.index;
import static java.util.Arrays.asList;
import static org.apache.commons.lang3.StringUtils.defaultIfBlank;
import static org.apache.commons.lang3.StringUtils.defaultString;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.cmdbuild.data.store.email.EmailConstants.ADDRESSES_SEPARATOR;
import static org.cmdbuild.logic.email.EmailLogic.Statuses.outgoing;
import static org.joda.time.DateTime.now;

import javax.activation.DataHandler;

import org.cmdbuild.common.api.mail.MailApi;
import org.cmdbuild.common.api.mail.MailApiFactory;
import org.cmdbuild.common.api.mail.NewMailQueue;
import org.cmdbuild.common.api.mail.QueueableNewMail;
import org.cmdbuild.data.store.email.EmailAccount;
import org.cmdbuild.data.store.email.EmailAccountFacade;
import org.cmdbuild.logic.email.EmailAttachmentsLogic.Attachment;
import org.cmdbuild.logic.email.EmailLogic.Email;
import org.cmdbuild.services.email.AllConfigurationWrapper;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.Multimap;

public class DefaultEmailQueue implements EmailQueue {

	private static final Predicate<Email> DELAY_ELAPSED = new Predicate<Email>() {

		@Override
		public boolean apply(final Email input) {
			return input.getDate().plus(input.getDelay()).isAfter(now());
		}

	};

	private static final Optional<String> ABSENT = absent();

	private static final Function<Email, Optional<String>> ACCOUNT_NAME_OR_ABSENT = new Function<Email, Optional<String>>() {

		@Override
		public Optional<String> apply(final Email input) {
			return isBlank(input.getAccount()) ? ABSENT : Optional.of(input.getAccount());
		}

	};

	private static final String CONTENT_TYPE = "text/html; charset=UTF-8";

	private final EmailAccountFacade emailAccountFacade;
	private final MailApiFactory mailApiFactory;
	private final EmailLogic emailLogic;
	private final EmailAttachmentsLogic emailAttachmensLogic;

	public DefaultEmailQueue(final EmailAccountFacade emailAccountFacade, final MailApiFactory mailApiFactory,
			final EmailLogic emailLogic, final EmailAttachmentsLogic emailAttachmensLogic) {
		this.emailAccountFacade = emailAccountFacade;
		this.mailApiFactory = mailApiFactory;
		this.emailLogic = emailLogic;
		this.emailAttachmensLogic = emailAttachmensLogic;
	}

	@Override
	public void execute() {
		final Iterable<Email> elements = from(emailLogic.readAll(outgoing())) //
				.filter(DELAY_ELAPSED);
		final Multimap<Optional<String>, Email> emailsByAccount = index(elements, ACCOUNT_NAME_OR_ABSENT);
		for (final Optional<String> accountName : emailsByAccount.keySet()) {
			final Optional<EmailAccount> account;
			if (accountName.isPresent()) {
				account = emailAccountFacade.firstOfOrDefault(asList(accountName.get()));
			} else {
				account = emailAccountFacade.defaultAccount();
			}
			if (account.isPresent()) {
				final MailApi api = mailApiFactory.create(AllConfigurationWrapper.of(account.get()));
				final NewMailQueue queue = api.newMailQueue();
				for (final Email email : emailsByAccount.get(accountName)) {
					final QueueableNewMail newMail = queue.newMail() //
							.withFrom(defaultIfBlank(email.getFromAddress(), account.get().getAddress())) //
							.withTo(splitAddresses(defaultString(email.getToAddresses()))) //
							.withCc(splitAddresses(defaultString(email.getCcAddresses()))) //
							.withBcc(splitAddresses(defaultString(email.getBccAddresses()))) //
							.withSubject(email.getSubject()) //
							.withContent(email.getContent()) //
							.withContentType(CONTENT_TYPE);
					for (final Attachment attachment : emailAttachmensLogic.readAll(email)) {
						final Optional<DataHandler> dataHandler = emailAttachmensLogic.read(email, attachment);
						if (dataHandler.isPresent()) {
							newMail.withAttachment(dataHandler.get(), attachment.getFileName());
						}
					}
					newMail.add();
				}
				queue.sendAll();
			} else {
				logger.warn("missing account (even default) going to next one");
			}
		}

	}

	private Iterable<String> splitAddresses(final String addresses) {
		return on(ADDRESSES_SEPARATOR) //
				.omitEmptyStrings() //
				.trimResults() //
				.split(addresses);
	}

}
