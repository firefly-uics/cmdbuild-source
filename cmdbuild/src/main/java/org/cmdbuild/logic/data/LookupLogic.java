package org.cmdbuild.logic.data;

import static com.google.common.collect.FluentIterable.from;
import static com.google.common.collect.Iterables.filter;
import static java.util.Arrays.asList;
import static org.apache.commons.lang.StringUtils.defaultIfBlank;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.cmdbuild.dao.entry.CMCard;
import org.cmdbuild.dao.entrytype.CMAttribute;
import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.dao.entrytype.attributetype.LookupAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.NullAttributeTypeVisitor;
import org.cmdbuild.dao.view.CMDataView;
import org.cmdbuild.exception.ORMException.ORMExceptionType;
import org.cmdbuild.logic.Logic;
import org.cmdbuild.model.data.Attribute;
import org.cmdbuild.services.store.DataViewStore;
import org.cmdbuild.services.store.DataViewStore.BaseStorableConverter;
import org.cmdbuild.services.store.DataViewStore.StorableConverter;
import org.cmdbuild.services.store.Store;
import org.cmdbuild.services.store.Store.Storable;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

public class LookupLogic implements Logic {

	public static final class LookupTypeDto {

		public final String name;
		public final String parent;

		private final transient int hashCode;
		private final transient String toString;

		public LookupTypeDto(final String name, final String parentType) {
			this.name = defaultIfBlank(name, null);
			this.parent = defaultIfBlank(parentType, null);

			this.hashCode = new HashCodeBuilder() //
					.append(this.name) //
					.append(this.parent) //
					.toHashCode();
			this.toString = new ToStringBuilder(this, ToStringStyle.SIMPLE_STYLE) //
					.append("name", name) //
					.append("parent", parentType) //
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

		final Long id;
		public final String code;
		public final String description;
		public final LookupTypeDto type;

		public LookupDto(final String code, final String description, final LookupTypeDto type) {
			this(null, code, description, type);
		}

		public LookupDto(final Long id, final String code, final String description, final LookupTypeDto type) {
			this.id = id;
			this.code = code;
			this.description = description;
			this.type = type;
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
			return new LookupDto( //
					card.getId(), //
					(String) card.getCode(), //
					(String) card.getDescription(), //
					new LookupTypeDto(//
							card.get("Type", String.class), //
							card.get("ParentType", String.class)));
		}

		@Override
		public Map<String, Object> getValues(final LookupDto storable) {
			final Map<String, Object> values = Maps.newHashMap();
			values.put("Code", storable.code);
			values.put("Description", storable.description);
			values.put("Type", storable.type.name);
			values.put("ParentType", storable.type.parent);
			values.put("Number", 0);
			values.put("IsDefault", false);
			return values;
		}

	};

	private static final Function<LookupDto, LookupTypeDto> LOOKUP_TO_LOOKUP_TYPE = new Function<LookupDto, LookupTypeDto>() {
		@Override
		public LookupTypeDto apply(final LookupDto input) {
			return input.type;
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
			final LookupDto lookup = new LookupDto(null, null, newType);
			store.create(lookup);
		} else {
			logger.info("old one specified, modifying existing one");
			for (final LookupDto lookup : getAllLookup(oldType)) {
				final LookupDto newLookup = new LookupDto( //
						lookup.id, //
						lookup.code, //
						lookup.description, //
						newType);
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

	private Iterable<LookupDto> getAllLookup(final LookupTypeDto type) {
		return filter(store.list(), typesWith(type));
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
