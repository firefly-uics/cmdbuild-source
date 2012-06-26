package org.cmdbuild.dao.legacywrappers;

import java.util.Date;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;

import org.cmdbuild.dao.entry.CMCard;
import org.cmdbuild.dao.entry.CMCard.CMCardDefinition;
import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.elements.AttributeValue;
import org.cmdbuild.elements.history.TableHistory;
import org.cmdbuild.elements.interfaces.ICard;
import org.cmdbuild.elements.interfaces.ICard.CardAttributes;
import org.joda.time.DateTime;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

public class CardWrapper implements CMCard, CMCardDefinition {

	protected static final Set<String> cardSystemAttributes;

	static {
		cardSystemAttributes = new HashSet<String>();
		for (final CardAttributes a : CardAttributes.values()) {
			cardSystemAttributes.add(a.dbColumnName());
		}
		cardSystemAttributes.add(TableHistory.EndDateAttribute);
	}

	protected final ICard card;

	public CardWrapper(final ICard card) {
		this.card = card;
	}

	@Override
	public final Object getId() {
		return Long.valueOf(card.getId());
	}

	@Override
	public final String getUser() {
		return card.getUser();
	}

	@Override
	public final DateTime getBeginDate() {
		return new DateTime(card.getBeginDate());
	}

	@Override
	public final DateTime getEndDate() {
		if (card.getSchema().getAttributes().containsKey(TableHistory.EndDateAttribute)) {
			final Date endDate = card.getAttributeValue(TableHistory.EndDateAttribute).getDate();
			return new DateTime(endDate);
		} else {
			return null;
		}
	}

	@Override
	public final Object get(String key) {
		return card.getValue(key);
	}

	@Override
	public final Iterable<Entry<String, Object>> getValues() {
		return Iterables.transform(getNonSystemAttributeValueMap(), new Function<Entry<String,AttributeValue>, Entry<String, Object>>() {

			@Override
			public Entry<String, Object> apply(final Entry<String, AttributeValue> input) {
				return new Entry<String, Object>() {

					@Override
					public String getKey() {
						return input.getKey();
					}

					@Override
					public Object getValue() {
						return input.getValue();
					}

					@Override
					public Object setValue(Object value) {
						final Object oldValue = getValue(); 
						card.setValue(getKey(), value);
						return oldValue;
					}

				};
			}
			
		});
	}

	private Iterable<Entry<String, AttributeValue>> getNonSystemAttributeValueMap() {
		return Iterables.filter(card.getAttributeValueMap().entrySet(), new Predicate<Entry<String, AttributeValue>>() {

			@Override
			public boolean apply(Entry<String, AttributeValue> input) {
				return isUserAttributeName(input.getKey());
			}

		});
	}

	protected boolean isUserAttributeName(final String name) {
		return !cardSystemAttributes.contains(name);
	}

	@Override
	public CMClass getType() {
		return new ClassWrapper(card.getSchema());
	}

	/*
	 * CMCardDefinition
	 */

	/**
	 * Sets only non-system values
	 */
	@Override
	public CMCardDefinition set(final String key, final Object value) {
		if (isUserAttributeName(key)) {
			card.getAttributeValue(key).setValue(value);
		}
		return this;
	}

	@Override
	public CMCard save() {
		card.save();
		return this;
	}

}
