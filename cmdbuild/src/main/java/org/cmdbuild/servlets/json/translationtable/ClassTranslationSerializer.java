package org.cmdbuild.servlets.json.translationtable;

import static org.cmdbuild.servlets.json.CommunicationConstants.DESCRIPTION;
import static org.cmdbuild.servlets.json.CommunicationConstants.INDEX;

import java.util.Collection;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.cmdbuild.dao.entrytype.CMAttribute;
import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.dao.entrytype.CMEntryType;
import org.cmdbuild.logic.data.access.DataAccessLogic;
import org.cmdbuild.logic.data.access.DataAccessLogic.AttributesQuery;
import org.cmdbuild.logic.translation.TranslationLogic;
import org.cmdbuild.logic.translation.TranslationObject;
import org.cmdbuild.logic.translation.converter.AttributeConverter;
import org.cmdbuild.logic.translation.converter.ClassConverter;
import org.cmdbuild.servlets.json.management.JsonResponse;

import com.google.common.collect.Lists;
import com.google.common.collect.Ordering;

public class ClassTranslationSerializer extends EntryTypeTranslationSerializer {

	// TODO make it configurable
	static final String ENTRYTYPE_SORTER_PROPERTY = DESCRIPTION;
	static final String ENTRYTYPE_SORTER_DIRECTION = "ASC";
	static final String ATTRIBUTE_SORTER_PROPERTY = INDEX;
	static final String ATTRIBUTE_SORTER_DIRECTION = "ASC";

	private static final AttributesQuery NO_LIMIT_AND_OFFSET = new AttributesQuery() {

		@Override
		public Integer limit() {
			return null;
		}

		@Override
		public Integer offset() {
			return null;
		}

	};

	public ClassTranslationSerializer(final DataAccessLogic dataLogic, final boolean activeOnly,
			final TranslationLogic translationLogic) {
		super(dataLogic, activeOnly, translationLogic);
	}

	@Override
	public JsonResponse serialize() {
		final Iterable<? extends CMClass> classes = dataLogic.findClasses(activeOnly);
		final Iterable<? extends CMClass> sortedClasses = EntryTypeSorter //
				.of(ENTRYTYPE_SORTER_PROPERTY) //
				.getOrdering(ENTRYTYPE_SORTER_DIRECTION) //
				.sortedCopy(classes);
		return readStructure(sortedClasses);
	}

	JsonResponse readStructure(final Iterable<? extends CMClass> sortedClasses) {
		final Collection<JsonElement> jsonClasses = Lists.newArrayList();
		for (final CMClass cmclass : sortedClasses) {
			final String className = cmclass.getName();
			final JsonElement jsonClass = new JsonElement();
			jsonClass.setName(className);
			final Collection<JsonField> classFields = readFields(cmclass);
			final Iterable<? extends CMAttribute> allAttributes = dataLogic.getAttributes(className, activeOnly,
					NO_LIMIT_AND_OFFSET);
			final Iterable<? extends CMAttribute> sortedAttributes = sortAttributes(allAttributes);
			final Collection<JsonElement> jsonAttributes = serializeAttributes(sortedAttributes);
			jsonClass.setAttributes(jsonAttributes);
			jsonClass.setFields(classFields);
			jsonClasses.add(jsonClass);
		}
		return JsonResponse.success(jsonClasses);
	}

	Iterable<? extends CMEntryType> sort(final Iterable<? extends CMEntryType> allAttributes) {
		final Iterable<? extends CMEntryType> sortedAttributes = EntryTypeSorter //
				.of(ENTRYTYPE_SORTER_PROPERTY) //
				.getOrdering(ENTRYTYPE_SORTER_DIRECTION) //
				.sortedCopy(allAttributes);
		return sortedAttributes;
	}

	Iterable<? extends CMAttribute> sortAttributes(final Iterable<? extends CMAttribute> allAttributes) {
		final Iterable<? extends CMAttribute> sortedAttributes = AttributeSorter //
				.of(ATTRIBUTE_SORTER_PROPERTY) //
				.getOrdering(ATTRIBUTE_SORTER_DIRECTION) //
				.sortedCopy(allAttributes);
		return sortedAttributes;
	}

	Collection<JsonElement> serializeAttributes(final Iterable<? extends CMAttribute> attributes) {
		final Collection<JsonElement> attributesSerialization = Lists.newArrayList();
		for (final CMAttribute attribute : attributes) {
			final String attributeName = attribute.getName();
			final Collection<JsonField> attributeFields = readFields(attribute);
			final JsonElement jsonAttribute = new JsonElement();
			jsonAttribute.setName(attributeName);
			jsonAttribute.setFields(attributeFields);
			attributesSerialization.add(jsonAttribute);
		}
		return attributesSerialization;
	}

