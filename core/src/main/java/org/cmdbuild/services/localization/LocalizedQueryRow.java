package org.cmdbuild.services.localization;

import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;
import static org.apache.commons.lang3.StringUtils.defaultIfBlank;

import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;
import org.cmdbuild.dao.entry.CMCard;
import org.cmdbuild.dao.entry.CMRelation;
import org.cmdbuild.dao.entry.ForwardingCard;
import org.cmdbuild.dao.entry.ForwardingRelation;
import org.cmdbuild.dao.entry.LookupValue;
import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.dao.entrytype.CMDomain;
import org.cmdbuild.dao.query.CMQueryRow;
import org.cmdbuild.dao.query.ForwardingQueryRow;
import org.cmdbuild.dao.query.clause.QueryRelation;
import org.cmdbuild.dao.query.clause.alias.Alias;
import org.cmdbuild.data.store.lookup.Lookup;
import org.cmdbuild.data.store.lookup.LookupStore;
import org.cmdbuild.data.store.lookup.LookupType;
import org.cmdbuild.logic.translation.TranslationFacade;
import org.cmdbuild.logic.translation.TranslationObject;
import org.cmdbuild.logic.translation.converter.LookupConverter;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;

public class LocalizedQueryRow extends ForwardingQueryRow {

	private final CMQueryRow delegate;
	private final Function<CMCard, CMCard> TO_LOCALIZED_CARD;
	private final Function<CMRelation, CMRelation> TO_LOCALIZED_RELATION;
	private final Function<LookupValue, LookupValue> TRANSLATE;
	private final Function<Entry<String, Object>, Entry<String, Object>> TRANSLATE_LOOKUPS;

	protected LocalizedQueryRow(final CMQueryRow delegate, final TranslationFacade facade, final LookupStore lookupStore) {
		this.delegate = delegate;
		this.TO_LOCALIZED_CARD = new Function<CMCard, CMCard>() {
			@Override
			public CMCard apply(final CMCard input) {
				return (input == null) ? null : new LocalizedCard(proxyCard(input), facade, lookupStore);
			}

			private CMCard proxyCard(final CMCard input) {
				return new ForwardingCard() {

					@Override
					protected CMCard delegate() {
						return input;
					}

					@Override
					public Object get(final String key) {
						return proxyValue(super.get(key));
					}

					@Override
					public <T> T get(final String key, final Class<? extends T> requiredType) {
						return requiredType.cast(get(key));
					}

					@Override
					public Iterable<Entry<String, Object>> getAllValues() {
						return proxyValues(super.getAllValues());
					}

					@Override
					public Iterable<Entry<String, Object>> getValues() {
						return proxyValues(super.getValues());
					}
				};
			}
		};

		this.TO_LOCALIZED_RELATION = new Function<CMRelation, CMRelation>() {
			@Override
			public CMRelation apply(final CMRelation input) {
				return (input == null) ? null : new LocalizedRelation(proxyRelation(input), facade, lookupStore);
			}

			private CMRelation proxyRelation(final CMRelation input) {
				return new ForwardingRelation() {

					@Override
					protected CMRelation delegate() {
						return input;
					}

					@Override
					public Iterable<Entry<String, Object>> getValues() {
						return proxyValues(super.getValues());
					}

					@Override
					public Iterable<Entry<String, Object>> getAllValues() {
						return proxyValues(super.getAllValues());
					}

					@Override
					public Object get(final String key) {
						return proxyValue(super.get(key));
					}

					@Override
					public <T> T get(final String key, final Class<? extends T> requiredType) {
						return requiredType.cast(super.get(key));
					}
				};
			}
		};

		this.TRANSLATE = new Function<LookupValue, LookupValue>() {

			@Override
			public LookupValue apply(final LookupValue input) {
				final LookupConverter converter = LookupConverter.of(LookupConverter.description());
				final LookupType lookupType = LookupType.newInstance() //
						.withName(input.getLooupType()) //
						.build();
				final Iterable<Lookup> allLookups = lookupStore.readAll(lookupType);
				String uuid = StringUtils.EMPTY;
				for (final Lookup lookup : allLookups) {
					if (lookup.getId().equals(input.getId())) {
						uuid = lookup.getTranslationUuid();
						break;
					}
				}
				final TranslationObject translationObject = converter.create(uuid);
				final String translatedDescription = facade.read(translationObject);
				final String description = defaultIfBlank(translatedDescription, input.getDescription());
				return new LookupValue(input.getId(), description, input.getLooupType(), input.getTranslationUuid());
			}
		};

		this.TRANSLATE_LOOKUPS = new Function<Entry<String, Object>, Entry<String, Object>>() {
			@Override
			public Entry<String, Object> apply(final Entry<String, Object> input) {
				if (input.getValue() instanceof LookupValue) {
					LookupValue lookupValue = LookupValue.class.cast(input.getValue());
					lookupValue = TRANSLATE.apply(lookupValue);
					input.setValue(lookupValue);
				}
				return input;
			}
		};
	}

	@Override
	protected CMQueryRow delegate() {
		return delegate;
	}

	@Override
	public CMCard getCard(final Alias alias) {
		return proxy(super.getCard(alias));
	}

	@Override
	public CMCard getCard(final CMClass type) {
		return proxy(super.getCard(type));
	}

	@Override
	public QueryRelation getRelation(final Alias alias) {
		return proxy(super.getRelation(alias));
	}

	@Override
	public QueryRelation getRelation(final CMDomain type) {
		return proxy(super.getRelation(type));
	}

	private CMCard proxy(final CMCard card) {
		return TO_LOCALIZED_CARD.apply(card);
	}

	private QueryRelation proxy(final QueryRelation queryRelation) {
		return QueryRelation.newInstance(proxy(queryRelation.getRelation()), queryRelation.getQueryDomain()
				.getQuerySource());
	}

	private CMRelation proxy(final CMRelation relation) {
		return TO_LOCALIZED_RELATION.apply(relation);
	}

	private Object proxyValue(final Object attribute) {
		Object translatedAttribute = null;
		if (attribute instanceof LookupValue) {
			translatedAttribute = TRANSLATE.apply((LookupValue) attribute);
		}
		return defaultIfNull(translatedAttribute, attribute);
	}

	private Iterable<Entry<String, Object>> proxyValues(final Iterable<Entry<String, Object>> allValues) {
		return Iterables.transform(allValues, TRANSLATE_LOOKUPS);
	}

}