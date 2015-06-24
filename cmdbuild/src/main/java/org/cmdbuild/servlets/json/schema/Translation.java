package org.cmdbuild.servlets.json.schema;

import static com.google.common.collect.FluentIterable.from;
import static org.cmdbuild.servlets.json.CommunicationConstants.ACTIVE;
import static org.cmdbuild.servlets.json.CommunicationConstants.ATTRIBUTES;
import static org.cmdbuild.servlets.json.CommunicationConstants.CODE;
import static org.cmdbuild.servlets.json.CommunicationConstants.DESCRIPTION;
import static org.cmdbuild.servlets.json.CommunicationConstants.FIELD;
import static org.cmdbuild.servlets.json.CommunicationConstants.NAME;
import static org.cmdbuild.servlets.json.CommunicationConstants.SORT;
import static org.cmdbuild.servlets.json.CommunicationConstants.TRANSLATIONS;
import static org.cmdbuild.servlets.json.CommunicationConstants.TRANSLATION_UUID;
import static org.cmdbuild.servlets.json.CommunicationConstants.VALUES;
import static org.cmdbuild.servlets.json.schema.Utils.toMap;

import java.util.Collection;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.cmdbuild.common.Constants;
import org.cmdbuild.dao.entrytype.CMAttribute;
import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.dao.entrytype.CMDomain;
import org.cmdbuild.data.store.lookup.Lookup;
import org.cmdbuild.data.store.lookup.LookupStore;
import org.cmdbuild.data.store.lookup.LookupType;
import org.cmdbuild.logic.data.access.DataAccessLogic;
import org.cmdbuild.logic.translation.TranslationObject;
import org.cmdbuild.logic.translation.converter.AttributeConverter;
import org.cmdbuild.logic.translation.converter.ClassConverter;
import org.cmdbuild.logic.translation.converter.Converter;
import org.cmdbuild.logic.translation.converter.DomainConverter;
import org.cmdbuild.logic.translation.converter.FilterConverter;
import org.cmdbuild.logic.translation.converter.InstanceConverter;
import org.cmdbuild.logic.translation.converter.LookupConverter;
import org.cmdbuild.logic.translation.converter.MenuItemConverter;
import org.cmdbuild.logic.translation.converter.ReportConverter;
import org.cmdbuild.logic.translation.converter.ViewConverter;
import org.cmdbuild.logic.translation.converter.WidgetConverter;
import org.cmdbuild.servlets.json.JSONBaseWithSpringContext;
import org.cmdbuild.servlets.json.management.JsonResponse;
import org.cmdbuild.servlets.json.translation.TranslationSerializerFactory;
import org.cmdbuild.servlets.json.translation.TranslationSerializer;
import org.cmdbuild.servlets.utils.Parameter;
import org.codehaus.jackson.annotate.JsonProperty;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Ordering;

public class Translation extends JSONBaseWithSpringContext {

	private static final String TYPE = "type";
	private static final String IDENTIFIER = "identifier";
	private static final String OWNER = "owner";
	private static final String CLASS_SORTER = "description";
	private static final String CLASS_SORTER_DIRECTION = "ASC";

	private final DataAccessLogic dataLogic = userDataAccessLogic();
	private final LookupStore lookupStore = lookupStore();

	@JSONExported
	@Admin
	public JsonResponse read( //
			@Parameter(value = TYPE) final String type, //
			@Parameter(value = OWNER, required = false) final String owner, //
			@Parameter(value = IDENTIFIER) final String identifier, //
			@Parameter(value = FIELD) final String field //
	) {
		final Converter converter = createConverter(type, field);
		final TranslationObject translationObject = converter.withOwner(owner) //
				.withIdentifier(identifier) //
				.create();
		final Map<String, String> translations = translationLogic().readAll(translationObject);
		return JsonResponse.success(translations);
	}

	@JSONExported
	@Admin
	public void update( //
			@Parameter(value = TYPE) final String type, //
			@Parameter(value = OWNER, required = false) final String owner, //
			@Parameter(value = IDENTIFIER) final String identifier, //
			@Parameter(value = FIELD) final String field, //
			@Parameter(value = TRANSLATIONS) final JSONObject translations //
	) {
		final Converter converter = createConverter(type, field);
		final TranslationObject translationObject = converter //
				.withOwner(owner) //
				.withIdentifier(identifier) //
				.withTranslations(toMap(translations)) //
				.create();
		translationLogic().update(translationObject);
	}

