package org.cmdbuild.logic.data;

import static java.lang.String.format;
import static org.apache.commons.lang.StringUtils.defaultIfBlank;
import static org.apache.commons.lang.StringUtils.isNotBlank;
import static org.apache.commons.lang.StringUtils.trim;

import java.util.EnumSet;
import java.util.Set;

import org.apache.commons.lang.Validate;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.cmdbuild.common.Builder;
import org.cmdbuild.dao.entrytype.attributetype.BooleanAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.CMAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.CharAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.DateAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.DateTimeAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.DecimalAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.DoubleAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.IPAddressAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.IntegerAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.StringAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.TextAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.TimeAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.UndefinedAttributeType;
import org.cmdbuild.logger.Log;

public class AttributeDTO {

	private static enum AttributeTypeBuilder {

		BOOLEAN {
			@Override
			public CMAttributeType<?> buildFrom(final AttributeDTOBuilder attributeDTOBuilder) {
				return new BooleanAttributeType();
			}
		}, //
		CHAR {
			@Override
			public CMAttributeType<?> buildFrom(final AttributeDTOBuilder attributeDTOBuilder) {
				return new CharAttributeType();
			}
		}, //
		DATE {
			@Override
			public CMAttributeType<?> buildFrom(final AttributeDTOBuilder attributeDTOBuilder) {
				return new DateAttributeType();
			}
		}, //
		DECIMAL {
			@Override
			public CMAttributeType<?> buildFrom(final AttributeDTOBuilder attributeDTOBuilder) {
				final Integer precision = attributeDTOBuilder.precision;
				final Integer scale = attributeDTOBuilder.scale;
				return new DecimalAttributeType(precision, scale);
			}
		}, //
		DOUBLE {
			@Override
			public CMAttributeType<?> buildFrom(final AttributeDTOBuilder attributeDTOBuilder) {
				return new DoubleAttributeType();
			}
		}, //
		FOREIGNKEY {
			@Override
			public CMAttributeType<?> buildFrom(final AttributeDTOBuilder attributeDTOBuilder) {
				// TODO Auto-generated method stub
				return null;
			}
		}, //
		INET {
			@Override
			public CMAttributeType<?> buildFrom(final AttributeDTOBuilder attributeDTOBuilder) {
				return new IPAddressAttributeType();
			}
		}, //
		INTEGER {
			@Override
			public CMAttributeType<?> buildFrom(final AttributeDTOBuilder attributeDTOBuilder) {
				return new IntegerAttributeType();
			}
		}, //
		LOOKUP {
			@Override
			public CMAttributeType<?> buildFrom(final AttributeDTOBuilder attributeDTOBuilder) {
				// TODO Auto-generated method stub
				return null;
			}
		}, //
		REFERENCE {
			@Override
			public CMAttributeType<?> buildFrom(final AttributeDTOBuilder attributeDTOBuilder) {
				// TODO Auto-generated method stub
				return null;
			}
		}, //
		STRING {
			@Override
			public CMAttributeType<?> buildFrom(final AttributeDTOBuilder attributeDTOBuilder) {
				final Integer length = attributeDTOBuilder.length;
				return new StringAttributeType(length);
			}
		}, //
		TIME {
			@Override
			public CMAttributeType<?> buildFrom(final AttributeDTOBuilder attributeDTOBuilder) {
				return new TimeAttributeType();
			}
		}, //
		TIMESTAMP {
			@Override
			public CMAttributeType<?> buildFrom(final AttributeDTOBuilder attributeDTOBuilder) {
				return new DateTimeAttributeType();
			}
		}, //
		TEXT {
			@Override
			public CMAttributeType<?> buildFrom(final AttributeDTOBuilder attributeDTOBuilder) {
				return new TextAttributeType();
			}
		}, //

		UNDEFINED {
			@Override
			public CMAttributeType<?> buildFrom(final AttributeDTOBuilder attributeDTOBuilder) {
				return new UndefinedAttributeType();
			}
		}; //

		public abstract CMAttributeType<?> buildFrom(AttributeDTOBuilder attributeDTOBuilder);

		public static AttributeTypeBuilder from(final String name) {
			for (final AttributeTypeBuilder attributeType : values()) {
				if (attributeType.name().equals(name)) {
					return attributeType;
				}
			}
			Log.CMDBUILD.warn(format("cannot find attribute type for name '%s'", name));
			return UNDEFINED;
		}

	}

	private static enum Condition {

		ACTIVE, //
		DISPLAYABLE_IN_LIST, //
		NULL_VALUES_ALLOWED, //
		UNIQUE_VALUES, //

	}

