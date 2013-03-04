package org.cmdbuild.logic.data;

import static com.google.common.collect.FluentIterable.from;
import static com.google.common.collect.Iterables.filter;
import static com.google.common.collect.Lists.newArrayList;
import static java.util.Arrays.asList;
import static java.util.Collections.sort;
import static org.apache.commons.lang.StringUtils.defaultIfBlank;

import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.cmdbuild.common.Builder;
import org.cmdbuild.dao.entry.CMCard;
import org.cmdbuild.dao.entrytype.CMAttribute;
import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.dao.entrytype.attributetype.LookupAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.NullAttributeTypeVisitor;
import org.cmdbuild.dao.view.CMDataView;
import org.cmdbuild.exception.NotFoundException.NotFoundExceptionType;
import org.cmdbuild.exception.ORMException.ORMExceptionType;
import org.cmdbuild.logic.Logic;
import org.cmdbuild.logic.data.LookupLogic.LookupTypeDto.LookupTypeDtoBuilder;
import org.cmdbuild.model.data.Attribute;
import org.cmdbuild.services.store.DataViewStore;
import org.cmdbuild.services.store.DataViewStore.BaseStorableConverter;
import org.cmdbuild.services.store.DataViewStore.StorableConverter;
import org.cmdbuild.services.store.Store;
import org.cmdbuild.services.store.Store.Storable;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

public class LookupLogic implements Logic {

	public static final class LookupTypeDto {

		public static class LookupTypeDtoBuilder implements Builder<LookupTypeDto> {

			private String name;
			private String parent;

			/**
			 * instantiate using {@link LookupTypeDto#newInstance()}
			 */
			private LookupTypeDtoBuilder() {
			}

			public LookupTypeDtoBuilder withName(final String value) {
				this.name = value;
				return this;
			}

			public LookupTypeDtoBuilder withParent(final String value) {
				this.parent = value;
				return this;
			}

			@Override
			public LookupTypeDto build() {
				this.name = defaultIfBlank(name, null);
				this.parent = defaultIfBlank(parent, null);

				return new LookupTypeDto(this);
			}

		}

		public static LookupTypeDtoBuilder newInstance() {
			return new LookupTypeDtoBuilder();
		}

		public final String name;
		public final String parent;

		private final transient int hashCode;
		private final transient String toString;

		public LookupTypeDto(final LookupTypeDtoBuilder builder) {
			this.name = builder.name;
			this.parent = builder.parent;

			this.hashCode = new HashCodeBuilder() //
					.append(this.name) //
					.append(this.parent) //
					.toHashCode();
			this.toString = new ToStringBuilder(this, ToStringStyle.SIMPLE_STYLE) //
					.append("name", name) //
					.append("parent", parent) //
					.toString();
		}

		@Override
		public int hashCode() {
			return hashCode;
		}

		@Override
		public boolean equals(final Object obj) {
			if (obj == this) {
				return true;
			}
			if (!(obj instanceof LookupTypeDto)) {
				return false;
			}
			final LookupTypeDto other = LookupTypeDto.class.cast(obj);
			return new EqualsBuilder() //
					.append(name, other.name) //
					.append(parent, other.parent) //
					.isEquals();
		}

		@Override
		public String toString() {
			return toString;
		}

	}

	public static final class LookupDto implements Storable {

		public static class LookupDtoBuilder implements Builder<LookupDto> {

			private Long id;
			private String code;
			private String description;
			private LookupTypeDto type;
			private Integer number;
			private boolean active;
			private boolean isDefault;
			private Long parentId;
			private LookupDto parent;

			/**
			 * instantiate using {@link LookupDto#newInstance()}
			 */
			private LookupDtoBuilder() {
			}

			public LookupDtoBuilder clone(final LookupDto lookup) {
				this.id = lookup.id;
				this.code = lookup.code;
				this.description = lookup.description;
				this.type = lookup.type;
				this.number = lookup.number;
				this.active = lookup.active;
				this.isDefault = lookup.isDefault;
				this.parentId = lookup.parentId;
				this.parent = lookup.parent;
				return this;
			}