	@JSONExported
	@Admin
	public JsonResponse readStructure( //
			@Parameter(value = TYPE) final String type, //
			@Parameter(value = SORT, required = false) JSONArray sorters, //
			@Parameter(value = ACTIVE, required = false) boolean activeOnly //
	) throws JSONException {
		activeOnly = true;

		JSONObject classSorter = new JSONObject();
		classSorter.put("element", "class");
		classSorter.put("property", "description");
		classSorter.put("direction", "ASC");

		JSONObject attributeSorter = new JSONObject();
		classSorter.put("element", "attribute");
		classSorter.put("property", "index");
		classSorter.put("direction", "ASC");

		sorters = new JSONArray().put(classSorter);
		sorters.put(attributeSorter);

		final TranslationSerializerFactory factory = TranslationSerializerFactory //
				.newInstance() //
				.withDataAccessLogic(dataLogic) //
				.withLookupStore(lookupStore) //
				.withTranslationLogic(translationLogic())
				.withType(type) //
				.withActiveOnly(activeOnly) //
				.withSorters(sorters) //
				.build();
		
		TranslationSerializer serializer = factory.createSerializer();
		return serializer.serialize();
	}

	private Collection<JsonField> readFields(final CMClass cmclass) {
		final Collection<JsonField> jsonFields = Lists.newArrayList();
		final TranslationObject translationObject = ClassConverter.DESCRIPTION //
				.withIdentifier(cmclass.getName()) //
				.create();
		final Map<String, String> fieldTranslations = translationLogic().readAll(translationObject);
		final JsonField field = new JsonField();
		field.setName(ClassConverter.description());
		field.setTranslations(fieldTranslations);
		field.setValue(cmclass.getDescription());
		jsonFields.add(field);
		return jsonFields;
	}

	private Collection<JsonField> readFields(final Lookup value) {
		final Collection<JsonField> jsonFields = Lists.newArrayList();
		final TranslationObject translationObject = LookupConverter.DESCRIPTION //
				.withIdentifier(value.getTranslationUuid()) //
				.create();
		final Map<String, String> fieldTranslations = translationLogic().readAll(translationObject);
		final JsonField field = new JsonField();
		field.setName(LookupConverter.description());
		field.setTranslations(fieldTranslations);
		field.setValue(value.getDescription());
		jsonFields.add(field);
		return jsonFields;
	}

	private Collection<JsonField> readFields(final CMAttribute attribute) {
		final Collection<JsonField> jsonFields = Lists.newArrayList();
		final String ownerName = attribute.getOwner().getName();
		final TranslationObject translationObjectForDescription = AttributeConverter.CLASSATTRIBUTE_DESCRIPTION //
				.withOwner(ownerName).withIdentifier(attribute.getName()) //
				.create();
		final Map<String, String> descriptionTranslations = translationLogic().readAll(translationObjectForDescription);
		final JsonField descriptionField = new JsonField();
		descriptionField.setName(AttributeConverter.description());
		descriptionField.setTranslations(descriptionTranslations);
		descriptionField.setValue(attribute.getDescription());
		jsonFields.add(descriptionField);

		final TranslationObject translationObjectForGroup = AttributeConverter.CLASSATTRIBUTE_GROUP //
				.withOwner(ownerName).withIdentifier(attribute.getName()) //
				.create();
		final Map<String, String> groupTranslations = translationLogic().readAll(translationObjectForGroup);
		final JsonField groupField = new JsonField();
		groupField.setName(AttributeConverter.group());
		groupField.setTranslations(groupTranslations);
		groupField.setValue(attribute.getGroup());
		jsonFields.add(groupField);
		return jsonFields;
	}

	private Collection<JsonField> readFields(final CMDomain domain) {
		final Collection<JsonField> jsonFields = Lists.newArrayList();
		TranslationObject translationObject = DomainConverter.DESCRIPTION //
				.withIdentifier(domain.getName()) //
				.create();
		Map<String, String> fieldTranslations = translationLogic().readAll(translationObject);
		final JsonField field = new JsonField();
		field.setName(ClassConverter.description());
		field.setTranslations(fieldTranslations);
		field.setValue(domain.getDescription());
		jsonFields.add(field);

		translationObject = DomainConverter.DIRECT_DESCRIPTION //
				.withIdentifier(domain.getName()) //
				.create();
		fieldTranslations = translationLogic().readAll(translationObject);
		field.setName(DomainConverter.directDescription());
		field.setTranslations(fieldTranslations);
		field.setValue(domain.getDescription1());
		jsonFields.add(field);

		translationObject = DomainConverter.INVERSE_DESCRIPTION //
				.withIdentifier(domain.getName()) //
				.create();
		fieldTranslations = translationLogic().readAll(translationObject);
		field.setName(DomainConverter.inverseDescription());
		field.setTranslations(fieldTranslations);
		field.setValue(domain.getDescription2());
		jsonFields.add(field);

		return jsonFields;
	}

