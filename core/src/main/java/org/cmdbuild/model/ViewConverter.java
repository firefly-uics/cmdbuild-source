package org.cmdbuild.model;

import java.util.HashMap;
import java.util.Map;

import org.cmdbuild.dao.entry.CMCard;
import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.dao.view.CMDataView;
import org.cmdbuild.data.store.dao.BaseStorableConverter;

public class ViewConverter extends BaseStorableConverter<_View> {

	public static final String VIEW_CLASS_NAME = "_View";

	private final static String //
			DESCRIPTION = "Description",
			FILTER = "Filter", ID = "id", NAME = "Name",
			SOURCE_CLASS = "IdSourceClass",
			SOURCE_FUNCTION = "SourceFunction", TYPE = "Type";

	private final CMDataView dataView;

	public ViewConverter(final CMDataView dataView) {
		this.dataView = dataView;
	}

	@Override
	public String getClassName() {
		return VIEW_CLASS_NAME;
	}

	@Override
	public _View convert(final CMCard card) {
		final _View view = new View();
		final Long reference = card.get(SOURCE_CLASS, Long.class);
		if (reference != null) {
			final CMClass sourceClass = dataView.findClass(reference);
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
	public Map<String, Object> getValues(final _View view) {
		final Map<String, Object> values = new HashMap<String, Object>();
		if (View.ViewType.FILTER.equals(view.getType())) {
			final CMClass sourceClass = dataView.findClass(view.getSourceClassName());
			if (sourceClass != null) {
				values.put(SOURCE_CLASS, sourceClass.getId());
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
