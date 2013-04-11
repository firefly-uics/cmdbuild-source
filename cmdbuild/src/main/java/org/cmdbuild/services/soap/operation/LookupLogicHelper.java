package org.cmdbuild.services.soap.operation;

import static com.google.common.collect.FluentIterable.from;
import static org.apache.commons.lang.StringUtils.EMPTY;
import static org.apache.commons.lang.StringUtils.defaultIfEmpty;

import org.cmdbuild.data.store.lookup.LookupDto;
import org.cmdbuild.data.store.lookup.LookupTypeDto;
import org.cmdbuild.logic.data.lookup.LookupLogic;
import org.cmdbuild.services.soap.types.Lookup;
import org.slf4j.Logger;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import com.google.common.base.Function;
import com.google.common.base.Predicate;

public class LookupLogicHelper {

	private static Logger logger = LookupLogic.logger;
	private static Marker marker = MarkerFactory.getMarker(LookupLogicHelper.class.getName());

	private static interface AttributeChecker {

		boolean check(LookupDto input);

	}

	private final LookupLogic logic;

	public LookupLogicHelper(final LookupLogic lookupLogic) {
		this.logic = lookupLogic;
	}

	public int createLookup(final Lookup lookup) {
		final LookupDto lookupDto = transform(lookup);
		return logic.createOrUpdateLookup(lookupDto).intValue();
	}

	public boolean updateLookup(final Lookup lookup) {
		final LookupDto lookupDto = transform(lookup);
		logic.createOrUpdateLookup(lookupDto).intValue();
		return true;
	}

	public boolean disableLookup(final int id) {
		logic.disableLookup(Long.valueOf(id));
		return true;
	}

	public Lookup getLookupById(final int id) {
		final LookupDto lookup = logic.getLookup(Long.valueOf(id));
		return transform(lookup, true);
	}

	public Lookup[] getLookupListByCode(final String type, final String code, final boolean parentList) {
		return getLookupListByAttribute(type, new AttributeChecker() {
			@Override
			public boolean check(final LookupDto input) {
				return code.equals(input.code);
			}
		}, parentList);
	}

	public Lookup[] getLookupListByDescription(final String type, final String description, final boolean parentList) {
		return getLookupListByAttribute(type, new AttributeChecker() {
			@Override
			public boolean check(final LookupDto input) {
				return description.equals(input.description);
			}
		}, parentList);
	}

	private Lookup[] getLookupListByAttribute(final String type, final AttributeChecker attributeChecker,
			final boolean parentList) {
		final LookupTypeDto lookupType = LookupTypeDto.newInstance() //
				.withName(type) //
				.build();
		final Iterable<LookupDto> lookupList = logic.getAllLookup(lookupType, true, 0, Integer.MAX_VALUE);
		return from(lookupList) //
				.filter(new Predicate<LookupDto>() {
					@Override
					public boolean apply(final LookupDto input) {
						return attributeChecker.check(input);
					}
				}) //
				.transform(new Function<LookupDto, Lookup>() {
					@Override
					public Lookup apply(final LookupDto input) {
						return transform(input, parentList);
					}
				}) //
				.toArray(Lookup.class);
	}

	private LookupDto transform(final Lookup from) {
		return LookupDto.newInstance() //
				.withType(LookupTypeDto.newInstance()//
						.withName(from.getType())) //
				.withCode(defaultIfEmpty(from.getCode(), EMPTY)) //
				.withId(Long.valueOf(from.getId())) //
				.withDescription(from.getDescription()) //
				.withNotes(from.getNotes()) //
				.withParentId(Long.valueOf(from.getParentId())) //
				.withNumber(from.getPosition()) //
				.build();
	}

	private Lookup transform(final LookupDto from, final boolean parentList) {
		logger.debug(marker, "serializing lookup '{}'", from);
		final Lookup to = new Lookup();
		to.setId(from.id.intValue());
		to.setCode(from.code);
		to.setDescription(from.description);
		to.setNotes(from.notes);
		to.setType(from.type.name);
		to.setPosition(from.number);
		if (from.parent != null) {
			to.setParentId(from.parentId.intValue());
		}
		if (parentList && from.parent != null) {
			to.setParent(transform(from.parent, true));
		}
		return to;
	}

}
