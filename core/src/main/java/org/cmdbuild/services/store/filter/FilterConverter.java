package org.cmdbuild.services.store.filter;

import static com.google.common.collect.Maps.newHashMap;
import static org.cmdbuild.common.Constants.CODE_ATTRIBUTE;
import static org.cmdbuild.common.Constants.DESCRIPTION_ATTRIBUTE;
import static org.cmdbuild.common.Constants.ID_ATTRIBUTE;

import java.util.Map;

import org.cmdbuild.dao.entry.CMCard;
import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.dao.view.CMDataView;
import org.cmdbuild.data.store.dao.BaseStorableConverter;
import org.cmdbuild.services.store.filter.FilterStore.Filter;

public class FilterConverter extends BaseStorableConverter<Filter> {

	public static final String CLASS_NAME = "_Filter";

	public static final String ID = ID_ATTRIBUTE;
	public static final String NAME = CODE_ATTRIBUTE;
	public static final String DESCRIPTION = DESCRIPTION_ATTRIBUTE;
	public static final String ENTRYTYPE = "IdSourceClass";
	public static final String FILTER = "Filter";
	public static final String FOR_GROUP = "Template";
	public static final String OWNER = "IdOwner";

	private final CMDataView dataView;

	public FilterConverter(final CMDataView dataView) {
		this.dataView = dataView;
	}

	@Override
	public String getClassName() {
		return CLASS_NAME;
	}

	@Override
	public Filter convert(final CMCard card) {
		final Long classId = card.get(ENTRYTYPE, Long.class);
		final CMClass clazz = dataView.findClass(classId);
		return FilterDTO.newFilter() //
				.withId(card.getId()) //
				.withName(card.get(NAME, String.class)) //
				.withDescription(card.get(DESCRIPTION, String.class)) //
				.forClass(clazz.getIdentifier().getLocalName()) //
				.withValue(card.get(FILTER, String.class)) //
				.asTemplate(card.get(FOR_GROUP, Boolean.class)) //
				.withOwner(Number.class.cast(card.get(OWNER, Integer.class)).longValue()) //
				.build();
	}

	@Override
	public Map<String, Object> getValues(final Filter storable) {
		final Map<String, Object> values = newHashMap();
		values.put(NAME, storable.getName());
		values.put(DESCRIPTION, storable.getDescription());
		values.put(ENTRYTYPE, dataView.findClass(storable.getClassName()).getId());
		values.put(FILTER, storable.getValue());
		values.put(FOR_GROUP, storable.isTemplate());
		values.put(OWNER, storable.getOwner());
		return values;
	}

}
