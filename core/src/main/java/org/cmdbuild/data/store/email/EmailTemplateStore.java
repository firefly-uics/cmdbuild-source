package org.cmdbuild.data.store.email;

import static com.google.common.base.Suppliers.memoize;
import static com.google.common.collect.FluentIterable.from;
import static com.google.common.collect.Maps.transformValues;
import static com.google.common.collect.Maps.uniqueIndex;
import static org.apache.commons.lang3.StringUtils.defaultString;
import static org.cmdbuild.dao.query.clause.AnyAttribute.anyAttribute;
import static org.cmdbuild.dao.query.clause.QueryAliasAttribute.attribute;
import static org.cmdbuild.dao.query.clause.alias.Utils.as;
import static org.cmdbuild.dao.query.clause.join.Over.over;
import static org.cmdbuild.dao.query.clause.where.EqualsOperatorAndValue.eq;
import static org.cmdbuild.dao.query.clause.where.SimpleWhereClause.condition;
import static org.cmdbuild.dao.query.clause.where.WhereClauses.alwaysTrue;
import static org.cmdbuild.data.store.Groupables.notGroupable;
import static org.cmdbuild.data.store.email.EmailTemplateStorableConverter.BCC;
import static org.cmdbuild.data.store.email.EmailTemplateStorableConverter.BODY;
import static org.cmdbuild.data.store.email.EmailTemplateStorableConverter.CC;
import static org.cmdbuild.data.store.email.EmailTemplateStorableConverter.DESCRIPTION;
import static org.cmdbuild.data.store.email.EmailTemplateStorableConverter.FROM;
import static org.cmdbuild.data.store.email.EmailTemplateStorableConverter.NAME;
import static org.cmdbuild.data.store.email.EmailTemplateStorableConverter.SUBJECT;
import static org.cmdbuild.data.store.email.EmailTemplateStorableConverter.TO;

