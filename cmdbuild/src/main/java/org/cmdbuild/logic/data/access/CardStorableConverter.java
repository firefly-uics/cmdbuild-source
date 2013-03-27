package org.cmdbuild.logic.data.access;

import java.util.Map;
import java.util.WeakHashMap;

import org.cmdbuild.dao.entry.CMCard;
import org.cmdbuild.dao.entrytype.CMEntryType;
import org.cmdbuild.model.data.Card;
import org.cmdbuild.services.store.DataViewStore.BaseStorableConverter;

public class CardStorableConverter extends BaseStorableConverter<Card> {

	private static Map<String, CardStorableConverter> cache = new WeakHashMap<String, CardStorableConverter>();

	public static CardStorableConverter of(final CMEntryType entryType) {
		return of(entryType.getName());
	}

	public static CardStorableConverter of(final CMCard card) {
		return of(card.getType().getName());
	}

	public static CardStorableConverter of(final Card card) {
		return of(card.getClassName());
	}

	public static CardStorableConverter of(final String className) {
		synchronized (cache) {
			CardStorableConverter instance = cache.get(className);
			if (instance == null) {
				instance = new CardStorableConverter(className);
				cache.put(className, instance);
			}
			return instance;
		}
	}

	private final String className;

	public CardStorableConverter(final String className) {
		this.className = className;
	}

	@Override
	public String getClassName() {
		return className;
	}

	@Override
	public Card convert(final CMCard card) {
		return Card.newInstance() //
				.withId(card.getId()) //
				.withClassName(card.getType().getName()) //
				.withClassId(card.getType().getId()) //
				.withClassDescription(card.getType().getDescription()) //
				.withBeginDate(card.getBeginDate()) //
				.withEndDate(card.getEndDate()) //
				.withUser(card.getUser()) //
				.withAllAttributes(card.getValues()) //
				.build();
	}

	@Override
	public Map<String, Object> getValues(final Card storable) {
		return storable.getAttributes();
	}

}