	public static class AttributeDTOBuilder implements Builder<AttributeDTO> {

		private String name;
		private Long owner;
		private String description;
		private String group;
		private String defaultValue;
		private String typeName;
		private CMAttributeType<?> type;
		private Integer precision;
		private Integer scale;
		private Integer length;
		private final Set<Condition> conditions;

		private AttributeDTOBuilder() {
			// use factory method
			conditions = EnumSet.of(Condition.ACTIVE);
		}

		public AttributeDTOBuilder withName(final String name) {
			this.name = trim(name);
			return this;
		}

		public AttributeDTOBuilder withOwner(final Long owner) {
			this.owner = owner;
			return this;
		}

		public AttributeDTOBuilder withDescription(final String description) {
			this.description = description;
			return this;
		}

		public AttributeDTOBuilder withGroup(final String group) {
			this.group = group;
			return this;
		}

		public AttributeDTOBuilder withDefaultValue(final String defaultValue) {
			this.defaultValue = defaultValue;
			return this;
		}

		public AttributeDTOBuilder thatIsDisplayableInList(final boolean isDisplayableInList) {
			addOrRemoveCondition(Condition.DISPLAYABLE_IN_LIST, isDisplayableInList);
			return this;
		}

		public AttributeDTOBuilder thatIsMandatory(final boolean allowsNullValues) {
			addOrRemoveCondition(Condition.NULL_VALUES_ALLOWED, allowsNullValues);
			return this;
		}

		public AttributeDTOBuilder thatIsUnique(final boolean thatIsUnique) {
			addOrRemoveCondition(Condition.UNIQUE_VALUES, thatIsUnique);
			return this;
		}

		public AttributeDTOBuilder thatIsActive(final boolean isActive) {
			addOrRemoveCondition(Condition.ACTIVE, isActive);
			return this;
		}

		private void addOrRemoveCondition(final Condition condition, final boolean b) {
			if (b) {
				conditions.add(condition);
			} else {
				conditions.remove(condition);
			}
		}

		public AttributeDTOBuilder withType(final String type) {
			this.typeName = type;
			return this;
		}

		public AttributeDTOBuilder withPrecision(final Integer precision) {
			this.precision = precision;
			return this;
		}

		public AttributeDTOBuilder withScale(final Integer scale) {
			this.scale = scale;
			return this;
		}

		public AttributeDTOBuilder withLength(final Integer length) {
			this.length = length;
			return this;
		}

		@Override
		public AttributeDTO build() {
			Validate.isTrue(isNotBlank(name), "invalid name");
			Validate.notNull(owner, "missing owner");
			Validate.isTrue(owner > 0, "invalid owner");
			description = defaultIfBlank(description, name);
			calculateType();
//			Validate.isTrue(!(type instanceof UndefinedAttributeType), "undefined type");
			return new AttributeDTO(this);
		}

		private void calculateType() {
			type = AttributeTypeBuilder.from(typeName).buildFrom(this);
		}

	}

	public static AttributeDTOBuilder newAttributeDTO() {
		return new AttributeDTOBuilder();
	}

	private final String name;
	private final String description;
	private final Long owner;
	private final String group;
	private final CMAttributeType<?> type;
	private final String defaultValue;
	private final Set<Condition> conditions;

	private final String toString;

	private AttributeDTO(final AttributeDTOBuilder builder) {
		this.name = builder.name;
		this.description = builder.description;
		this.owner = builder.owner;
		this.group = builder.group;
		this.type = builder.type;
		this.defaultValue = builder.defaultValue;
		this.conditions = builder.conditions;

		this.toString = ToStringBuilder.reflectionToString(this);
	}

	@Override
	public String toString() {
		return toString;
	}

	public String getName() {
		return name;
	}

	public String getDescription() {
		return description;
	}

	public Long getOwner() {
		return owner;
	}

	public String getGroup() {
		return group;
	}

	public CMAttributeType<?> getType() {
		return type;
	}

	public String getDefaultValue() {
		return defaultValue;
	}

	public boolean isDisplayableInList() {
		return conditions.contains(Condition.DISPLAYABLE_IN_LIST);
	}

	public boolean isMandatory() {
		return conditions.contains(Condition.NULL_VALUES_ALLOWED);
	}

	public boolean isUnique() {
		return conditions.contains(Condition.UNIQUE_VALUES);
	}

	public boolean isActive() {
		return conditions.contains(Condition.ACTIVE);
	}

}
