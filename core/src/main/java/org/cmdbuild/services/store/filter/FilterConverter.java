package org.cmdbuild.services.store.filter;

import static org.cmdbuild.services.store.filter.DataViewFilterStore.DESCRIPTION_ATTRIBUTE_NAME;
import static org.cmdbuild.services.store.filter.DataViewFilterStore.ENTRYTYPE_ATTRIBUTE_NAME;
import static org.cmdbuild.services.store.filter.DataViewFilterStore.FILTER_ATTRIBUTE_NAME;
import static org.cmdbuild.services.store.filter.DataViewFilterStore.MASTER_ATTRIBUTE_NAME;
import static org.cmdbuild.services.store.filter.DataViewFilterStore.NAME_ATTRIBUTE_NAME;
import static org.cmdbuild.services.store.filter.DataViewFilterStore.TEMPLATE_ATTRIBUTE_NAME;

import java.util.Map;

import org.cmdbuild.dao.entry.CMCard;
import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.dao.view.CMDataView;
import org.cmdbuild.data.store.dao.BaseStorableConverter;
import org.cmdbuild.services.store.filter.FilterStore.Filter;

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

		return FilterDTO.newFilter() //
				.withId(card.getId()) //
				.withName(card.get(NAME_ATTRIBUTE_NAME, String.class)) //
				.withDescription(card.get(DESCRIPTION_ATTRIBUTE_NAME, String.class)) //
				.forClass(clazz.getIdentifier().getLocalName()) //
				.withValue(card.get(FILTER_ATTRIBUTE_NAME, String.class)) //
				.asTemplate(card.get(TEMPLATE_ATTRIBUTE_NAME, Boolean.class)) //
				.withOwner(Number.class.cast(card.get(MASTER_ATTRIBUTE_NAME, Integer.class)).longValue()) //
				.build();
	}

	@Override
	public Map<String, Object> getValues(final Filter storable) {
		throw new UnsupportedOperationException(
				"This converter must be used only for reading. Writing is not supported.");
	}

}
