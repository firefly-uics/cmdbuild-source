package org.cmdbuild.data.store;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Lists.transform;
import static org.cmdbuild.dao.driver.postgres.Const.ID_ATTRIBUTE;
import static org.cmdbuild.dao.query.clause.AnyAttribute.anyAttribute;
import static org.cmdbuild.dao.query.clause.QueryAliasAttribute.attribute;
import static org.cmdbuild.dao.query.clause.where.AndWhereClause.and;
import static org.cmdbuild.dao.query.clause.where.EqualsOperatorAndValue.eq;
import static org.cmdbuild.dao.query.clause.where.SimpleWhereClause.condition;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.cmdbuild.common.Holder;
import org.cmdbuild.dao.entry.CMCard;
import org.cmdbuild.dao.entry.CMCard.CMCardDefinition;
import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.dao.query.CMQueryResult;
import org.cmdbuild.dao.query.CMQueryRow;
import org.cmdbuild.dao.query.QuerySpecsBuilder;
import org.cmdbuild.dao.query.clause.where.WhereClause;
import org.cmdbuild.dao.view.CMDataView;
import org.cmdbuild.data.store.Store.Storable;
import org.cmdbuild.exception.NotFoundException;
import org.cmdbuild.logic.data.Utils;
import org.slf4j.Logger;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import com.google.common.base.Function;

public class DataViewStore<T extends Storable> implements Store<T> {

	protected static final Marker marker = MarkerFactory.getMarker(DataViewStore.class.getName());

	private static final String DEFAULT_IDENTIFIER_ATTRIBUTE_NAME = ID_ATTRIBUTE;

	private static final WhereClause NO_GROUP_WHERE_CLAUSE = null;

	public static interface StorableConverter<T extends Storable> {

		String SYSTEM_USER = "system"; // FIXME

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
		Object getGroupAttributeValue();

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

		String getUser(T storable);

	}

	public static abstract class BaseStorableConverter<T extends Storable> implements StorableConverter<T> {

		protected Logger logger = DataViewStore.logger;

		@Override
		public final String getGroupAttributeName() {
			return null;
		}

		@Override
		public final Object getGroupAttributeValue() {
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

		@Override
		public String getUser(final T storable) {
			return SYSTEM_USER;
		};

		// TODO use static methods directly instead
		protected String readStringAttribute(final CMCard card, final String attributeName) {
			return Utils.readString(card, attributeName);
		}

		// TODO use static methods directly instead
		protected Long readLongAttribute(final CMCard card, final String attributeName) {
			return Utils.readLong(card, attributeName);
		}

	}

	public static <T extends Storable> DataViewStore<T> newInstance(final CMDataView view,
			final StorableConverter<T> converter) {
		return new DataViewStore<T>(view, converter);
	}

	private final CMDataView view;
	private final StorableConverter<T> converter;
	private final Holder<CMClass> storeClass;

	public DataViewStore(final CMDataView view, final StorableConverter<T> converter) {
		this.view = view;
		this.converter = converter;
		this.storeClass = new Holder<CMClass>() {

			private CMClass storeClass;

			@Override
			public CMClass get() {
				logger.debug(marker, "looking for class with name '{}'", converter.getClassName());
				CMClass storeClass = this.storeClass;
				if (storeClass == null) {
					synchronized (this) {
						storeClass = this.storeClass;
						if (storeClass == null) {
							final String className = converter.getClassName();
							this.storeClass = storeClass = view.findClass(className);
							if (this.storeClass == null) {
								logger.error(marker, "class '{}' has not been found", converter.getClassName());
								throw NotFoundException.NotFoundExceptionType.CLASS_NOTFOUND.createException();
							}
						}
					}
				}
				return storeClass;
			}

		};
	}

	@Override
	public Storable create(final T storable) {
		logger.debug(marker, "creating a new storable element");

		logger.trace(marker, "getting data to be stored");
		final String user = converter.getUser(storable);
		final Map<String, Object> values = converter.getValues(storable);

		logger.trace(marker, "filling new card's attributes");
		final CMCardDefinition card = view.createCardFor(storeClass.get());
		fillCard(card, values, user);

		logger.debug(marker, "saving card");
		return converter.storableOf(card.save());
	}

	@Override
	public T read(final Storable storable) {
		logger.info(marker, "reading storable element with identifier '{}'", storable.getIdentifier());

		final CMCard card = findCard(storable);

		logger.debug(marker, "converting card to storable element");
		return converter.convert(card);
	}

