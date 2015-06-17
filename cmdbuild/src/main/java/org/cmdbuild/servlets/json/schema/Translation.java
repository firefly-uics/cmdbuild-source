package org.cmdbuild.servlets.json.schema;

import static org.cmdbuild.servlets.json.CommunicationConstants.FIELD;
import static org.cmdbuild.servlets.json.CommunicationConstants.TRANSLATIONS;
import static org.cmdbuild.servlets.json.schema.Utils.toMap;

import java.util.Map;

import org.apache.commons.lang3.Validate;
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
import org.cmdbuild.servlets.utils.Parameter;
import org.json.JSONObject;

public class Translation extends JSONBaseWithSpringContext {
	
	private static final String TYPE = "type";
	private static final String IDENTIFIER = "identifier";
	private static final String OWNER = "owner";
	
	@JSONExported
	@Admin
	public void create(
			@Parameter(value = TYPE) final String type, //
			@Parameter(value = OWNER, required = false) final String owner, //
			@Parameter(value = IDENTIFIER) final String identifier, //
			@Parameter(value = FIELD) final String field, //
			@Parameter(value = TRANSLATIONS) final JSONObject translations //
			){
		final Converter converter = createConverter(type, field);
		final TranslationObject translationObject = converter //
				.withOwner(owner) //
				.withIdentifier(identifier) //
				.withTranslations(toMap(translations)) //
				.create();
		translationLogic().create(translationObject);
	}
	
	@JSONExported
	@Admin
	public JsonResponse read( //
			@Parameter(value = TYPE) final String type, //
			@Parameter(value = OWNER, required = false) final String owner, //
			@Parameter(value = IDENTIFIER) final String identifier, //
			@Parameter(value = FIELD) final String field //
	) {
		final Converter converter = createConverter(type, field);
		final TranslationObject translationObject = converter
				.withOwner(owner) //
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
	public void delete( //
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
		translationLogic().delete(translationObject);
	}

	private Converter createConverter(final String type, final String field) {
		final TranslatableElement element = TranslatableElement.of(type);
		final Converter converter = element.createConverter(field);
		Validate.isTrue(converter.isValid());
		return converter;
	}
	
	private enum TranslatableElement{
		
		CLASS("class"){
			@Override
			Converter createConverter(String field) {
				return ClassConverter.of(field);
			}
		},
		CLASSATTRIBUTE("attributeclass"){
			@Override
			Converter createConverter(String field) {
				return AttributeConverter.of(AttributeConverter.forClass(), field);
			}
		},
		DOMAIN("domain"){
			@Override
			Converter createConverter(String field) {
				return DomainConverter.of(field);
			}
		},
		DOMAINATTRIBUTE("attributedomain"){
			@Override
			Converter createConverter(String field) {
				return AttributeConverter.of(AttributeConverter.forDomain(), field);
			}
		},
		FILTER("filter"){
			@Override
			Converter createConverter(String field) {
				return FilterConverter.of(field);
			}
		},
		INSTANCE_NAME("instancename"){
			@Override
			Converter createConverter(String field) {
				return InstanceConverter.of(field);
			}
		},
		LOOKUP_VALUE("lookupvalue"){
			@Override
			Converter createConverter(String field) {
				return LookupConverter.of(field);
			}
		},
		MENU_ITEM("menuitem"){
			
			@Override
			Converter createConverter(String field) {
				return MenuItemConverter.of(field);
			}
			
		},
		REPORT("report"){
			
			@Override
			Converter createConverter(String field) {
				return ReportConverter.of(field);
			}
		},
		VIEW("view"){
			
			@Override
			Converter createConverter(String field) {
				return ViewConverter.of(field);
			}
			
		},
		WIDGET("classwidget"){
			
			@Override
			Converter createConverter(String field) {
				return WidgetConverter.of(field);
			}
			
		}, 
		
		UNDEFINED("undefined"){
			
			@Override
			Converter createConverter(String field) {
				throw new UnsupportedOperationException();
			}
			
		};
		
		private String type;

		private TranslatableElement(String type){
			this.type = type;
		};
		
		abstract Converter createConverter(String field);
		
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