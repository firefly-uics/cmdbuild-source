package org.cmdbuild.logic.data.lookup;

import static com.google.common.collect.FluentIterable.from;
import static com.google.common.collect.Iterables.size;
import static com.google.common.collect.Lists.newArrayList;
import static java.util.Arrays.asList;
import static java.util.Collections.sort;
import static org.apache.commons.lang.StringUtils.isBlank;
import static org.cmdbuild.exception.ORMException.ORMExceptionType.ORM_CHANGE_LOOKUPTYPE_ERROR;
import static org.cmdbuild.logic.PrivilegeUtils.assure;
import static org.cmdbuild.logic.data.lookup.Util.actives;
import static org.cmdbuild.logic.data.lookup.Util.limited;
import static org.cmdbuild.logic.data.lookup.Util.toLookupType;
import static org.cmdbuild.logic.data.lookup.Util.typesWith;
import static org.cmdbuild.logic.data.lookup.Util.uniques;
import static org.cmdbuild.logic.data.lookup.Util.withId;

import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.cmdbuild.auth.user.OperationUser;
import org.cmdbuild.dao.entrytype.CMAttribute;
import org.cmdbuild.dao.entrytype.CMAttribute.Mode;
import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.dao.entrytype.CMEntryType;
import org.cmdbuild.dao.entrytype.attributetype.CMAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.LookupAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.NullAttributeTypeVisitor;
import org.cmdbuild.dao.view.CMAttributeDefinition;
import org.cmdbuild.dao.view.CMDataView;
import org.cmdbuild.data.store.Store.Storable;
import org.cmdbuild.data.store.lookup.LookupDto;
import org.cmdbuild.data.store.lookup.LookupStore;
import org.cmdbuild.data.store.lookup.LookupTypeDto;
import org.cmdbuild.exception.NotFoundException;
import org.cmdbuild.exception.NotFoundException.NotFoundExceptionType;
import org.cmdbuild.exception.ORMException;
import org.cmdbuild.exception.ORMException.ORMExceptionType;
import org.cmdbuild.logic.Logic;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import com.google.common.base.Predicate;

public class LookupLogic implements Logic {

	private static final Marker marker = MarkerFactory.getMarker(LookupLogic.class.getName());

	private static class Exceptions {

		private Exceptions() {
			// prevents instantiation
		}

		public static NotFoundException lookupTypeNotFound(final LookupTypeDto type) {
			return NotFoundExceptionType.LOOKUP_TYPE_NOTFOUND.createException(type.name);
		}

		public static NotFoundException lookupNotFound(final Long id) {
			return NotFoundExceptionType.LOOKUP_NOTFOUND.createException(id.toString());
		}

		public static ORMException multipleElementsWithSameId() {
			return ORMExceptionType.ORM_UNIQUE_VIOLATION.createException();
		}

	}

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

	private final LookupStore store;
	private final OperationUser operationUser;
	private final CMDataView dataView;

	public LookupLogic(final LookupStore store, final OperationUser operationUser, final CMDataView dataView) {
		this.store = store;
		this.operationUser = operationUser;
		this.dataView = dataView;
	}

	public Iterable<LookupTypeDto> getAllTypes() {
		logger.info(marker, "getting all lookup types");
		return from(store.list()) //
				.transform(toLookupType()) //
				.filter(uniques());
	}