	private JsonResponse readStructureForClasses(final boolean activeOnly) {
		final Iterable<? extends CMClass> onlyClasses = dataLogic.findClasses(activeOnly);
		return readStructureForClassesOrProcesses(onlyClasses);
	}

	private JsonResponse readStructureForProcesses(String sorter) {
		final Iterable<? extends CMClass> allClasses = dataLogic.findAllClasses();
		final Iterable<? extends CMClass> onlyProcessess = from(allClasses).filter(new Predicate<CMClass>() {

			@Override
			public boolean apply(final CMClass input) {
				final CMClass processBaseClass = dataLogic.findClass(Constants.BASE_PROCESS_CLASS_NAME);
				return processBaseClass.isAncestorOf(input);
			}
		});
		return readStructureForClassesOrProcesses(onlyProcessess);
	}

	private JsonResponse readStructureForLookups() {
		final Iterable<LookupType> allTypes = lookupStore.readAllTypes();
		final Collection<JsonLookupType> jsonLookupTypes = Lists.newArrayList();
		for (final LookupType type : allTypes) {
			final Iterable<Lookup> valuesOfType = lookupStore.readAll(type);
			final Collection<JsonLookupValue> jsonValues = Lists.newArrayList();
			final JsonLookupType jsonType = new JsonLookupType();
			jsonType.setDescription(type.name);
			for (final Lookup value : valuesOfType) {
				final JsonLookupValue jsonValue = new JsonLookupValue();
				final String code = value.code();
				final String uuid = value.getTranslationUuid();
				jsonValue.setCode(code);
				jsonValue.setTranslationUuid(uuid);
				final Collection<JsonField> jsonFields = readFields(value);
				jsonValue.setFields(jsonFields);
				jsonValues.add(jsonValue);
			}
			jsonType.setValues(jsonValues);
			jsonLookupTypes.add(jsonType);
		}
		return JsonResponse.success(jsonLookupTypes);
	}
	
	
	private static final Ordering<CMAttribute> ORDER_BY_INDEX = new Ordering<CMAttribute>() {
		@Override
		public int compare(final CMAttribute left, final CMAttribute right) {
			return left.getIndex() > right.getIndex() ? +1 :left.getIndex() < right.getIndex() ? -1 : 0;
		}
	};
	

