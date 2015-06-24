package org.cmdbuild.servlets.json.translation;

import static org.cmdbuild.servlets.json.CommunicationConstants.DESCRIPTION;
import static org.cmdbuild.servlets.json.CommunicationConstants.INDEX;

import java.util.Collection;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.cmdbuild.dao.entrytype.CMAttribute;
import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.logic.data.access.DataAccessLogic;
import org.cmdbuild.logic.translation.TranslationLogic;
import org.cmdbuild.logic.translation.TranslationObject;
import org.cmdbuild.logic.translation.converter.AttributeConverter;
import org.cmdbuild.logic.translation.converter.ClassConverter;
import org.cmdbuild.servlets.json.management.JsonResponse;
import org.cmdbuild.servlets.json.schema.Translation.JsonAttribute;
import org.cmdbuild.servlets.json.schema.Translation.JsonEntryType;
import org.cmdbuild.servlets.json.schema.Translation.JsonField;

import com.google.common.collect.Lists;
import com.google.common.collect.Ordering;

public class ClassTranslationSerializer implements TranslationSerializer {

	protected final DataAccessLogic dataLogic;
	private final boolean activeOnly;
	private final TranslationLogic translationLogic;

	// TODO make it configurable
	private static final String CLASS_SORTER_PROPERTY = DESCRIPTION;
	private static final String CLASS_SORTER_DIRECTION = "ASC";
	private static final String ATTRIBUTE_SORTER_PROPERTY = INDEX;
	private static final String ATTRIBUTE_SORTER_DIRECTION = "ASC";

	public ClassTranslationSerializer(final DataAccessLogic dataLogic, final boolean activeOnly,
			final TranslationLogic translationLogic) {
		this.dataLogic = dataLogic;
		this.activeOnly = activeOnly;
		this.translationLogic = translationLogic;
	}

	@Override
	public JsonResponse serialize() {
		final Iterable<? extends CMClass> classes = dataLogic.findClasses(activeOnly);
		final Iterable<? extends CMClass> sortedClasses = ClassSorter //
				.of(CLASS_SORTER_PROPERTY) //
				.getOrdering(CLASS_SORTER_DIRECTION) //
				.sortedCopy(classes);

		return readStructure(sortedClasses);
	}

	protected JsonResponse readStructure(final Iterable<? extends CMClass> sortedClasses) {
		final Collection<JsonEntryType> jsonClasses = Lists.newArrayList();
		for (final CMClass cmclass : sortedClasses) {
			final String className = cmclass.getName();
			final JsonEntryType jsonClass = new JsonEntryType();
			jsonClass.setName(className);
			final Collection<JsonField> classFields = readFields(cmclass);
			final Iterable<? extends CMAttribute> allAttributes = cmclass.getAllAttributes();
			final Iterable<? extends CMAttribute> sortedAttributes = AttributeSorter //
					.of(ATTRIBUTE_SORTER_PROPERTY) //
					.getOrdering(ATTRIBUTE_SORTER_DIRECTION) //
					.sortedCopy(allAttributes);
			final Collection<JsonAttribute> jsonAttributes = Lists.newArrayList();
			for (final CMAttribute attribute : sortedAttributes) {
				final String attributeName = attribute.getName();
				final Collection<JsonField> attributeFields = readFields(attribute);
				final JsonAttribute jsonAttribute = new JsonAttribute();
				jsonAttribute.setName(attributeName);
				jsonAttribute.setFields(attributeFields);
				jsonAttributes.add(jsonAttribute);
			}
			jsonClass.setAttributes(jsonAttributes);
			jsonClass.setFields(classFields);
			jsonClasses.add(jsonClass);
		}
		return JsonResponse.success(jsonClasses);
	}

	private Collection<JsonField> readFields(final CMAttribute attribute) {
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

	private static enum ClassSorter {
		NAME("name") {
			@Override
			protected Ordering<CMClass> getOrdering() {
					return ORDER_CLASS_BY_NAME;
			}
		},
		DESCRIPTION("description") {
			@Override
			protected Ordering<CMClass> getOrdering() {
					return ORDER_CLASS_BY_DESCRIPTION;
			}
		},
		UNDEFINED(StringUtils.EMPTY) {
			@Override
			protected Ordering<CMClass> getOrdering() {
				throw new UnsupportedOperationException();
			}
		};

		private final String sorter;

		private ClassSorter(final String sorter) {
			this.sorter = sorter;
		}

		abstract Ordering<CMClass> getOrdering();

		protected Ordering<CMClass> getOrdering(final String direction) {
			if (direction.equalsIgnoreCase("DESC")) {
				return getOrdering().reverse();
			} else {
				return getOrdering();
			}
		}

		private static ClassSorter of(final String field) {
			for (final ClassSorter element : values()) {
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

		protected Ordering<CMAttribute> getOrdering(final String direction) {
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

	private static final Ordering<CMClass> ORDER_CLASS_BY_NAME = new Ordering<CMClass>() {
		@Override
		public int compare(final CMClass left, final CMClass right) {
			return left.getName().compareTo(right.getName());
		}
	};

	private static final Ordering<CMClass> ORDER_CLASS_BY_DESCRIPTION = new Ordering<CMClass>() {
		@Override
		public int compare(final CMClass left, final CMClass right) {
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