import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.Validate;
import org.cmdbuild.dao.entry.CMCard;
import org.cmdbuild.dao.entry.CMCard.CMCardDefinition;
import org.cmdbuild.dao.entry.CMRelation;
import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.dao.entrytype.CMDomain;
import org.cmdbuild.dao.entrytype.CMEntryType;
import org.cmdbuild.dao.query.CMQueryRow;
import org.cmdbuild.dao.query.clause.QueryDomain.Source;
import org.cmdbuild.dao.query.clause.alias.Alias;
import org.cmdbuild.dao.query.clause.alias.NameAlias;
import org.cmdbuild.dao.query.clause.where.WhereClause;
import org.cmdbuild.dao.view.CMDataView;
import org.cmdbuild.data.store.Groupable;
import org.cmdbuild.data.store.Storable;
import org.cmdbuild.data.store.Store;
import org.cmdbuild.data.store.metadata.MetadataConverter;
import org.cmdbuild.exception.CMDBException;
import org.cmdbuild.exception.NotFoundException;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Supplier;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class EmailTemplateStore implements Store<EmailTemplate> {

	private static final Marker marker = MarkerFactory.getMarker(EmailTemplateStore.class.getName());

	public static class Builder implements org.apache.commons.lang3.builder.Builder<EmailTemplateStore> {

		private CMDataView dataView;

		private Builder() {
			// use factory method
		}

		@Override
		public EmailTemplateStore build() {
			validate();
			return new EmailTemplateStore(this);
		}

		private void validate() {
			Validate.notNull(dataView, "missing '%s'", CMDataView.class);
		}

		public Builder withDataView(final CMDataView dataView) {
			this.dataView = dataView;
			return this;
		}

	}

	public static Builder newInstance() {
		return new Builder();
	}

	private static abstract class EntryTypeSupplier<T extends CMEntryType> implements Supplier<T> {

		protected final CMDataView dataView;
		protected final String name;

		protected EntryTypeSupplier(final CMDataView dataView, final String name) {
			this.dataView = dataView;
			this.name = name;
		}

		@Override
		public T get() {
			final T found = find();
			if (found == null) {
				logger.error(marker, "'{}' has not been found", BASE_CLASS);
				throw notFoundException();
			}
			return found;
		}

		protected abstract T find();

		protected abstract CMDBException notFoundException();

	}

	private static class ClassSupplier extends EntryTypeSupplier<CMClass> {

		public ClassSupplier(final CMDataView dataView, final String name) {
			super(dataView, name);
		}

		@Override
		protected CMClass find() {
			return dataView.findClass(name);
		}

		@Override
		protected CMDBException notFoundException() {
			return NotFoundException.NotFoundExceptionType.CLASS_NOTFOUND.createException(name);
		}

	}

	private static class DomainSupplier extends EntryTypeSupplier<CMDomain> {

		public DomainSupplier(final CMDataView dataView, final String name) {
			super(dataView, name);
		}

		@Override
		protected CMDomain find() {
			return dataView.findDomain(name);
		}

		@Override
		protected CMDBException notFoundException() {
			return NotFoundException.NotFoundExceptionType.DOMAIN_NOTFOUND.createException(name);
		}

	}

	private static class CardSupplier implements Supplier<CMCard> {

		private final CMCard card;
		private final Long id;

		public CardSupplier(final CMCard card) {
			this.card = card;
			this.id = card.getId();
		}

		@Override
		public CMCard get() {
			return card;
		}

		@Override
		public boolean equals(final Object obj) {
			if (obj == this) {
				return true;
			}
			if (!(obj instanceof CardSupplier)) {
				return false;
			}
			final CardSupplier other = CardSupplier.class.cast(obj);
			return id.equals(other.id);
		}

		@Override
		public int hashCode() {
			return id.hashCode();
		}

	}

	private static final Function<CardSupplier, String> TO_METADATA_NAME = new Function<CardSupplier, String>() {

		@Override
		public String apply(final CardSupplier input) {
			return input.get().get(MetadataConverter.NAME, String.class);
		}

	};

	private static final Function<CardSupplier, String> TO_METADATA_VALUE = new Function<CardSupplier, String>() {

		@Override
		public String apply(final CardSupplier input) {
			return input.get().get(MetadataConverter.VALUE, String.class);
		}

	};

	private static Function<Entry<CardSupplier, Collection<CardSupplier>>, EmailTemplate> TO_EMAIL_TEMPLATE = new Function<Entry<CardSupplier, Collection<CardSupplier>>, EmailTemplate>() {

		@Override
		public EmailTemplate apply(final Entry<CardSupplier, Collection<CardSupplier>> input) {
			final CMCard source = input.getKey().get();
			final Collection<CardSupplier> destinations = input.getValue();
			final Map<String, String> variables = transformValues(uniqueIndex(destinations, TO_METADATA_NAME),
					TO_METADATA_VALUE);
			return EmailTemplate.newInstance() //
					.withId(source.getId()) //
					.withName(defaultString(source.get(NAME, String.class))) //
					.withDescription(defaultString(source.get(DESCRIPTION, String.class))) //
					.withFrom(defaultString(source.get(FROM, String.class))) //
					.withTo(defaultString(source.get(TO, String.class))) //
					.withCc(defaultString(source.get(CC, String.class))) //
					.withBcc(defaultString(source.get(BCC, String.class))) //
					.withSubject(defaultString(source.get(SUBJECT, String.class))) //
					.withBody(defaultString(source.get(BODY, String.class))) //
					.withVariables(variables) //
					.build();
		}

	};

	private static final String BASE_CLASS = "_EmailTemplate";
	private static final String CLASS_METADATA = "ClassMetadata";

	protected static final Alias EMAIL_TEMPLATE = NameAlias.as("SRC");
	protected static final Alias METADATA = NameAlias.as("DST");
	protected static final Alias DOMAIN = NameAlias.as("DOM");

	private final CMDataView dataView;
	private final Supplier<CMClass> source;
	private final Supplier<CMClass> destination;
	private final Supplier<CMDomain> domain;

	private EmailTemplateStore(final Builder builder) {
		this.dataView = builder.dataView;
		this.source = memoize(new ClassSupplier(dataView, BASE_CLASS));
		this.destination = memoize(new ClassSupplier(dataView, MetadataConverter.CLASS_NAME));
		this.domain = memoize(new DomainSupplier(dataView, CLASS_METADATA));
	}

	private CMClass emailTemplate() {
		return source.get();
	}

	private CMClass metadata() {
		return destination.get();
	}

	private CMDomain domain() {
		return domain.get();
	}

	@Override
	public Storable create(final EmailTemplate storable) {
		logger.debug(marker, "creating new element");
		final CMCard master = fill(dataView.createCardFor(emailTemplate()), storable).save();
		for (final Entry<String, String> entry : storable.getVariables().entrySet()) {
			final CMCard detail = fill(dataView.createCardFor(metadata()), entry).save();
			dataView.createRelationFor(domain()) //
					.setCard1(master) //
					.setCard2(detail) //
					.create();
		}
		return storable;
	}

	@Override
	public EmailTemplate read(final Storable storable) {
		logger.debug(marker, "reading element '{}'", storable);
		final Iterable<CMQueryRow> result = query(whereClauseOf(storable));
		return from(convertToStorable(result)) //
				.first() //
				.get();
	}

	@Override
	public Collection<EmailTemplate> readAll() {
		logger.debug(marker, "reading all elements");
		return readAll(notGroupable());
	}

	@Override
	public Collection<EmailTemplate> readAll(final Groupable groupable) {
		logger.debug(marker, "reading all elements with additional grouping condition '{}'", groupable);
		final WhereClause whereClause = whereClauseOf(groupable);
		final Iterable<CMQueryRow> result = query(whereClause);
		return from(convertToStorable(result)) //
				.toList();
	}

	@Override
	public void update(final EmailTemplate storable) {
		logger.debug(marker, "updating element '{}'", storable);
		final Iterable<CMQueryRow> result = query(whereClauseOf(storable));
		final CMCard emailTemplate = emailTemplateFrom(result.iterator().next());
		fill(dataView.update(emailTemplate), storable).save();
		final Map<String, String> currentVariables = Maps.newHashMap(storable.getVariables());
		for (final CMQueryRow row : result) {
			final Optional<CMCard> optionalMetadata = metadataFrom(row);
			if (optionalMetadata.isPresent()) {
				final CMCard metadata = optionalMetadata.get();
				final String name = metadata.get(MetadataConverter.NAME, String.class);
				final String value = metadata.get(MetadataConverter.VALUE, String.class);
				if (currentVariables.containsKey(name)) {
					// should update value or do nothing
					final String newValue = currentVariables.get(name);
					if (ObjectUtils.equals(value, newValue)) {
						// nothing to do
					} else {
						fill(dataView.update(metadata), Maps.immutableEntry(name, newValue)).save();
					}
				} else {
					// should delete relation and card
					final Optional<CMRelation> optionalRelation = relationFrom(row);
					if (optionalRelation.isPresent()) {
						final CMRelation relation = optionalRelation.get();
						dataView.delete(relation);
					}
					dataView.delete(metadata);
				}
				currentVariables.remove(name);
			}
		}
		for (final Entry<String, String> entry : currentVariables.entrySet()) {
			final CMCard metadata = fill(dataView.createCardFor(metadata()), entry).save();
			dataView.createRelationFor(domain()) //
					.setCard1(emailTemplate) //
					.setCard2(metadata) //
					.create();
		}
	}

	@Override
	public void delete(final Storable storable) {
		logger.debug(marker, "deleting element '{}'", storable);
		final Iterable<CMQueryRow> result = query(whereClauseOf(storable));
		final CMCard emailTemplate = emailTemplateFrom(result.iterator().next());
		for (final CMQueryRow row : result) {
			final Optional<CMRelation> optionalRelation = relationFrom(row);
			if (optionalRelation.isPresent()) {
				final CMRelation relation = optionalRelation.get();
				dataView.delete(relation);
			}
			final Optional<CMCard> optionalMetadata = metadataFrom(row);
			if (optionalMetadata.isPresent()) {
				final CMCard metadata = optionalMetadata.get();
				dataView.delete(metadata);
			}
		}
		dataView.delete(emailTemplate);
	}

	private CMCardDefinition fill(final CMCardDefinition card, final EmailTemplate emailTemplate) {
		return card //
				.set(NAME, emailTemplate.getName()) //
				.set(DESCRIPTION, emailTemplate.getDescription()) //
				.set(FROM, emailTemplate.getFrom()) //
				.set(TO, emailTemplate.getTo()) //
				.set(CC, emailTemplate.getCc()) //
				.set(BCC, emailTemplate.getBcc()) //
				.set(SUBJECT, emailTemplate.getSubject()) //
				.set(BODY, emailTemplate.getBody());
	}

	private CMCardDefinition fill(final CMCardDefinition card, final Entry<String, String> metadata) {
		return card //
				.set(MetadataConverter.NAME, metadata.getKey()) //
				.set(MetadataConverter.VALUE, metadata.getValue());
	}

	private Iterable<CMQueryRow> query(final WhereClause whereClause) {
		return dataView //
				.select(anyAttribute(EMAIL_TEMPLATE), anyAttribute(METADATA)) //
				.from(emailTemplate(), as(EMAIL_TEMPLATE)) //
				.leftJoin(metadata(), as(METADATA), over(domain(), as(DOMAIN)), Source._1) //
				.where(whereClause) //
				.run();
	}

	private WhereClause whereClauseOf(final Storable storable) {
		logger.debug(marker, "building specific where clause");
		final String attributeName = NAME;
		final Object attributeValue = storable.getIdentifier();
		return condition(attribute(EMAIL_TEMPLATE, attributeName), eq(attributeValue));
	}

	private WhereClause whereClauseOf(final Groupable groupable) {
		logger.debug(marker, "building group where clause");
		final WhereClause clause;
		final String attributeName = groupable.getGroupAttributeName();
		if (attributeName != null) {
			logger.debug(marker, "group attribute name is '{}', building where clause", attributeName);
			final Object attributeValue = groupable.getGroupAttributeValue();
			clause = condition(attribute(emailTemplate(), attributeName), eq(attributeValue));
		} else {
			logger.debug(marker, "group attribute name not specified");
			clause = alwaysTrue();
		}
		return clause;
	}

	private Iterable<EmailTemplate> convertToStorable(final Iterable<CMQueryRow> result) {
		final Map<CardSupplier, Collection<CardSupplier>> map = Maps.newHashMap();
		for (final CMQueryRow row : result) {
			final CMCard emailTemplate = emailTemplateFrom(row);
			final Optional<CMCard> optionalMetadata = metadataFrom(row);
			if (!map.containsKey(new CardSupplier(emailTemplate))) {
				final Collection<CardSupplier> destinations = Lists.newArrayList();
				map.put(new CardSupplier(emailTemplate), destinations);
			}
			if (optionalMetadata.isPresent()) {
				map.get(new CardSupplier(emailTemplate)).add(new CardSupplier(optionalMetadata.get()));
			}
		}
		return from(map.entrySet()) //
				.transform(TO_EMAIL_TEMPLATE);
	}

	private CMCard emailTemplateFrom(final CMQueryRow row) {
		return row.getCard(EMAIL_TEMPLATE);
	}

	private Optional<CMCard> metadataFrom(final CMQueryRow row) {
		try {
			return Optional.of(row.getCard(METADATA));
		} catch (final Exception e) {
			return Optional.absent();
		}
	}

	private Optional<CMRelation> relationFrom(final CMQueryRow row) {
		try {
			return Optional.of(row.getRelation(DOMAIN).getRelation());
		} catch (final Exception e) {
			return Optional.absent();
		}
	}

}
