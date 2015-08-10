package org.cmdbuild.servlets.json.schema;

import static org.cmdbuild.servlets.json.CommunicationConstants.ACTIVE;
import static org.cmdbuild.servlets.json.CommunicationConstants.FIELD;
import static org.cmdbuild.servlets.json.CommunicationConstants.SORT;
import static org.cmdbuild.servlets.json.CommunicationConstants.TRANSLATIONS;
import static org.cmdbuild.servlets.json.schema.Utils.toMap;

import java.io.IOException;
import java.util.Map;
import java.util.Map.Entry;

import javax.activation.DataHandler;

import org.apache.commons.lang3.Validate;
import org.cmdbuild.dao.entrytype.CMAttribute;
import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.dao.entrytype.CMDomain;
import org.cmdbuild.dao.view.CMDataView;
import org.cmdbuild.logic.data.access.DataAccessLogic;
import org.cmdbuild.logic.translation.TranslationObject;
import org.cmdbuild.logic.translation.converter.Converter;
import org.cmdbuild.servlets.json.JSONBaseWithSpringContext;
import org.cmdbuild.servlets.json.management.JsonResponse;
import org.cmdbuild.servlets.json.translationtable.TranslationSerializer;
import org.cmdbuild.servlets.json.translationtable.TranslationSerializerFactory;
import org.cmdbuild.servlets.utils.Parameter;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Translation extends JSONBaseWithSpringContext {

	private static final String TYPE = "type";
	private static final String IDENTIFIER = "identifier";
	private static final String OWNER = "owner";
	private final CMDataView dataView = userDataView();
	private final DataAccessLogic dataLogic = userDataAccessLogic();

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

	@Admin
	@JSONExported(contentType = "text/csv")
	public DataHandler exportCsv(@Parameter(value = TYPE) final String type, //
			@Parameter(value = SORT, required = false) final JSONArray sorters, //
			@Parameter(value = ACTIVE, required = false) final boolean activeOnly //
	) throws JSONException {

		final TranslationSerializerFactory factory = TranslationSerializerFactory //
				.newInstance() //
				.withActiveOnly(activeOnly) //
				.withAuthLogic(authLogic()) //
				.withDataAccessLogic(userDataAccessLogic()) //
				.withFilterStore(filterStore()) //
				.withLookupStore(lookupStore()) //
				.withMenuLogic(menuLogic()) //
				.withReportStore(reportStore()).withSorters(sorters) //
				.withTranslationLogic(translationLogic()) //
				.withType(type) //
				.withViewLogic(viewLogic()) //
				.build();

		final TranslationSerializer serializer = factory.createSerializer();
		DataHandler output = null;
		try {
			output = serializer.serializeCsv();
		} catch (final IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return output;
	}

	@JSONExported
	@Admin
	public JsonResponse readStructure( //
			@Parameter(value = TYPE) final String type, //
			@Parameter(value = SORT, required = false) final JSONArray sorters, //
			@Parameter(value = ACTIVE, required = false) final boolean activeOnly //
	) throws JSONException {

		final TranslationSerializerFactory factory = TranslationSerializerFactory //
				.newInstance() //
				.withActiveOnly(activeOnly) //
				.withAuthLogic(authLogic()) //
				.withDataAccessLogic(userDataAccessLogic()) //
				.withFilterStore(filterStore()) //
				.withLookupStore(lookupStore()) //
				.withMenuLogic(menuLogic()) //
				.withReportStore(reportStore()).withSorters(sorters) //
				.withTranslationLogic(translationLogic()) //
				.withType(type) //
				.withViewLogic(viewLogic()) //
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
}