package org.cmdbuild.logic.data.access;

import static com.google.common.collect.FluentIterable.from;
import static com.google.common.collect.Maps.newHashMap;
import static com.google.common.collect.Sets.newHashSet;
import static org.cmdbuild.dao.driver.postgres.Const.DESCRIPTION_ATTRIBUTE;
import static org.cmdbuild.dao.driver.postgres.Const.ID_ATTRIBUTE;
import static org.cmdbuild.dao.query.clause.QueryAliasAttribute.attribute;
import static org.cmdbuild.dao.query.clause.where.InOperatorAndValue.in;
import static org.cmdbuild.dao.query.clause.where.SimpleWhereClause.condition;

import java.util.Map;
import java.util.Set;

import org.cmdbuild.common.Builder;
import org.cmdbuild.common.Constants;
import org.cmdbuild.dao.entry.CMCard;
import org.cmdbuild.dao.entry.CMEntry;
import org.cmdbuild.dao.entrytype.CMAttribute;
import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.dao.entrytype.CMDomain;
import org.cmdbuild.dao.entrytype.attributetype.DateAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.DateTimeAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.ForeignKeyAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.LookupAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.NullAttributeTypeVisitor;
import org.cmdbuild.dao.entrytype.attributetype.ReferenceAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.TimeAttributeType;
import org.cmdbuild.dao.query.CMQueryRow;
import org.cmdbuild.dao.view.CMDataView;
import org.cmdbuild.data.store.DataViewStore;
import org.cmdbuild.data.store.Store;
import org.cmdbuild.data.store.lookup.LookupDto;
import org.cmdbuild.data.store.lookup.LookupStorableConverter;
import org.cmdbuild.exception.NotFoundException.NotFoundExceptionType;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import com.google.common.base.Function;

public class ForeignReferenceResolver<T extends CMEntry> {

	public static interface EntryFiller<T extends CMEntry> {

		void setInput(T input);

		void setValue(String name, Object value);

		T getOutput();

	}

	public static class ForeignReferenceResolverBuilder<T extends CMEntry> implements
			Builder<ForeignReferenceResolver<T>> {

		private CMDataView systemDataView;
		private CMClass entryType;
		private Iterable<T> entries;
		public EntryFiller<T> entryFiller;

		@Override
		public ForeignReferenceResolver<T> build() {
			return new ForeignReferenceResolver<T>(this);
		}

		public ForeignReferenceResolverBuilder<T> withSystemDataView(final CMDataView value) {
			systemDataView = value;
			return this;
		}

		public ForeignReferenceResolverBuilder<T> withEntryType(final CMClass value) {
			entryType = value;
			return this;
		}

		public ForeignReferenceResolverBuilder<T> withEntries(final Iterable<T> value) {
			entries = value;
			return this;
		}

		public ForeignReferenceResolverBuilder<T> withEntryFiller(final EntryFiller<T> value) {
			entryFiller = value;
			return this;
		}

	}

	public static <T extends CMEntry> ForeignReferenceResolverBuilder<T> newInstance() {
		return new ForeignReferenceResolverBuilder<T>();
	}

	private final CMDataView systemDataView;
	private final CMClass entryType;
	private final Iterable<T> entries;
	private final EntryFiller<T> valueSetter;

	private final Map<CMClass, Set<Long>> idsByEntryType = newHashMap();
	private final Map<Long, String> representationsById = newHashMap();

	public ForeignReferenceResolver(final ForeignReferenceResolverBuilder<T> builder) {
		this.systemDataView = builder.systemDataView;
		this.entryType = builder.entryType;
		this.entries = builder.entries;
		this.valueSetter = builder.entryFiller;
	}