	@Override
	Collection<JsonField> readFields(final CMAttribute attribute) {
		final Collection<JsonField> jsonFields = Lists.newArrayList();
		final String ownerName = attribute.getOwner().getName();
		final TranslationObject translationObjectForDescription = AttributeConverter.CLASSATTRIBUTE_DESCRIPTION //
				.withOwner(ownerName).withIdentifier(attribute.getName()) //
				.create();
		final Map<String, String> descriptionTranslations = translationLogic.readAll(translationObjectForDescription);
		final JsonField descriptionField = new JsonField();
		descriptionField.setName(AttributeConverter.description());
		descriptionField.setTranslations(descriptionTranslations);
		descriptionField.setValue(attribute.getDescription());
		jsonFields.add(descriptionField);

		final TranslationObject translationObjectForGroup = AttributeConverter.CLASSATTRIBUTE_GROUP //
				.withOwner(ownerName).withIdentifier(attribute.getName()) //
				.create();
		final Map<String, String> groupTranslations = translationLogic.readAll(translationObjectForGroup);
		final JsonField groupField = new JsonField();
		groupField.setName(AttributeConverter.group());
		groupField.setTranslations(groupTranslations);
		groupField.setValue(attribute.getGroup());
		jsonFields.add(groupField);
		return jsonFields;
	}

	private Collection<JsonField> readFields(final CMClass cmclass) {
		final Collection<JsonField> jsonFields = Lists.newArrayList();
		final TranslationObject translationObject = ClassConverter.DESCRIPTION //
				.withIdentifier(cmclass.getName()) //
				.create();
		final Map<String, String> fieldTranslations = translationLogic.readAll(translationObject);
		final JsonField field = new JsonField();
		field.setName(ClassConverter.description());
		field.setTranslations(fieldTranslations);
		field.setValue(cmclass.getDescription());
		jsonFields.add(field);
		return jsonFields;
	}

	static enum EntryTypeSorter {
		NAME("name") {
			@Override
			protected Ordering<CMEntryType> getOrdering() {
				return ORDER_ENTRYTYPE_BY_NAME;
			}
		},
		DESCRIPTION("description") {
			@Override
			protected Ordering<CMEntryType> getOrdering() {
				return ORDER_ENTRYTYPE_BY_DESCRIPTION;
			}
		},
		UNDEFINED(StringUtils.EMPTY) {
			@Override
			protected Ordering<CMEntryType> getOrdering() {
				throw new UnsupportedOperationException();
			}
		};

		private final String sorter;

		private EntryTypeSorter(final String sorter) {
			this.sorter = sorter;
		}

		abstract Ordering<CMEntryType> getOrdering();

		Ordering<CMEntryType> getOrdering(final String direction) {
			if (direction.equalsIgnoreCase("DESC")) {
				return getOrdering().reverse();
			} else {
				return getOrdering();
			}
		}

		static EntryTypeSorter of(final String field) {
			for (final EntryTypeSorter element : values()) {
				if (element.sorter.equalsIgnoreCase(field)) {
					return element;
				}
			}
			return UNDEFINED;
		}
	}

	private static enum AttributeSorter {
		NAME("name") {
			@Override
			protected Ordering<CMAttribute> getOrdering() {
				return ORDER_ATTRIBUTE_BY_NAME;
			}
		},
		DESCRIPTION("description") {
			@Override
			protected Ordering<CMAttribute> getOrdering() {
				return ORDER_ATTRIBUTE_BY_DESCRIPTION;
			}
		},
		INDEX("index") {
			@Override
			protected Ordering<CMAttribute> getOrdering() {
				return ORDER_ATTRIBUTE_BY_INDEX;
			}
		},
		UNDEFINED(StringUtils.EMPTY) {
			@Override
			protected Ordering<CMAttribute> getOrdering() {
				throw new UnsupportedOperationException();
			}
		};

		private final String sorter;

		private AttributeSorter(final String sorter) {
			this.sorter = sorter;
		}

		abstract Ordering<CMAttribute> getOrdering();

		Ordering<CMAttribute> getOrdering(final String direction) {
			if (direction.equalsIgnoreCase("DESC")) {
				return getOrdering().reverse();
			} else {
				return getOrdering();
			}
		}

		private static AttributeSorter of(final String field) {
			for (final AttributeSorter element : values()) {
				if (element.sorter.equalsIgnoreCase(field)) {
					return element;
				}
			}
			return UNDEFINED;
		}
	}

	private static final Ordering<CMEntryType> ORDER_ENTRYTYPE_BY_NAME = new Ordering<CMEntryType>() {
		@Override
		public int compare(final CMEntryType left, final CMEntryType right) {
			return left.getName().compareTo(right.getName());
		}
	};

	private static final Ordering<CMEntryType> ORDER_ENTRYTYPE_BY_DESCRIPTION = new Ordering<CMEntryType>() {
		@Override
		public int compare(final CMEntryType left, final CMEntryType right) {
			return left.getDescription().compareTo(right.getDescription());
		}
	};

	private static final Ordering<CMAttribute> ORDER_ATTRIBUTE_BY_NAME = new Ordering<CMAttribute>() {
		@Override
		public int compare(final CMAttribute left, final CMAttribute right) {
			return left.getName().compareTo(right.getName());
		}
	};

	private static final Ordering<CMAttribute> ORDER_ATTRIBUTE_BY_DESCRIPTION = new Ordering<CMAttribute>() {
		@Override
		public int compare(final CMAttribute left, final CMAttribute right) {
			return left.getDescription().compareTo(right.getDescription());
		}
	};

	private static final Ordering<CMAttribute> ORDER_ATTRIBUTE_BY_INDEX = new Ordering<CMAttribute>() {
		@Override
		public int compare(final CMAttribute left, final CMAttribute right) {
			return left.getIndex() > right.getIndex() ? +1 : left.getIndex() < right.getIndex() ? -1 : 0;
		}
	};

}
