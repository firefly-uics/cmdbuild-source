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
import org.cmdbuild.data.converter.StorableConverter;
import org.cmdbuild.services.store.Store.Storable;

import com.google.common.collect.Lists;

public class DataViewStore<T extends Storable> implements Store<T> {

	private final CMDataView view;
	private final StorableConverter<T> converter;

	public DataViewStore(CMDataView view, final StorableConverter<T> converter) {
		this.view = view;
		this.converter = converter;
	}

	@Override
	public void create(T storable) {
		Map<String, Object> map = converter.getValues(storable);
		String className = converter.getClassName();
		CMClass storeClass = view.findClass(className);
		CMCardDefinition cardToCreate = view.createCardFor(storeClass);
		for (Entry<String, Object> entry : map.entrySet()) {
			cardToCreate.set(entry.getKey(), entry.getValue());
		}
		cardToCreate.save();
	}

	@Override
	public void update(T storable) {
		Map<String, Object> map = converter.getValues(storable);
		Long cardId = storable.getIdentifier();
		CMCard fetchedCard = findCard(cardId, converter.getClassName());
		CMCardDefinition cardToUpdate = view.update(fetchedCard);
		for (Entry<String, Object> entry : map.entrySet()) {
			cardToUpdate.set(entry.getKey(), entry.getValue());
		}
		cardToUpdate.save();
	}

	private CMCard findCard(Long cardId, String className) {
		Validate.isTrue(cardId > 0);
		CMClass storeClass = view.findClass(className);
		CMQueryRow row = view.select(anyAttribute(storeClass)) //
				.from(storeClass) //
				.where(condition(attribute(storeClass, "Id"), eq(cardId))) //
				.run().getOnlyRow();
		CMCard fetchedCard = row.getCard(storeClass);
		return fetchedCard;
	}

	@Override
	public void delete(T storable) {
		Long cardId = storable.getIdentifier();
		CMCard cardToDelete = findCard(cardId, converter.getClassName());
		view.delete(cardToDelete);
	}

	@Override
	public T read(Long identifier) {
		CMCard fetchedCard = findCard(identifier, converter.getClassName());
		Validate.notNull(fetchedCard);
		return converter.convert(fetchedCard);
	}

	@Override
	public List<T> list() {
		List<T> list = Lists.newArrayList();
		CMClass clazz = view.findClass(converter.getClassName());
		CMQueryResult result = view.select(anyAttribute(clazz)) //
				.from(clazz) //
				.run();
		for (CMQueryRow row : result) {
			list.add(converter.convert(row.getCard(clazz)));
		}
		return list;
	}

}