			public LookupDtoBuilder withId(final Long value) {
				this.id = value;
				return this;
			}

			public LookupDtoBuilder withCode(final String value) {
				this.code = value;
				return this;
			}

			public LookupDtoBuilder withDescription(final String value) {
				this.description = value;
				return this;
			}

			public LookupDtoBuilder withType(final LookupTypeDtoBuilder builder) {
				return withType(builder.build());
			}

			public LookupDtoBuilder withType(final LookupTypeDto value) {
				this.type = value;
				return this;
			}

			public LookupDtoBuilder withNumber(final Integer value) {
				this.number = value;
				return this;
			}

			public LookupDtoBuilder withActiveStatus(final boolean value) {
				this.active = value;
				return this;
			}

			public LookupDtoBuilder withDefaultStatus(final boolean value) {
				this.isDefault = value;
				return this;
			}

			public LookupDtoBuilder withParentId(final Long parentId) {
				this.parentId = parentId;
				return this;
			}

			public LookupDtoBuilder withParent(final LookupDto parent) {
				this.parentId = parent.id;
				this.parent = parent;
				return this;
			}

			@Override
			public LookupDto build() {
				return new LookupDto(this);
			}

		}

		public static LookupDtoBuilder newInstance() {
			return new LookupDtoBuilder();
		}

		public final Long id;
		public final String code;
		public final String description;
		public final LookupTypeDto type;
		public final Integer number;
		public final boolean active;
		public final boolean isDefault;
		public final Long parentId;
		public final LookupDto parent;

		private LookupDto(final LookupDtoBuilder builder) {
			this.id = builder.id;
			this.code = builder.code;
			this.description = builder.description;
			this.type = builder.type;
			this.number = builder.number;
			this.active = builder.active;
			this.isDefault = builder.isDefault;
			this.parentId = builder.parentId;
			this.parent = builder.parent;
		}

		@Override
		public String getIdentifier() {
			return id.toString();
		}

	}

	private static final StorableConverter<LookupDto> LOOKUP_STORABLE_CONVERTER = new BaseStorableConverter<LookupDto>() {

		private static final String LOOKUP_TABLE_NAME = "LookUp";

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
					.withType(LookupTypeDto.newInstance() //
							.withName(card.get("Type", String.class)) //
							.withParent(card.get("ParentType", String.class))) //
					.withNumber(card.get("Number", Integer.class)) //
					.withActiveStatus(card.get("Active", Boolean.class)) //
					.withDefaultStatus(card.get("IsDefault", Boolean.class)) //
					.withParentId(card.get("ParentId", Long.class)) //
					.build();
		}

