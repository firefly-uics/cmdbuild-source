package org.cmdbuild.services.localization;

import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;
import static org.apache.commons.lang3.StringUtils.defaultIfBlank;

import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;
import org.cmdbuild.dao.entry.CMCard;
import org.cmdbuild.dao.entry.ForwardingCard;
import org.cmdbuild.dao.entry.LookupValue;
import org.cmdbuild.data.store.lookup.Lookup;
import org.cmdbuild.data.store.lookup.LookupStore;
import org.cmdbuild.data.store.lookup.LookupType;
import org.cmdbuild.logic.translation.TranslationFacade;
import org.cmdbuild.logic.translation.TranslationObject;
import org.cmdbuild.logic.translation.converter.LookupConverter;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;

public class LocalizedCard extends ForwardingCard implements CMCard {
	private final CMCard delegate;
	private final Function<LookupValue, LookupValue> TRANSLATE;
	private final Function<Entry<String, Object>, Entry<String, Object>> TRANSLATE_LOOKUPS;

	public LocalizedCard(final CMCard delegate, final TranslationFacade facade, final LookupStore lookupStore) {

		this.delegate = delegate;

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
	protected CMCard delegate() {
		return delegate;
	}

	@Override
	public Object get(final String key) {
		return proxy(super.get(key));
	}

	private Object proxy(final Object attribute) {
		Object translatedAttribute = null;
		if (attribute instanceof LookupValue) {
			translatedAttribute = TRANSLATE.apply((LookupValue) attribute);
		}
		return defaultIfNull(translatedAttribute, attribute);
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

	private Iterable<Entry<String, Object>> proxyValues(final Iterable<Entry<String, Object>> allValues) {
		return Iterables.transform(super.getAllValues(), TRANSLATE_LOOKUPS);
	}

}