	public void saveLookupType(final LookupTypeDto newType, final LookupTypeDto oldType) {
		logger.info(marker, "saving lookup type, new is '{}', old is '{}'", newType, oldType);

		assure(operationUser.hasAdministratorPrivileges());

		if (isBlank(newType.name)) {
			logger.error("invalid name '{}' for lookup type", newType.name);
			throw ORM_CHANGE_LOOKUPTYPE_ERROR.createException();
		}

		final LookupTypeDto existingLookupType = typeForNameAndParent(oldType.name, oldType.parent);
		if (existingLookupType == null) {
			logger.info(marker, "old one not specified, creating a new one");
			final LookupDto lookup = LookupDto.newInstance() //
					.withType(newType) //
					.withNumber(1) //
					.withActiveStatus(true) //
					.build();
			store.create(lookup);
		} else {
			logger.info(marker, "old one specified, modifying existing one");
			for (final LookupDto lookup : store.listForType(oldType)) {
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

			logger.info(marker, "updates existing classes' attributes");
			for (final CMClass existingClass : dataView.findClasses()) {
				logger.debug(marker, "examining class '{}'", existingClass.getName());
				for (final CMAttribute existingAttribute : existingClass.getAttributes()) {
					logger.debug(marker, "examining attribute '{}'", existingAttribute.getName());
					existingAttribute.getType().accept(new NullAttributeTypeVisitor() {

						@Override
						public void visit(final LookupAttributeType attributeType) {
							if (asList(oldType.name, newType.name).contains(attributeType.getLookupTypeName())) {
								dataView.updateAttribute(attribute(existingAttribute, newType));
							}
						}

						private CMAttributeDefinition attribute(final CMAttribute attribute, final LookupTypeDto type) {
							return new CMAttributeDefinition() {

								@Override
								public String getName() {
									return attribute.getName();
								}

								@Override
								public CMEntryType getOwner() {
									return attribute.getOwner();
								}

								@Override
								public CMAttributeType<?> getType() {
									return new LookupAttributeType(type.name);
								}

								@Override
								public String getDescription() {
									return attribute.getDescription();
								}

								@Override
								public String getDefaultValue() {
									return attribute.getDefaultValue();
								}

								@Override
								public boolean isDisplayableInList() {
									return attribute.isDisplayableInList();
								}

								@Override
								public boolean isMandatory() {
									return attribute.isMandatory();
								}

								@Override
								public boolean isUnique() {
									return attribute.isUnique();
								}

								@Override
								public boolean isActive() {
									return attribute.isActive();
								}

								@Override
								public Mode getMode() {
									return attribute.getMode();
								}

								@Override
								public int getIndex() {
									return attribute.getIndex();
								}

								@Override
								public String getGroup() {
									return attribute.getGroup();
								}

								@Override
								public int getClassOrder() {
									return attribute.getClassOrder();
								}

								@Override
								public String getEditorType() {
									return attribute.getEditorType();
								}

								@Override
								public String getForeignKeyDestinationClassName() {
									return attribute.getForeignKeyDestinationClassName();
								}

							};
						}

					});
				}
			}
		}
	}

	public Iterable<LookupDto> getAllLookup(final LookupTypeDto type, final boolean activeOnly, final int start,
			final int limit) {
		logger.info(marker, "getting all lookups for type '{}'", type);

		final LookupTypeDto realType = typeFor(typesWith(type.name));

		logger.debug(marker, "getting all lookups for real type '{}'", realType);

		final Iterable<LookupDto> elements = store.listForType(realType);

		if (!elements.iterator().hasNext()) {
			logger.error(marker, "no lookup was found for type '{}'", realType);
			throw Exceptions.lookupTypeNotFound(realType);
		}

		final List<LookupDto> list = newArrayList(elements);

		logger.debug(marker, "ordering elements");
		sort(list, NUMBER_COMPARATOR);

		return from(list) //
				.filter(actives(activeOnly)) //
				.filter(limited(start, limit));
	}

	public Iterable<LookupDto> getAllLookupOfParent(final LookupTypeDto type) {
		logger.info(marker, "getting all lookups for the parent of type '{}'", type);
		final LookupTypeDto current = typeFor(typesWith(type.name));
		final LookupTypeDto parent = LookupTypeDto.newInstance() //
				.withName(current.parent) //
				.build();
		return store.listForType(parent);
	}

	public LookupDto getLookup(final Long id) {
		logger.info(marker, "getting lookup with id '{}'", id);
		final Iterator<LookupDto> elements = from(store.list()) //
				.filter(new Predicate<LookupDto>() {
					@Override
					public boolean apply(final LookupDto input) {
						return input.id.equals(input);
					};
				}) //
				.iterator();
		if (!elements.hasNext()) {
			throw Exceptions.lookupNotFound(id);
		}
		final LookupDto lookup = elements.next();
		if (elements.hasNext()) {
			logger.error(marker, "multiple elements with id '{}'", id);
			throw Exceptions.multipleElementsWithSameId();
		}
		return lookup;
	}

	public void enableLookup(final Long id) {
		logger.info(marker, "enabling lookup with id '{}'", id);
		assure(operationUser.hasAdministratorPrivileges());
		setActiveStatus(true, id);
	}

	public void disableLookup(final Long id) {
		logger.info(marker, "disabling lookup with id '{}'", id);
		assure(operationUser.hasAdministratorPrivileges());
		setActiveStatus(false, id);
	}

	private void setActiveStatus(final boolean status, final Long id) {
		logger.info(marker, "setting active status '{}' for lookup with id '{}'", status, id);
		if (id <= 0) {
			logger.warn(marker, "invalid id '{}', exiting without doing nothing", id);
			return;
		}

		logger.debug(marker, "getting lookup with id '{}'", id);
		final Iterator<LookupDto> shouldBeOneOnly = from(store.list()) //
				.filter(withId(id)) //
				.iterator();

		if (!shouldBeOneOnly.hasNext()) {
			throw Exceptions.lookupNotFound(id);
		}

		logger.debug(marker, "updating lookup active to '{}'", status);
		final LookupDto lookup = LookupDto.newInstance() //
				.clone(shouldBeOneOnly.next()) //
				.withActiveStatus(status) //
				.build();

		store.update(lookup);
	}

	private LookupTypeDto typeForNameAndParent(final String name, final String parent) {
		logger.info(marker, "getting lookup type with name '{}' and parent '{}'", name, parent);
		return typeFor(typesWith(name, parent));
	}

	private LookupTypeDto typeFor(final Predicate<LookupTypeDto> predicate) {
		logger.info(marker, "getting lookup type for predicate");
		final Iterator<LookupTypeDto> shouldBeOneOnly = from(getAllTypes()) //
				.filter(predicate) //
				.iterator();
		final LookupTypeDto found;
		if (!shouldBeOneOnly.hasNext()) {
			logger.warn(marker, "lookup type not found");
			found = null;
		} else {
			logger.warn(marker, "lookup type successfully found");
			found = shouldBeOneOnly.next();
		}
		if ((found != null) && shouldBeOneOnly.hasNext()) {
			logger.error(marker, "more than one lookup type has been found");
			throw ORMExceptionType.ORM_GENERIC_ERROR.createException();
		}
		return found;
	}

	public Long createOrUpdateLookup(final LookupDto lookup) {
		logger.info(marker, "creating or updating lookup '{}'", lookup);

		assure(operationUser.hasAdministratorPrivileges());

		final LookupDto lookupWithRealType = LookupDto.newInstance() //
				.clone(lookup) //
				.withType(typeFor(typesWith(lookup.type.name))) //
				.build();

		final Long id;
		if (isNotExistent(lookupWithRealType)) {
			logger.info(marker, "creating lookup '{}'", lookupWithRealType);

			logger.debug(marker, "checking lookup number ('{}'), if not valid assigning a valid one",
					lookupWithRealType.number);
			final LookupDto toBeCreated;
			if (hasNoValidNumber(lookupWithRealType)) {
				final int count = size(store.listForType(lookupWithRealType.type));
				toBeCreated = LookupDto.newInstance() //
						.clone(lookupWithRealType) //
						.withNumber(count + 1) //
						.build();
			} else {
				toBeCreated = lookupWithRealType;
			}

			final Storable created = store.create(toBeCreated);
			id = Long.valueOf(created.getIdentifier());
		} else {
			logger.info(marker, "updating lookup '{}'", lookupWithRealType);

			logger.debug(marker, "checking lookup number ('{}'), if not valid assigning a valid one",
					lookupWithRealType.number);
			final LookupDto toBeUpdated;
			if (hasNoValidNumber(lookupWithRealType)) {
				final LookupDto actual = store.read(lookupWithRealType);
				toBeUpdated = LookupDto.newInstance() //
						.clone(lookupWithRealType) //
						.withNumber(actual.number) //
						.build();
			} else {
				toBeUpdated = lookupWithRealType;
			}

			store.update(toBeUpdated);
			id = lookupWithRealType.id;
		}
		return id;
	}

	private static boolean isNotExistent(final LookupDto lookup) {
		return lookup.id == null || lookup.id <= 0;
	}

	private static boolean hasNoValidNumber(final LookupDto lookup) {
		return lookup.number == null || lookup.number <= 0;
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
	public void reorderLookup(final LookupTypeDto type, final Map<Long, Integer> positions) {
		logger.info(marker, "reordering lookups for type '{}'", type);

		assure(operationUser.hasAdministratorPrivileges());

		final LookupTypeDto realType = typeFor(typesWith(type.name));
		final Iterable<LookupDto> lookups = store.listForType(realType);
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

}
