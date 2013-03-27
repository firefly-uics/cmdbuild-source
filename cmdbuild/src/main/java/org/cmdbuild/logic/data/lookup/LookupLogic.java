package org.cmdbuild.logic.data.lookup;

import static com.google.common.collect.FluentIterable.from;
import static com.google.common.collect.Iterables.size;
import static com.google.common.collect.Lists.newArrayList;
import static java.util.Arrays.asList;
import static java.util.Collections.sort;
import static org.cmdbuild.logic.data.lookup.Util.actives;
import static org.cmdbuild.logic.data.lookup.Util.limited;
import static org.cmdbuild.logic.data.lookup.Util.toLookupType;
import static org.cmdbuild.logic.data.lookup.Util.typesWith;
import static org.cmdbuild.logic.data.lookup.Util.uniques;
import static org.cmdbuild.logic.data.lookup.Util.withId;
import static org.cmdbuild.logic.data.lookup.Util.withType;

import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.cmdbuild.dao.entrytype.CMAttribute;
import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.dao.entrytype.attributetype.LookupAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.NullAttributeTypeVisitor;
import org.cmdbuild.dao.view.CMDataView;
import org.cmdbuild.exception.NotFoundException.NotFoundExceptionType;
import org.cmdbuild.exception.ORMException.ORMExceptionType;
import org.cmdbuild.logic.Logic;
import org.cmdbuild.logic.TemporaryObjectsBeforeSpringDI;
import org.cmdbuild.logic.data.DataDefinitionLogic;
import org.cmdbuild.logic.data.access.DataAccessLogic;
import org.cmdbuild.model.data.Attribute;
import org.cmdbuild.services.store.DataViewStore;
import org.cmdbuild.services.store.DataViewStore.StorableConverter;
import org.cmdbuild.services.store.Store;

import com.google.common.base.Predicate;
import com.google.common.collect.Maps;

public class LookupLogic implements Logic {

	private static final StorableConverter<LookupDto> LOOKUP_STORABLE_CONVERTER = new LookupStorableConverter();

	private static final Comparator<LookupDto> NUMBER_COMPARATOR = new Comparator<LookupDto>() {
		@Override
		public int compare(final LookupDto o1, final LookupDto o2) {
			if (o1.number > o2.number) {
				return 1;
			} else if (o1.number < o2.number) {
				return -1;
			}
			return 0;
		}
	};

	private final Store<LookupDto> store;

	public LookupLogic(final CMDataView view) {
		this.store = new DataViewStore<LookupDto>(view, LOOKUP_STORABLE_CONVERTER);
	}

	public Iterable<LookupTypeDto> getAllTypes() {
		logger.info("getting all lookup types");
		return from(store.list()) //
				.transform(toLookupType()) //
				.filter(uniques());
	}

	public void saveLookupType(final LookupTypeDto newType, final LookupTypeDto oldType) {
		logger.info("saving lookup type, new is '{}', old is '{}'", newType, oldType);
		final LookupTypeDto existingLookupType = typeForNameAndParent(oldType.name, oldType.parent);
		if (existingLookupType == null) {
			logger.info("old one not specified, creating a new one");
			final LookupDto lookup = LookupDto.newInstance() //
					.withType(newType) //
					.build();
			store.create(lookup);
		} else {
			logger.info("old one specified, modifying existing one");
			for (final LookupDto lookup : listForType(oldType)) {
				final LookupDto newLookup = LookupDto.newInstance() //
						.withId(lookup.id) //
						.withCode(lookup.code) //
						.withDescription(lookup.description) //
						.withType(newType) //
						.withNumber(lookup.number) //
						.withActiveStatus(lookup.active) //
						.withDefaultStatus(lookup.isDefault) //
						.build();
				store.update(newLookup);
			}

			logger.info("updates existing classes' attributes");
			final DataAccessLogic dataAccessLogic = TemporaryObjectsBeforeSpringDI.getSystemDataAccessLogic();
			final DataDefinitionLogic dataDefinitionLogic = TemporaryObjectsBeforeSpringDI.getDataDefinitionLogic();

			for (final CMClass existingClass : dataAccessLogic.findAllClasses()) {
				for (final CMAttribute existingAttribute : existingClass.getAttributes()) {
					existingAttribute.getType().accept(new NullAttributeTypeVisitor() {
						@Override
						public void visit(final LookupAttributeType attributeType) {
							if (asList(oldType.name, newType.name).contains(attributeType.getLookupTypeName())) {
								final Attribute attribute = Attribute.newAttribute() //
										.withName(existingAttribute.getName()) //
										.withOwner(existingAttribute.getOwner().getName()) //
										.withDescription(existingAttribute.getDescription()) //
										.withGroup(existingAttribute.getGroup()) //
										.withType("LOOKUP") //
										.withLookupType(newType.name) //
										.withMode(existingAttribute.getMode()) //
										.withEditorType(existingAttribute.getEditorType()) //
										.withForeignKeyDestinationClassName( //
												existingAttribute.getForeignKeyDestinationClassName()) //
										.thatIsDisplayableInList(existingAttribute.isDisplayableInList()) //
										.thatIsMandatory(existingAttribute.isMandatory()) //
										.thatIsUnique(existingAttribute.isUnique()) //
										.thatIsActive(existingAttribute.isActive()) //
										.build();
								dataDefinitionLogic.createOrUpdate(attribute);
							}
						}
					});
				}
			}
		}
	}