	private JsonResponse readStructureForClassesOrProcesses(final Iterable<? extends CMClass> classes) {
		final Collection<JsonEntryType> jsonClasses = Lists.newArrayList();
		for (final CMClass cmclass : classes) {
			final String className = cmclass.getName();
			final JsonEntryType jsonClass = new JsonEntryType();
			jsonClass.setName(className);
			final Collection<JsonField> classFields = readFields(cmclass);
			final Iterable<? extends CMAttribute> allAttributes = cmclass.getAllAttributes();
			final Iterable<? extends CMAttribute> sortedAttributes = ORDER_BY_INDEX.sortedCopy(allAttributes);
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
		Collection<JsonEntryType> sortedClasses = Lists.newArrayList();
		if (CLASS_SORTER_DIRECTION.equals("ASC")) {
			sortedClasses = JsonEntryType.Sorter //
					.of(CLASS_SORTER) //
					.getOrdering() //
					.sortedCopy(jsonClasses);
		} else if (CLASS_SORTER_DIRECTION.equals("DESC")) {
			sortedClasses = JsonEntryType.Sorter //
					.of(CLASS_SORTER) //
					.getOrdering() //
					.reverse() //
					.sortedCopy(jsonClasses);
		}
		return JsonResponse.success(sortedClasses);
	}

	private JsonResponse readStructureForDomains() {
		final Iterable<? extends CMDomain> allDomains = dataLogic.findAllDomains();
		final Collection<JsonEntryType> jsonDomains = Lists.newArrayList();
		for (final CMDomain domain : allDomains) {
			final String className = domain.getName();
			Collection<JsonField> jsonFields = readFields(domain);
			final Iterable<? extends CMAttribute> allAttributes = domain.getAllAttributes();
			final Collection<JsonAttribute> jsonAttributes = Lists.newArrayList();
			for (final CMAttribute attribute : allAttributes) {
				final String attributeName = attribute.getName();
				jsonFields = readFields(attribute);
				final JsonAttribute jsonAttribute = new JsonAttribute();
				jsonAttribute.setName(attributeName);
				jsonAttribute.setFields(jsonFields);
				jsonAttributes.add(jsonAttribute);
			}
			final JsonEntryType jsonDomain = new JsonEntryType();
			jsonDomain.setName(className);
			jsonDomain.setAttributes(jsonAttributes);
			jsonDomain.setFields(jsonFields);
			jsonDomains.add(jsonDomain);
		}
		return JsonResponse.success(jsonDomains);
	}

	private Converter createConverter(final String type, final String field) {
		final TranslatableElement element = TranslatableElement.of(type);
		final Converter converter = element.createConverter(field);
		Validate.isTrue(converter.isValid());
		return converter;
	}

	private enum TranslatableElement {

		CLASS("class") {
			@Override
			Converter createConverter(final String field) {
				return ClassConverter.of(field);
			}

			@Override
			Iterable<String> allowedFields() {
				return Lists.newArrayList(ClassConverter.description());
			}
		},
		ATTRIBUTECLASS("attributeclass") {
			@Override
			Converter createConverter(final String field) {
				return AttributeConverter.of(AttributeConverter.forClass(), field);
			}

			@Override
			Iterable<String> allowedFields() {
				return Lists.newArrayList(AttributeConverter.description());
			}
		},
		DOMAIN("domain") {
			@Override
			Converter createConverter(final String field) {
				return DomainConverter.of(field);
			}

			@Override
			Iterable<String> allowedFields() {
				return Lists.newArrayList(DomainConverter.description(), DomainConverter.directDescription(),
						DomainConverter.inverseDescription(), DomainConverter.masterDetail());
			}
		},
		ATTRIBUTEDOMAIN("attributedomain") {
			@Override
			Converter createConverter(final String field) {
				return AttributeConverter.of(AttributeConverter.forDomain(), field);
			}

			@Override
			Iterable<String> allowedFields() {
				return Lists.newArrayList(AttributeConverter.description());
			}
		},
		FILTER("filter") {
			@Override
			Converter createConverter(final String field) {
				return FilterConverter.of(field);
			}

			@Override
			Iterable<String> allowedFields() {
				return Lists.newArrayList(FilterConverter.description());
			}
		},
		INSTANCE_NAME("instancename") {
			@Override
			Converter createConverter(final String field) {
				return InstanceConverter.of(field);
			}

			@Override
			Iterable<String> allowedFields() {
				return null;
			}
		},
		LOOKUP_VALUE("lookupvalue") {
			@Override
			Converter createConverter(final String field) {
				return LookupConverter.of(field);
			}

			@Override
			Iterable<String> allowedFields() {
				return Lists.newArrayList(LookupConverter.description());
			}
		},
		MENU_ITEM("menuitem") {

			@Override
			Converter createConverter(final String field) {
				return MenuItemConverter.of(field);
			}

			@Override
			Iterable<String> allowedFields() {
				return Lists.newArrayList(MenuItemConverter.description());
			}

		},
		REPORT("report") {

			@Override
			Converter createConverter(final String field) {
				return ReportConverter.of(field);
			}

			@Override
			Iterable<String> allowedFields() {
				return Lists.newArrayList(ReportConverter.description());
			}

		},
		VIEW("view") {

			@Override
			Converter createConverter(final String field) {
				return ViewConverter.of(field);
			}

			@Override
			Iterable<String> allowedFields() {
				return Lists.newArrayList(ViewConverter.description());
			}

		},
		WIDGET("classwidget") {

			@Override
			Converter createConverter(final String field) {
				return WidgetConverter.of(field);
			}

			@Override
			Iterable<String> allowedFields() {
				return Lists.newArrayList(WidgetConverter.label());
			}

		},

		UNDEFINED("undefined") {

			@Override
			Converter createConverter(final String field) {
				throw new UnsupportedOperationException();
			}

			@Override
			Iterable<String> allowedFields() {
				throw new UnsupportedOperationException();
			}

		};

		private final String type;

		private TranslatableElement(final String type) {
			this.type = type;
		};

		abstract Converter createConverter(String field);

		abstract Iterable<String> allowedFields();

		private static TranslatableElement of(final String type) {
			for (final TranslatableElement element : values()) {
				if (element.type.equalsIgnoreCase(type)) {
					return element;
				}
			}
			return UNDEFINED;
		}

	};

	public static final class JsonEntryType {
		private String name;
		private Collection<JsonField> fields;
		private Collection<JsonAttribute> attributes;

		@JsonProperty(NAME)
		public String getName() {
			return name;
		}

		@JsonProperty(ATTRIBUTES)
		public Collection<JsonAttribute> getAttributes() {
			return attributes;
		}

		// TODO move to Constants
		@JsonProperty("fields")
		public Collection<JsonField> getFields() {
			return fields;
		}

		public void setName(final String name) {
			this.name = name;
		}

		public void setFields(final Collection<JsonField> fields) {
			this.fields = fields;
		}

		public void setAttributes(final Collection<JsonAttribute> attributes) {
			this.attributes = attributes;
		}
		

		private static final Ordering<JsonEntryType> ORDER_BY_NAME = new Ordering<JsonEntryType>() {
			@Override
			public int compare(final JsonEntryType left, final JsonEntryType right) {
				return left.getName().compareTo(right.getName());
			}
		};

		private static final Predicate<JsonField> DESCRIPTION_FIELD = new Predicate<JsonField>() {

			@Override
			public boolean apply(final JsonField input) {
				return input.getName().equalsIgnoreCase(DESCRIPTION);
			}
		};

		private static final Ordering<JsonEntryType> ORDER_BY_DESCRIPTION_FIELD = new Ordering<JsonEntryType>() {
			@Override
			public int compare(final JsonEntryType left, final JsonEntryType right) {
				final JsonField leftDescription = Iterables.get(Iterables.filter(left.getFields(), DESCRIPTION_FIELD),
						0);
				final JsonField rightDescription = Iterables.get(
						Iterables.filter(right.getFields(), DESCRIPTION_FIELD), 0);
				return leftDescription.getValue().compareTo(rightDescription.getValue());
			}
		};

		public static enum Sorter {
			NAME("name") {
				@Override
				public Ordering<JsonEntryType> getOrdering() {
					return ORDER_BY_NAME;
				}
			},
			DESCRIPTION("description") {
				@Override
				public Ordering<JsonEntryType> getOrdering() {
					return ORDER_BY_DESCRIPTION_FIELD;
				}
			},
			UNDEFINED(StringUtils.EMPTY) {
				@Override
				public Ordering<JsonEntryType> getOrdering() {
					throw new UnsupportedOperationException();
				}
			};

			private final String sorter;

			Sorter(final String sorter) {
				this.sorter = sorter;
			}

			public abstract Ordering<JsonEntryType> getOrdering();

			public static Sorter of(final String field) {
				for (final Sorter element : values()) {
					if (element.sorter.equalsIgnoreCase(field)) {
						return element;
					}
				}
				return UNDEFINED;
			}
		}

	}

	private static final class JsonLookupType {
		private String description;
		private Collection<JsonLookupValue> values;

		@JsonProperty(DESCRIPTION)
		public String getDescription() {
			return description;
		}

		@JsonProperty(VALUES)
		public Collection<JsonLookupValue> getValues() {
			return values;
		}

		public void setDescription(final String description) {
			this.description = description;
		}

		public void setValues(final Collection<JsonLookupValue> values) {
			this.values = values;
		}

	}

	private static final class JsonLookupValue {
		private String code;
		private String translationUuid;
		private Collection<JsonField> fields;

		@JsonProperty(CODE)
		public String getDescription() {
			return code;
		}

		@JsonProperty(TRANSLATION_UUID)
		public String getTranslationUuid() {
			return translationUuid;
		}

		@JsonProperty("fields")
		public Collection<JsonField> getFields() {
			return fields;
		}

		public void setCode(final String code) {
			this.code = code;
		}

		public void setTranslationUuid(final String translationUuid) {
			this.translationUuid = translationUuid;
		}

		public void setFields(final Collection<JsonField> fields) {
			this.fields = fields;
		}

	}

	public static final class JsonField {
		private String name;
		private String value;
		private Map<String, String> translations;

		@JsonProperty(NAME)
		public String getName() {
			return name;
		}

		@JsonProperty("value")
		public String getValue() {
			return value;
		}

		@JsonProperty(TRANSLATIONS)
		public Map<String, String> getTranslations() {
			return translations;
		}

		public void setName(final String name) {
			this.name = name;
		}

		public void setValue(final String value) {
			this.value = value;
		}

		public void setTranslations(final Map<String, String> translations) {
			this.translations = translations;
		}

	}

	public static final class JsonAttribute {
		private String name;
		private Collection<JsonField> fields;

		@JsonProperty(NAME)
		public String getName() {
			return name;
		}

		public void setName(final String name) {
			this.name = name;
		}

		// TODO move to Constants
		@JsonProperty("fields")
		public Collection<JsonField> getFields() {
			return fields;
		}

		public void setFields(final Collection<JsonField> fields) {
			this.fields = fields;
		}

	}
}