	public Iterable<T> resolve() {
		extractIdsByEntryType();
		calculateRepresentationsById();

		return from(entries) //
				.transform(new Function<T, T>() {

					final Store<LookupDto> lookupStore = new DataViewStore<LookupDto>(systemDataView,
							new LookupStorableConverter());

					@Override
					public T apply(final T input) {

						valueSetter.setInput(input);

						for (final CMAttribute attribute : input.getType().getAllAttributes()) {
							final String attributeName = attribute.getName();
							final Object rawValue = input.get(attributeName);
							if (rawValue == null) {
								continue;
							}

							valueSetter.setValue(attributeName, rawValue);

							/*
							 * FIXME move away?! looks like serializing stuff
							 */
							attribute.getType().accept(new NullAttributeTypeVisitor() {

								@Override
								public void visit(final ForeignKeyAttributeType attributeType) {
									final Long id = attributeType.convertValue(rawValue);
									setAttribute(attributeName, idAndDescription(id, representationsById.get(id)));
								}

								@Override
								public void visit(final LookupAttributeType attributeType) {
									final Long id = attributeType.convertValue(rawValue);
									final LookupDto lookup = lookupStore.read(LookupDto.newInstance() //
											.withId(id) //
											.build());
									setAttribute(attributeName, idAndDescription(lookup.id, descriptionOf(lookup)));
								}

								private String descriptionOf(final LookupDto lookup) {
									final String concatFormat = "%s - %s";
									String description = lookup.description;
									LookupDto parent = lookup.parent;
									if (parent != null) {
										description = String.format(concatFormat, descriptionOf(parent), description);
									}
									return description;
								}

								@Override
								public void visit(final ReferenceAttributeType attributeType) {
									final Long id = attributeType.convertValue(rawValue);
									setAttribute(attributeName, idAndDescription(id, representationsById.get(id)));
								}

								@Override
								public void visit(final DateAttributeType attributeType) {
									final DateTime date = attributeType.convertValue(rawValue);
									final DateTimeFormatter fmt = DateTimeFormat
											.forPattern(Constants.DATE_PRINTING_PATTERN);

									setAttribute(attributeName, fmt.print(date));
								}

								@Override
								public void visit(final TimeAttributeType attributeType) {
									final DateTime date = attributeType.convertValue(rawValue);
									final DateTimeFormatter fmt = DateTimeFormat
											.forPattern(Constants.TIME_PRINTING_PATTERN);

									setAttribute(attributeName, fmt.print(date));
								}

								@Override
								public void visit(final DateTimeAttributeType attributeType) {
									final DateTime date = attributeType.convertValue(rawValue);
									final DateTimeFormatter fmt = DateTimeFormat
											.forPattern(Constants.DATETIME_PRINTING_PATTERN);

									setAttribute(attributeName, fmt.print(date));
								}

								private void setAttribute(final String name, final Object value) {
									valueSetter.setValue(name, value);
								}

								private Map<String, Object> idAndDescription(final Long id, final String description) {
									final Map<String, Object> value = newHashMap();
									value.put("id", id);
									value.put("description", description);
									return value;
								}

							});

						}
						return valueSetter.getOutput();
					}

				});
	}

	private void extractIdsByEntryType() {
		for (final CMAttribute attribute : entryType.getActiveAttributes()) {
			attribute.getType().accept(new NullAttributeTypeVisitor() {

				@Override
				public void visit(final ForeignKeyAttributeType attributeType) {
					final String className = attributeType.getForeignKeyDestinationClassName();
					final CMClass target = systemDataView.findClass(className);
					extractIdsOfTarget(target);
				}

				@Override
				public void visit(final ReferenceAttributeType attributeType) {
					final ReferenceAttributeType type = ReferenceAttributeType.class.cast(attribute.getType());
					final CMDomain domain = systemDataView.findDomain(type.getIdentifier().getLocalName());
					if (domain == null) {
						throw NotFoundExceptionType.DOMAIN_NOTFOUND
								.createException(type.getIdentifier().getLocalName());
					}
					final CMClass target = domain.getClass1().isAncestorOf(entryType) ? domain.getClass2() : domain
							.getClass1();
					extractIdsOfTarget(target);
				}

				private void extractIdsOfTarget(final CMClass target) {
					Set<Long> ids = idsByEntryType.get(target);
					if (ids == null) {
						ids = newHashSet();
						idsByEntryType.put(target, ids);
					}

					for (final T entry : entries) {
						final Long id = entry.get(attribute.getName(), Long.class);
						ids.add(id);
					}
				}

			});
		}
	}

	private void calculateRepresentationsById() {
		for (final CMClass entryType : idsByEntryType.keySet()) {
			final Set<Long> ids = idsByEntryType.get(entryType);
			if (ids.isEmpty()) {
				continue;
			}
			final Iterable<CMQueryRow> rows = systemDataView.select(DESCRIPTION_ATTRIBUTE) //
					.from(entryType) //
					.where(condition(attribute(entryType, ID_ATTRIBUTE), in(ids.toArray()))) //
					.run();
			for (final CMQueryRow row : rows) {
				final CMCard card = row.getCard(entryType);
				representationsById.put(card.getId(), card.get(DESCRIPTION_ATTRIBUTE, String.class));
			}
		}
	}

}
