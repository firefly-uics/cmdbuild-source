package org.cmdbuild.services.store;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Lists.transform;
import static org.cmdbuild.dao.query.clause.AnyAttribute.anyAttribute;
import static org.cmdbuild.dao.query.clause.QueryAliasAttribute.attribute;
import static org.cmdbuild.dao.query.clause.where.AndWhereClause.and;
import static org.cmdbuild.dao.query.clause.where.EqualsOperatorAndValue.eq;
import static org.cmdbuild.dao.query.clause.where.SimpleWhereClause.condition;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.cmdbuild.dao.entry.CMCard;
import org.cmdbuild.dao.entry.CMCard.CMCardDefinition;
import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.dao.query.CMQueryResult;
import org.cmdbuild.dao.query.CMQueryRow;
import org.cmdbuild.dao.query.QuerySpecsBuilder;
import org.cmdbuild.dao.query.clause.where.WhereClause;
import org.cmdbuild.dao.view.CMDataView;
import org.cmdbuild.services.store.Store.Storable;

import com.google.common.base.Function;

public class DataViewStore<T extends Storable> implements Store<T> {

	private static final String DEFAULT_IDENTIFIER_ATTRIBUTE_NAME = "Id";

	private static final WhereClause NO_GROUP_WHERE_CLAUSE = null;

	public static interface StorableConverter<T extends Storable> {

		/**
		 * @return the name of the class in the store.
		 */
		String getClassName();

		/**
		 * Returns the name of the attribute that represents the group of the
		 * {@link Storable} objects, {@code null} if there is no grouping.
		 * Within a group the identifier must be unique. Implies a restriction
		 * over the {@link Store#read(Storable)}, {@link Store#update(Storable)}
		 * and {@link Store#list()} methods.
		 * 
		 * @return the name of the attribute or {@code null}.
		 */
		String getGroupAttributeName();

		/**
		 * Returns the name of the group. See
		 * {@link StorableConverter#getGroupAttributeName()}.
		 * 
		 * @return the name of the group.
		 */
		String getGroupAttributeValue();

		/**
		 * @return the name of the identifier attribute.
		 */
		String getIdentifierAttributeName();

		/**
		 * Converts a card into a {@link Storable}.
		 * 
		 * @param card
		 *            the cards that needs to be converted.
		 * 
		 * @return the instance of {@link Storable} representing the card.
		 */
		Storable storableOf(CMCard card);

		/**
		 * Converts a card into a {@link T}.
		 * 
		 * @param card
		 *            the cards that needs to be converted.
		 * 
		 * @return the instance of {@link T} representing the card.
		 */
		T convert(CMCard card);

		/**
		 * Converts a generic type into a map of <String, Object>, corresponding
		 * to attribute <name, value>
		 * 
		 * @param storable
		 * @return
		 */
		Map<String, Object> getValues(T storable);

	}

	public static abstract class BaseStorableConverter<T extends Storable> implements StorableConverter<T> {

		@Override
		public final String getGroupAttributeName() {
			return null;
		}

		@Override
		public final String getGroupAttributeValue() {
			return null;
		}

		@Override
		public String getIdentifierAttributeName() {
			return DEFAULT_IDENTIFIER_ATTRIBUTE_NAME;
		}

		@Override
		public Storable storableOf(final CMCard card) {
			return new Storable() {

				@Override
				public String getIdentifier() {
					final String attributeName = getIdentifierAttributeName();
					final String value;
					if (DEFAULT_IDENTIFIER_ATTRIBUTE_NAME.equals(attributeName)) {
						value = Long.toString(card.getId());
					} else {
						value = card.get(getIdentifierAttributeName(), String.class);
					}
					return value;
				}

			};
		}

	}

	public static <T extends Storable> DataViewStore<T> newInstance(final CMDataView view,
			final StorableConverter<T> converter) {
		return new DataViewStore<T>(view, converter);
	}

	private final CMDataView view;
	private final StorableConverter<T> converter;

	public DataViewStore(final CMDataView view, final StorableConverter<T> converter) {
		this.view = view;
		this.converter = converter;
	}

