package org.cmdbuild.services.store;

import static org.cmdbuild.services.store.DataViewFilterStore.DESCRIPTION_ATTRIBUTE_NAME;
import static org.cmdbuild.services.store.DataViewFilterStore.ENTRYTYPE_ATTRIBUTE_NAME;
import static org.cmdbuild.services.store.DataViewFilterStore.FILTER_ATTRIBUTE_NAME;
import static org.cmdbuild.services.store.DataViewFilterStore.NAME_ATTRIBUTE_NAME;
import static org.cmdbuild.services.store.DataViewFilterStore.TEMPLATE_ATTRIBUTE_NAME;

import java.util.Map;

import org.cmdbuild.dao.entry.CMCard;
import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.dao.view.CMDataView;
import org.cmdbuild.data.store.dao.BaseStorableConverter;
import org.cmdbuild.services.store.FilterStore.Filter;

public class FilterConverter extends BaseStorableConverter<Filter> {

	private final CMDataView dataView;

	public FilterConverter(final CMDataView dataView) {
		this.dataView = dataView;
	}

	@Override
	public String getClassName() {
		return DataViewFilterStore.FILTERS_CLASS_NAME;
	}

	@Override
	public Filter convert(final CMCard card) {

		final Long etr = card.get(ENTRYTYPE_ATTRIBUTE_NAME, Long.class);
		final CMClass clazz = dataView.findClass(etr);

		return FilterDTO.newFilter().withId(card.getId()) //
				.forClass(clazz.getIdentifier().getLocalName()) //
				.withName((String) card.get(NAME_ATTRIBUTE_NAME)) //
				.withDescription((String) card.get(DESCRIPTION_ATTRIBUTE_NAME)) //
				.withValue((String) card.get(FILTER_ATTRIBUTE_NAME)) //
				.asTemplate((Boolean) card.get(TEMPLATE_ATTRIBUTE_NAME)) //
				.build();
	}

	@Override
	public Map<String, Object> getValues(final Filter storable) {
		throw new UnsupportedOperationException(
				"This converter must be used only for reading. Writing is not supported.");
	}

}
