package org.cmdbuild.data.converter;

import java.util.HashMap;
import java.util.Map;

import org.cmdbuild.dao.driver.postgres.query.ColumnMapper.EntryTypeAttribute;
import org.cmdbuild.dao.entry.CMCard;
import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.dao.reference.EntryTypeReference;
import org.cmdbuild.dao.view.CMDataView;
import org.cmdbuild.logic.TemporaryObjectsBeforeSpringDI;
import org.cmdbuild.model.View;
import org.cmdbuild.services.store.DataViewStore.BaseStorableConverter;

public class ViewConverter extends BaseStorableConverter<View> {

	private final static String //
		CLASS_NAME = "_View",
		DESCRIPTION = "Description",
		FILTER = "Filter",
		ID = "id",
		NAME = "Name",
		SOURCE_CLASS = "SourceClass",
		SOURCE_FUNCTION = "SourceFunction",
		TYPE = "Type";

	@Override
	public String getClassName() {
		return CLASS_NAME;
	}

	@Override
	public View convert(CMCard card) {
		final CMDataView systemDataView = TemporaryObjectsBeforeSpringDI.getSystemView();
		final View view = new View();
		final EntryTypeReference reference = (EntryTypeReference) card.get(SOURCE_CLASS);
		if (reference.getId() != null) {
			final CMClass sourceClass = systemDataView.findClass(reference.getId());
			view.setSourceClassName(sourceClass.getName());
		}

		view.setId(card.getId());
		view.setName((String) card.get(NAME));
		view.setDescription((String) card.get(DESCRIPTION));
		view.setSourceFunction((String) card.get(SOURCE_FUNCTION));
		view.setType(View.ViewType.valueOf((String) card.get(TYPE)));
		view.setFilter((String) card.get(FILTER));

		return view;
	}

	@Override
	public Map<String, Object> getValues(View view) {
		final CMDataView systemDataView = TemporaryObjectsBeforeSpringDI.getSystemView();

		final Map<String, Object> values = new HashMap<String, Object>();
		if (View.ViewType.FILTER.equals(view.getType())) {
 			final CMClass sourceClass = systemDataView.findClass(view.getSourceClassName());
			if (sourceClass != null) {
				values.put(SOURCE_CLASS, EntryTypeReference.newInstance(sourceClass.getId()));
			}
		} else {
			values.put(SOURCE_CLASS, null);
		}

		values.put(DESCRIPTION, view.getDescription());
		values.put(FILTER, view.getFilter());
		values.put(ID, view.getId());
		values.put(NAME, view.getName());
		values.put(SOURCE_FUNCTION, view.getSourceFunction());
		values.put(TYPE, view.getType().toString());

		return values;
	}
}