	@Override
	public Storable create(final T storable) {
		final String className = converter.getClassName();
		final CMClass storeClass = view.findClass(className);
		final CMCardDefinition cardToCreate = view.createCardFor(storeClass);
		final Map<String, Object> map = converter.getValues(storable);
		for (final Entry<String, Object> entry : map.entrySet()) {
			cardToCreate.set(entry.getKey(), entry.getValue());
		}
		return converter.storableOf(cardToCreate.save());
	}

	@Override
	public T read(final Storable storable) {
		final CMCard fetchedCard = findCard(storable);
		return converter.convert(fetchedCard);
	}

	@Override
	public void update(final T storable) {
		final CMCard fetchedCard = findCard(storable);
		final CMCardDefinition cardToUpdate = view.update(fetchedCard);
		final Map<String, Object> map = converter.getValues(storable);
		for (final Entry<String, Object> entry : map.entrySet()) {
			cardToUpdate.set(entry.getKey(), entry.getValue());
		}
		cardToUpdate.save();
	}

	@Override
	public void delete(final Storable storable) {
		final CMCard cardToDelete = findCard(storable);
		view.delete(cardToDelete);
	}

	@Override
	public List<T> list() {
		final CMClass storeClass = view.findClass(converter.getClassName());
		final QuerySpecsBuilder querySpecsBuilder = view //
				.select(anyAttribute(storeClass)) //
				.from(storeClass);
		final WhereClause clause = groupWhereClause(storeClass);
		if (clause != NO_GROUP_WHERE_CLAUSE) {
			querySpecsBuilder.where(clause);
		}
		final CMQueryResult result = querySpecsBuilder.run();

		final List<T> list = transform(newArrayList(result), new Function<CMQueryRow, T>() {
			@Override
			public T apply(final CMQueryRow input) {
				return converter.convert(input.getCard(storeClass));
			}
		});
		return list;
	}

	private WhereClause groupWhereClause(final CMClass storeClass) {
		final WhereClause clause;
		final String groupAttributeName = converter.getGroupAttributeName();
		if (groupAttributeName != null) {
			final String groupAttributeValue = converter.getGroupAttributeValue();
			clause = condition(attribute(storeClass, groupAttributeName), eq(groupAttributeValue));
		} else {
			clause = NO_GROUP_WHERE_CLAUSE;
		}
		return clause;
	}

	/**
	 * Returns the {@link CMCard} corresponding to the {@link Storable} object.<br>
	 * 
	 * Override this if the {@link Storable#getIdentifier()} does not represent
	 * the card's id.
	 * 
	 * @param storable
	 *            the storable object.
	 * 
	 * @return the
	 * 
	 */
	private CMCard findCard(final Storable storable) {
		final CMClass storeClass = view.findClass(converter.getClassName());
		final CMQueryRow row = view.select(anyAttribute(storeClass)) //
				.from(storeClass) //
				.where(specificWhereClause(storeClass, storable)) //
				.run() //
				.getOnlyRow();
		final CMCard fetchedCard = row.getCard(storeClass);
		return fetchedCard;
	}

	private WhereClause specificWhereClause(final CMClass storeClass, final Storable storable) {
		final WhereClause groupWhereClause = groupWhereClause(storeClass);

		String identifierAttributeName = converter.getIdentifierAttributeName();
		if (identifierAttributeName == null) {
			identifierAttributeName = DEFAULT_IDENTIFIER_ATTRIBUTE_NAME;
		}

		final Object identifierAttributeValue;
		if (identifierAttributeName == DEFAULT_IDENTIFIER_ATTRIBUTE_NAME) {
			identifierAttributeValue = Long.parseLong(storable.getIdentifier());
		} else {
			identifierAttributeValue = storable.getIdentifier();
		}

		final WhereClause identifierWhereClause = condition(attribute(storeClass, identifierAttributeName),
				eq(identifierAttributeValue));
		return (groupWhereClause == null) ? identifierWhereClause : and(groupWhereClause, identifierWhereClause);
	}

}
