package org.cmdbuild.logic.data.lookup;

import static com.google.common.collect.Maps.filterValues;

import java.util.Map;

import org.cmdbuild.dao.entry.CMCard;
import org.cmdbuild.services.store.DataViewStore.BaseStorableConverter;

import com.google.common.base.Predicate;
import com.google.common.collect.Maps;

public class LookupStorableConverter extends BaseStorableConverter<LookupDto> {

	public static final String LOOKUP_TABLE_NAME = "LookUp";

	@Override
	public String getClassName() {
		return LOOKUP_TABLE_NAME;
	}

	@Override
	public LookupDto convert(final CMCard card) {
		return LookupDto.newInstance() //
				.withId(card.getId()) //
				.withCode((String) card.getCode()) //
				.withDescription((String) card.getDescription()) //
				.withNotes(card.get("Notes", String.class)) //
				.withType(LookupTypeDto.newInstance() //
						.withName(card.get("Type", String.class)) //
						.withParent(card.get("ParentType", String.class))) //
				.withNumber(card.get("Number", Integer.class)) //
				.withActiveStatus(card.get("Active", Boolean.class)) //
				.withDefaultStatus(card.get("IsDefault", Boolean.class)) //
				.withParentId(safeIntegerToLong(card.get("ParentId", Integer.class), Long.class)) //
				.build();
	}

	private static Long safeIntegerToLong(final Integer from, final Class<Long> toClass) {
		return (from == null) ? null : Long.valueOf(from);
	}

	@Override
	public Map<String, Object> getValues(final LookupDto storable) {
		final Map<String, Object> values = Maps.newHashMap();
		values.put("Code", storable.code);
		values.put("Description", storable.description);
		values.put("Notes", storable.notes);
		values.put("Type", storable.type.name);
		values.put("ParentType", storable.type.parent);
		values.put("Number", storable.number);
		values.put("Active", storable.active);
		values.put("IsDefault", storable.isDefault);
		values.put("ParentId", storable.parentId);
		return filterValues(values, new Predicate<Object>() {
			@Override
			public boolean apply(final Object input) {
				return (input != null);
			};
		});
	}

}