		@Override
		public Map<String, Object> getValues(final LookupDto storable) {
			final Map<String, Object> values = Maps.newHashMap();
			values.put("Code", storable.code);
			values.put("Description", storable.description);
			values.put("Type", storable.type.name);
			values.put("ParentType", storable.type.parent);
			values.put("Number", storable.number);
			values.put("Active", storable.active);
			values.put("IsDefault", storable.isDefault);
			values.put("ParentId", storable.parentId);
			return values;
		}

	};

	private static final Function<LookupDto, LookupTypeDto> LOOKUP_TO_LOOKUP_TYPE = new Function<LookupDto, LookupTypeDto>() {
		@Override
		public LookupTypeDto apply(final LookupDto input) {
			return input.type;
		}
	};

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

	private final CMDataView view;
	private final Store<LookupDto> store;

	public LookupLogic(final CMDataView view) {
		this.view = view;
		this.store = new DataViewStore<LookupDto>(view, LOOKUP_STORABLE_CONVERTER);
	}

	public Iterable<LookupTypeDto> getAllTypes() {
		logger.info("getting all lookup types");
		return from(store.list()) //
				.transform(LOOKUP_TO_LOOKUP_TYPE) //
				.filter(uniques());
	}

	private static Predicate<LookupTypeDto> uniques() {
		return new Predicate<LookupTypeDto>() {

			private final Set<LookupTypeDto> uniques = Sets.newHashSet();

			@Override
			public boolean apply(final LookupTypeDto input) {
				return uniques.add(input);
			}

		};
	}

	public void saveLookupType(final LookupTypeDto newType, final LookupTypeDto oldType) {
		logger.info("saving lookup type, new is '{}', old is '{}'", newType, oldType);
		final LookupTypeDto existingLookupType = typeForNameAndParent(oldType.name, oldType.parent);
		if (existingLookupType == null) {
			logger.info("old one not specified, creating a new one");
			final LookupDto lookup = LookupDto.newInstance().withType(newType).build();
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
			final DataAccessLogic dataAccessLogic = new DataAccessLogic(view);
			final DataDefinitionLogic dataDefinitionLogic = new DataDefinitionLogic(view);
			for (final CMClass existingClass : dataAccessLogic.findAllClasses()) {
				for (final CMAttribute existingAttribute : existingClass.getAllAttributes()) {
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

	private LookupTypeDto typeForNameAndParent(final String name, final String parent) {
		logger.info("getting lookup type with name '{}' and parent '{}'", name, parent);
		final Iterator<LookupTypeDto> shouldBeOneOnly = filter(getAllTypes(), typesWith(name, parent)).iterator();
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

	private static Predicate<LookupTypeDto> typesWith(final String name, final String parent) {
		logger.debug("filtering types with name '{}' and parent '{}'", name, parent);
		return new Predicate<LookupTypeDto>() {
			@Override
			public boolean apply(final LookupTypeDto input) {
				return new EqualsBuilder() //
						.append(name, input.name) //
						.append(parent, input.parent) //
						.isEquals();
			}
		};
	}

	public Iterable<LookupDto> getAllLookup(final LookupTypeDto type, final boolean activeOnly, final int start,
			final int limit) {
		logger.info("getting all lookups with type '{}'", type);
		final Iterable<LookupDto> elements = listForType(type);

		if (!elements.iterator().hasNext()) {
			logger.error("no lookup was found for type '{}'", type);
			throw NotFoundExceptionType.LOOKUP_TYPE_NOTFOUND.createException(type.name);
		}

		final List<LookupDto> list = newArrayList(elements);

		logger.debug("ordering elements");
		sort(list, NUMBER_COMPARATOR);

		return FluentIterable.from(list).filter(
		/*
		 * active ones only, only if required
		 */
		new Predicate<LookupDto>() {

			@Override
			public boolean apply(final LookupDto input) {
				return !activeOnly || input.active;
			}

		}).filter(
		/*
		 * specified range only
		 */
		new Predicate<LookupDto>() {

			private final int end = limit > 0 ? limit + start : Integer.MAX_VALUE;
			private int i = 0;

			@Override
			public boolean apply(final LookupDto input) {
				i++;
				return (start <= i && i < end);
			}

		});
	}

	private Iterable<LookupDto> listForType(final LookupTypeDto type) {
		logger.debug("getting lookups with type '{}'", type);

		final Iterable<LookupDto> lookups = store.list();

		final Map<Long, LookupDto> lookupsById = Maps.newHashMap();
		for (final LookupDto lookup : lookups) {
			lookupsById.put(lookup.id, lookup);
		}

		for (final LookupDto lookup : lookups) {
			final LookupDto lookupWithParent = buildWithParent(lookup, lookupsById);
			lookupsById.put(lookupWithParent.id, lookupWithParent);
		}

		return filter(lookupsById.values(), typesWith(type));
	}

	private LookupDto buildWithParent(final LookupDto lookup, final Map<Long, LookupDto> lookupsById) {
		final LookupDto lookupWithParent;
		final LookupDto parent = lookupsById.get(lookup.parentId);
		if (parent != null) {
			final Long grandparentId = parent.parentId;
			final LookupDto parentWithGrandparent;
			if (grandparentId != null) {
				parentWithGrandparent = buildWithParent(parent, lookupsById);
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

	private Predicate<LookupDto> typesWith(final LookupTypeDto type) {
		logger.debug("filtering lookups with type '{}'", type);
		return new Predicate<LookupDto>() {
			@Override
			public boolean apply(final LookupDto input) {
				return input.type.equals(type);
			}
		};
	}

}
