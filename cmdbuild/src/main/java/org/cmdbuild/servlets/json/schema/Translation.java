package org.cmdbuild.servlets.json.schema;

import static org.cmdbuild.servlets.json.CommunicationConstants.ACTIVE;
import static org.cmdbuild.servlets.json.CommunicationConstants.FIELD;
import static org.cmdbuild.servlets.json.CommunicationConstants.SORT;
import static org.cmdbuild.servlets.json.CommunicationConstants.TRANSLATIONS;
import static org.cmdbuild.servlets.json.schema.Utils.toMap;

import java.util.Map;

import org.apache.commons.lang3.Validate;
import org.cmdbuild.data.store.lookup.LookupStore;
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
import org.cmdbuild.servlets.json.translationtable.TranslationSerializer;
import org.cmdbuild.servlets.json.translationtable.TranslationSerializerFactory;
import org.cmdbuild.servlets.utils.Parameter;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.common.collect.Lists;

public class Translation extends JSONBaseWithSpringContext {

	private static final String TYPE = "type";
	private static final String IDENTIFIER = "identifier";
	private static final String OWNER = "owner";

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
			@Parameter(value = ACTIVE, required = false) final boolean activeOnly //
	) throws JSONException {

		final JSONObject classSorter = new JSONObject();
		classSorter.put("element", "class");
		classSorter.put("property", "description");
		classSorter.put("direction", "ASC");

		final JSONObject attributeSorter = new JSONObject();
		classSorter.put("element", "attribute");
		classSorter.put("property", "index");
		classSorter.put("direction", "ASC");

		sorters = new JSONArray().put(classSorter);
		sorters.put(attributeSorter);

		final TranslationSerializerFactory factory = TranslationSerializerFactory //
				.newInstance() //
				.withDataAccessLogic(dataLogic) //
				.withLookupStore(lookupStore) //
				.withTranslationLogic(translationLogic()).withType(type) //
				.withActiveOnly(activeOnly) //
				.withSorters(sorters) //
				.build();

		final TranslationSerializer serializer = factory.createSerializer();
		return serializer.serialize();
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

}