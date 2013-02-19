package org.cmdbuild.services.store;

import static org.cmdbuild.dao.query.clause.AnyAttribute.anyAttribute;
import static org.cmdbuild.dao.query.clause.QueryAliasAttribute.attribute;
import static org.cmdbuild.dao.query.clause.where.EqualsOperatorAndValue.eq;
import static org.cmdbuild.dao.query.clause.where.SimpleWhereClause.condition;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang.Validate;
import org.cmdbuild.dao.entry.CMCard;
import org.cmdbuild.dao.entry.CMCard.CMCardDefinition;
import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.dao.query.CMQueryResult;
import org.cmdbuild.dao.query.CMQueryRow;
import org.cmdbuild.dao.view.CMDataView;
import org.cmdbuild.services.store.Store.Storable;

import com.google.common.collect.Lists;

public class DataViewStore<T extends Storable> implements Store<T> {
	
	public static interface StorableConverter<T extends Storable> {

		/**
		 * 
		 * @return the name of the class in the store.
		 */
		String getClassName();

		T convert(CMCard card);

		/**
		 * Converts a generic type into a map of <String, Object>, corresponding to
		 * attribute <name, value>
		 * 
		 * @param storable
		 * @return
		 */
		Map<String, Object> getValues(T storable);

	}

	

	private final CMDataView view;
	private final StorableConverter<T> converter;

	public DataViewStore(final CMDataView view, final StorableConverter<T> converter) {
		this.view = view;
		this.converter = converter;
	}

	@Override
	public void create(final T storable) {
		final Map<String, Object> map = converter.getValues(storable);
		final String className = converter.getClassName();
		final CMClass storeClass = view.findClass(className);
		final CMCardDefinition cardToCreate = view.createCardFor(storeClass);
		for (final Entry<String, Object> entry : map.entrySet()) {
			cardToCreate.set(entry.getKey(), entry.getValue());
		}
		cardToCreate.save();
	}

	@Override
	public void update(final T storable) {
		final Map<String, Object> map = converter.getValues(storable);
		final Long cardId = storable.getIdentifier();
		final CMCard fetchedCard = findCard(cardId, converter.getClassName());
		final CMCardDefinition cardToUpdate = view.update(fetchedCard);
		for (final Entry<String, Object> entry : map.entrySet()) {
			cardToUpdate.set(entry.getKey(), entry.getValue());
		}
		cardToUpdate.save();
	}

	private CMCard findCard(final Long cardId, final String className) {
		Validate.isTrue(cardId > 0);
		final CMClass storeClass = view.findClass(className);
		final CMQueryRow row = view.select(anyAttribute(storeClass)) //
				.from(storeClass) //
				.where(condition(attribute(storeClass, "Id"), eq(cardId))) //
				.run().getOnlyRow();
		final CMCard fetchedCard = row.getCard(storeClass);
		return fetchedCard;
	}

	@Override
	public void delete(final T storable) {
		final Long cardId = storable.getIdentifier();
		final CMCard cardToDelete = findCard(cardId, converter.getClassName());
		view.delete(cardToDelete);
	}

	@Override
	public T read(final Long identifier) {
		final CMCard fetchedCard = findCard(identifier, converter.getClassName());
		Validate.notNull(fetchedCard);
		return converter.convert(fetchedCard);
	}

	@Override
	public List<T> list() {
		final List<T> list = Lists.newArrayList();
		final CMClass clazz = view.findClass(converter.getClassName());
		final CMQueryResult result = view.select(anyAttribute(clazz)) //
				.from(clazz) //
				.run();
		for (final CMQueryRow row : result) {
			list.add(converter.convert(row.getCard(clazz)));
		}
		return list;
	}

}