	@Override
	public void update(final T storable) {
		logger.debug(marker, "updating storable element with identifier '{}'", storable.getIdentifier());

		logger.trace(marker, "getting data to be stored");
		final String user = converter.getUser(storable);
		final Map<String, Object> values = converter.getValues(storable);

		logger.trace(marker, "filling existing card's attributes");
		final CMCard card = findCard(storable);
		final CMCardDefinition updatedCard = view.update(card);
		fillCard(updatedCard, values, user);

		logger.debug(marker, "saving card");
		updatedCard.save();
	}

	@Override
	public void delete(final Storable storable) {
		logger.debug(marker, "deleting storable element with identifier '{}'", storable.getIdentifier());
		final CMCard cardToDelete = findCard(storable);
		view.delete(cardToDelete);
	}

	@Override
	public List<T> list() {
		logger.debug(marker, "listing all storable elements");
		final QuerySpecsBuilder querySpecsBuilder = view //
				.select(anyAttribute(storeClass.get())) //
				.from(storeClass.get());
		final WhereClause clause = groupWhereClause(storeClass.get());
		if (clause != NO_GROUP_WHERE_CLAUSE) {
			querySpecsBuilder.where(clause);
		}
		final CMQueryResult result = querySpecsBuilder.run();

		final List<T> list = transform(newArrayList(result), new Function<CMQueryRow, T>() {
			@Override
			public T apply(final CMQueryRow input) {
				return converter.convert(input.getCard(storeClass.get()));
			}
		});
		return list;
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
		logger.debug(marker, "looking for storable element with identifier '{}'", storable.getIdentifier());
		final CMQueryRow row = view.select(anyAttribute(storeClass.get())) //
				.from(storeClass.get()) //
				.where(specificWhereClause(storeClass.get(), storable)) //
				.run() //
				.getOnlyRow();
		return row.getCard(storeClass.get());
	}

	private void fillCard(final CMCardDefinition card, final Map<String, Object> values, final String user) {
		logger.debug(marker, "filling card's attributes with values '{}'", values);
		for (final Entry<String, Object> entry : values.entrySet()) {
			logger.debug(marker, "setting attribute '{}' with value '{}'", entry.getKey(), entry.getValue());
			card.set(entry.getKey(), entry.getValue());
		}
		card.setUser(user);
	}

	private WhereClause specificWhereClause(final CMClass storeClass, final Storable storable) {
		logger.debug(marker, "building specific where clause");

		String identifierAttributeName = converter.getIdentifierAttributeName();
		if (identifierAttributeName == null) {
			logger.debug(marker, "identifier attribute not specified, using default one");
			identifierAttributeName = DEFAULT_IDENTIFIER_ATTRIBUTE_NAME;
		}

		final Object identifierAttributeValue;
		if (identifierAttributeName == DEFAULT_IDENTIFIER_ATTRIBUTE_NAME) {
			logger.debug(marker, "using default one identifier attribute, converting to default type");
			identifierAttributeValue = Long.parseLong(storable.getIdentifier());
		} else {
			identifierAttributeValue = storable.getIdentifier();
		}

		final WhereClause identifierWhereClause = condition(attribute(storeClass, identifierAttributeName),
				eq(identifierAttributeValue));

		final WhereClause groupWhereClause = groupWhereClause(storeClass);
		final WhereClause whereClause;
		if (groupWhereClause == null) {
			logger.debug(marker, "grouping not setted, using identifier only");
			whereClause = identifierWhereClause;
		} else {
			logger.debug(marker, "grouping setted, using both identifier and group");
			whereClause = and(groupWhereClause, identifierWhereClause);
		}
		return whereClause;
	}

	private WhereClause groupWhereClause(final CMClass storeClass) {
		logger.debug(marker, "building group where clause");
		final WhereClause clause;
		final String groupAttributeName = converter.getGroupAttributeName();
		if (groupAttributeName != null) {
			logger.debug(marker, "group attribute name is '{}', building where clause", groupAttributeName);
			final Object groupAttributeValue = converter.getGroupAttributeValue();
			clause = condition(attribute(storeClass, groupAttributeName), eq(groupAttributeValue));
		} else {
			logger.debug(marker, "group attribute name not specified");
			clause = NO_GROUP_WHERE_CLAUSE;
		}
		return clause;
	}
}