	public Iterable<LookupDto> getAllLookup(final LookupTypeDto type, final boolean activeOnly, final int start,
			final int limit) {
		logger.info("getting all lookups for type '{}'", type);
		final Iterable<LookupDto> elements = listForType(type);

		if (!elements.iterator().hasNext()) {
			logger.error("no lookup was found for type '{}'", type);
			throw NotFoundExceptionType.LOOKUP_TYPE_NOTFOUND.createException(type.name);
		}

		final List<LookupDto> list = newArrayList(elements);

		logger.debug("ordering elements");
		sort(list, NUMBER_COMPARATOR);

		return from(list) //
				.filter(actives(activeOnly)) //
				.filter(limited(start, limit));
	}

	public Iterable<LookupDto> getAllLookupOfParent(final LookupTypeDto type) {
		logger.info("getting all lookups for the parent of type '{}'", type);
		final LookupTypeDto current = typeFor(typesWith(type.name));
		final LookupTypeDto parent = LookupTypeDto.newInstance() //
				.withName(current.parent) //
				.build();
		return listForType(parent);
	}

	public void enableLookup(final Long id) {
		logger.info("enabling lookup with id '{}'", id);
		setActiveStatus(true, id);
	}

	public void disableLookup(final Long id) {
		logger.info("disabling lookup with id '{}'", id);
		setActiveStatus(false, id);
	}

	private void setActiveStatus(final boolean status, final Long id) {
		logger.info("setting active status '{}' for lookup with id '{}'", status, id);
		if (id <= 0) {
			logger.warn("invalid id '{}', exiting without doing nothing", id);
			return;
		}

		logger.debug("getting lookup with id '{}'", id);
		final Iterator<LookupDto> shouldBeOneOnly = from(store.list()) //
				.filter(withId(id)) //
				.iterator();

		if (!shouldBeOneOnly.hasNext()) {
			throw NotFoundExceptionType.LOOKUP_NOTFOUND.createException(id.toString());
		}

		logger.debug("updating lookup active to '{}'", status);
		final LookupDto lookup = LookupDto.newInstance() //
				.clone(shouldBeOneOnly.next()) //
				.withActiveStatus(status) //
				.build();

		store.update(lookup);
	}

	private LookupTypeDto typeForNameAndParent(final String name, final String parent) {
		logger.info("getting lookup type with name '{}' and parent '{}'", name, parent);
		return typeFor(typesWith(name, parent));
	}

	private LookupTypeDto typeFor(final Predicate<LookupTypeDto> predicate) {
		logger.info("getting lookup type for predicate");
		final Iterator<LookupTypeDto> shouldBeOneOnly = from(getAllTypes()) //
				.filter(predicate) //
				.iterator();
		final LookupTypeDto found;
		if (!shouldBeOneOnly.hasNext()) {
			logger.warn("lookup type not found");
			found = null;
		} else {
			logger.warn("lookup type successfully found");
			found = shouldBeOneOnly.next();
		}
		if ((found != null) && shouldBeOneOnly.hasNext()) {
			logger.error("more than one lookup has been found");
			throw ORMExceptionType.ORM_GENERIC_ERROR.createException();
		}
		return found;
	}

	public void createOrUpdateLookup(final LookupDto lookup) {
		if (lookup.id == null || lookup.id <= 0) {
			logger.info("creating lookup '{}'", lookup);

			final LookupDto toBeCreated;

			logger.debug("checking lookup number ('{}'), if not valid assigning a valid one", lookup.number);
			if (lookup.number == null || lookup.number <= 0) {
				final int count = size(listForType(lookup.type));
				toBeCreated = LookupDto.newInstance() //
						.clone(lookup) //
						.withNumber(count + 1) //
						.build();
			} else {
				toBeCreated = lookup;
			}

			store.create(toBeCreated);
		} else {
			logger.info("updating lookup '{}'", lookup);
			store.update(lookup);
		}
	}

	/**
	 * Reorders lookups.
	 * 
	 * @param lookupType
	 *            the lookup's type of elements that must be ordered.
	 * @param positions
	 *            the positions of the elements; key is the id of the lookup
	 *            element, value is the new index.
	 */
	public void reorderLookup(final LookupTypeDto lookupType, final Map<Long, Integer> positions) {
		logger.info("reordering lookups for type '{}'", lookupType);

		final Iterable<LookupDto> lookups = listForType(lookupType);
		for (final LookupDto lookup : lookups) {
			if (positions.containsKey(lookup.id)) {
				final int index = positions.get(lookup.id);
				final LookupDto updated = LookupDto.newInstance() //
						.clone(lookup) //
						.withNumber(index) //
						.build();
				store.update(updated);
			}
		}
	}

	private Iterable<LookupDto> listForType(final LookupTypeDto type) {
		logger.debug("getting lookups with type '{}'", type);

		final Iterable<LookupDto> lookups = store.list();

		final Map<Long, LookupDto> lookupsById = Maps.newHashMap();
		for (final LookupDto lookup : lookups) {
			lookupsById.put(lookup.id, lookup);
		}

		for (final LookupDto lookup : lookups) {
			final LookupDto lookupWithParent = buildLookupWithParentLookup(lookup, lookupsById);
			lookupsById.put(lookupWithParent.id, lookupWithParent);
		}

		return from(lookupsById.values()) //
				.filter(withType(type));
	}

	private LookupDto buildLookupWithParentLookup(final LookupDto lookup, final Map<Long, LookupDto> lookupsById) {
		final LookupDto lookupWithParent;
		final LookupDto parent = lookupsById.get(lookup.parentId);
		if (parent != null) {
			final Long grandparentId = parent.parentId;
			final LookupDto parentWithGrandparent;
			if (grandparentId != null) {
				parentWithGrandparent = buildLookupWithParentLookup(parent, lookupsById);
			} else {
				parentWithGrandparent = parent;
			}
			lookupWithParent = LookupDto.newInstance() //
					.clone(lookup) //
					.withParent(parentWithGrandparent) //
					.build();
		} else {
			lookupWithParent = lookup;
		}
		return lookupWithParent;
	}

}
