package org.cmdbuild.servlets.json.schema;

import static org.cmdbuild.servlets.json.CommunicationConstants.ACTIVE;
import static org.cmdbuild.servlets.json.CommunicationConstants.FIELD;
import static org.cmdbuild.servlets.json.CommunicationConstants.FILE_CSV;
import static org.cmdbuild.servlets.json.CommunicationConstants.SEPARATOR;
import static org.cmdbuild.servlets.json.CommunicationConstants.SORT;
import static org.cmdbuild.servlets.json.CommunicationConstants.TRANSLATIONS;
import static org.cmdbuild.servlets.json.schema.Utils.toMap;

import java.io.IOException;
import java.util.Map;

import javax.activation.DataHandler;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.lang3.Validate;
import org.cmdbuild.logic.translation.SetupFacade;
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
			@Parameter(value = SEPARATOR, required = false) final String separator, //
			@Parameter(value = SORT, required = false) final JSONArray sorters, //
			@Parameter(value = ACTIVE, required = false) boolean activeOnly //
	) throws JSONException, IOException {
		// TODO: discuss if we want this option
		activeOnly = false;

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
				.withSetupFacade(setupFacade()) //
				.build();

		final TranslationSerializer serializer = factory.createSerializer();
		final DataHandler output = serializer.exportCsv();
		return output;
	}

	@JSONExported
	public JsonResponse uploadCSV(@Parameter(FILE_CSV) final FileItem file, //
			@Parameter(value = TYPE) final String type, //
			@Parameter(SEPARATOR) final String separatorString //
	) throws IOException, JSONException {

		return null;
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
		return JsonResponse.success(serializer.serialize());
	}

	private Converter createConverter(final String type, final String field) {
		final TranslatableElement element = TranslatableElement.of(type);
		final Converter converter = element.createConverter(field);
		Validate.isTrue(converter.isValid());
		return converter;
	}
}