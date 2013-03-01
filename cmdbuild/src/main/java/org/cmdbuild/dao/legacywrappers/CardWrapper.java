package org.cmdbuild.dao.legacywrappers;

import java.util.Date;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;

import org.cmdbuild.common.annotations.OldDao;
import org.cmdbuild.dao.entry.CMCard;
import org.cmdbuild.dao.entry.CMCard.CMCardDefinition;
import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.dao.reference.CardReference;
import org.cmdbuild.elements.AttributeValue;
import org.cmdbuild.elements.Reference;
import org.cmdbuild.elements.history.TableHistory;
import org.cmdbuild.elements.interfaces.ICard;
import org.cmdbuild.elements.interfaces.ICard.CardAttributes;
import org.cmdbuild.elements.interfaces.ITable;
import org.cmdbuild.exception.NotFoundException;
import org.cmdbuild.services.auth.UserContext;
import org.cmdbuild.services.auth.UserOperations;
import org.joda.time.DateTime;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

/**
 * Wrapper for {@link ICard}.
 */
@OldDao
public class CardWrapper implements CMCard, CMCardDefinition {

	protected static final Set<String> cardSystemAttributes;

	static {
		cardSystemAttributes = new HashSet<String>();
		for (final CardAttributes a : CardAttributes.values()) {
			if (a.isVisibleByUsers()) {
				continue;
			}
			cardSystemAttributes.add(a.dbColumnName());
		}
		cardSystemAttributes.add(TableHistory.EndDateAttribute);
	}

	protected final ICard card;

	public CardWrapper(final ICard card) {
		this.card = card;
	}

	@Override
	public final Long getId() {
		return Long.valueOf(card.getId());
	}

	@Override
	public Object getCode() {
		return card.getCode();
	}

	@Override
	public Object getDescription() {
		return card.getDescription();
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
	public final Object get(final String key) {
		try {
			final AttributeValue av = card.getAttributeValue(key);
			return extractAndConvertValue(av);
		} catch (final NotFoundException e) {
			throw new IllegalArgumentException();
		}
	}

	@Override
	public <T> T get(final String key, final Class<? extends T> requiredType) {
		final Object value = get(key);
		return requiredType.cast(value);
	}

	private Object extractAndConvertValue(final AttributeValue av) {
		if (av.isNull()) {
			return null;
		}
		switch (av.getSchema().getType()) {
		case FOREIGNKEY:
			return toCardReference(av.getCard());
		case REFERENCE:
			return toCardReference(av.getReference());
		case LOOKUP:
			return LookupWrapper.newInstance(av.getLookup());
		case DATE:
		case TIME:
		case TIMESTAMP:
			return new DateTime(av.getDate().getTime());
		default:
			return av.getObject();
		}
	}

	private CardReference toCardReference(final Reference ref) {
		final ITable table = UserOperations.from(UserContext.systemContext()).tables().get(ref.getClassId());
		final Long cardId = Long.valueOf(ref.getId());
		return CardReference.newInstance(table.getName(), cardId, ref.getDescription());
	}

	private CardReference toCardReference(final ICard c) {
		final ITable table = c.getSchema();
		final Long cardId = Long.valueOf(c.getId());
		return CardReference.newInstance(table.getName(), cardId, c.getDescription());
	}

	@Override
	public final Iterable<Entry<String, Object>> getValues() {
		return Iterables.transform(getNonSystemAttributeValueMap(),
				new Function<Entry<String, AttributeValue>, Entry<String, Object>>() {

					@Override
					public Entry<String, Object> apply(final Entry<String, AttributeValue> input) {
						return new Entry<String, Object>() {

							@Override
							public String getKey() {
								return input.getKey();
							}

							@Override
							public Object getValue() {
								final AttributeValue av = input.getValue();
								return extractAndConvertValue(av);
							}

							@Override
							public Object setValue(final Object value) {
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
			public boolean apply(final Entry<String, AttributeValue> input) {
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
		setOnly(key, value);
		return this;
	}

	protected final void setOnly(final String key, final Object value) {
		if (isUserAttributeName(key)) {
			card.getAttributeValue(key).setValue(value);
		}
	}

	@Override
	public CMCardDefinition setCode(final Object value) {
		card.setCode((String) value);
		return this;
	}

	@Override
	public CMCardDefinition setDescription(final Object value) {
		card.setDescription((String) value);
		return this;
	}

	@Override
	public CMCard save() {
		card.save();
		return this;
	}

}
