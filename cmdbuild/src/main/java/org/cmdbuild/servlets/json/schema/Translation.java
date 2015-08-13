package org.cmdbuild.servlets.json.schema;

import static org.cmdbuild.servlets.json.CommunicationConstants.ACTIVE;
import static org.cmdbuild.servlets.json.CommunicationConstants.DESCRIPTION;
import static org.cmdbuild.servlets.json.CommunicationConstants.FIELD;
import static org.cmdbuild.servlets.json.CommunicationConstants.FILE_CSV;
import static org.cmdbuild.servlets.json.CommunicationConstants.SEPARATOR;
import static org.cmdbuild.servlets.json.CommunicationConstants.SORT;
import static org.cmdbuild.servlets.json.CommunicationConstants.TRANSLATIONS;
import static org.cmdbuild.servlets.json.schema.Utils.toMap;
import static org.cmdbuild.servlets.json.serializers.translations.commons.Constants.DEFAULT;
import static org.cmdbuild.servlets.json.serializers.translations.commons.Constants.IDENTIFIER;
import static org.cmdbuild.servlets.json.serializers.translations.commons.Constants.OWNER;
import static org.cmdbuild.servlets.json.serializers.translations.commons.Constants.TYPE;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.activation.DataHandler;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.lang3.Validate;
import org.cmdbuild.logic.translation.TranslationObject;
import org.cmdbuild.logic.translation.converter.Converter;
import org.cmdbuild.servlets.json.JSONBaseWithSpringContext;
import org.cmdbuild.servlets.json.management.JsonResponse;
import org.cmdbuild.servlets.json.serializers.translations.commons.TranslationSectionSerializer;
import org.cmdbuild.servlets.json.serializers.translations.table.TranslationSerializerFactory;
import org.cmdbuild.servlets.json.serializers.translations.table.TranslationSerializerFactory.Output;
import org.cmdbuild.servlets.json.translationtable.objects.TranslationSerialization;
import org.cmdbuild.servlets.json.translationtable.objects.csv.CsvTranslationRecord;
import org.cmdbuild.servlets.json.translationtable.objects.csv.DefaultCsvExporter;
import org.cmdbuild.servlets.utils.Parameter;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.common.base.Function;
import com.google.common.collect.Lists;

public class Translation extends JSONBaseWithSpringContext {

	private static final Function<Iterable<TranslationSerialization>, Iterable<Map<String, Object>>> TO_MAP = //
	new Function<Iterable<TranslationSerialization>, Iterable<Map<String, Object>>>() {

		@Override
		public Iterable<Map<String, Object>> apply(final Iterable<TranslationSerialization> input) {
			final Collection<Map<String, Object>> records = Lists.newArrayList();

			for (final TranslationSerialization serialization : input) {
				final CsvTranslationRecord record = CsvTranslationRecord.class.cast(serialization);
				records.add(record.getRecord());
			}
			return records;
		}
	};

	final List<String> commonHeaders = Lists.newArrayList(IDENTIFIER, DESCRIPTION, DEFAULT);

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
			@Parameter(value = ACTIVE, required = false) final boolean activeOnly //
	) throws JSONException, IOException {

		final TranslationSerializerFactory factory = TranslationSerializerFactory //
				.newInstance() //
				.withOutput(Output.CSV) //
				.withActiveOnly(activeOnly) //
				.withAuthLogic(authLogic()) //
				.withDataAccessLogic(userDataAccessLogic()) //
				.withFilterStore(filterStore()) //
				.withLookupStore(lookupStore()) //
				.withMenuLogic(menuLogic()) //
				.withReportStore(reportStore()) //
				.withSorters(sorters) //
				.withTranslationLogic(translationLogic()) //
				.withType(type) //
				.withViewLogic(viewLogic()) //
				.withSetupFacade(setupFacade()) //
				.build();

		final TranslationSectionSerializer serializer = factory.createSerializer();
		final Iterable<TranslationSerialization> records = serializer.serialize();

		final File outputFile = new File(type);
		final Iterable<Map<String, Object>> rows = TO_MAP.apply(records);

		final DataHandler dataHandler = DefaultCsvExporter.newInstance() //
				.withRecords(rows) //
				.withFile(outputFile) //
				.withHeaders(initHeaders()) //
				.withSeparator(separator) //
				.build() //
				.export();

		return dataHandler;
	}

	private String[] initHeaders() {
		final Iterable<String> enabledLanguages = setupFacade().getEnabledLanguages();
		final Collection<String> allHeaders = Lists.newArrayList(commonHeaders);
		for (final String lang : enabledLanguages) {
			allHeaders.add(lang);
		}
		String[] csvHeader = new String[allHeaders.size()];
		csvHeader = allHeaders.toArray(new String[0]);
		return csvHeader;
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
				.withOutput(Output.TABLE) //
				.withReportStore(reportStore()).withSorters(sorters) //
				.withTranslationLogic(translationLogic()) //
				.withType(type) //
				.withViewLogic(viewLogic()) //
				.build();

		final TranslationSectionSerializer serializer = factory.createSerializer();
		return JsonResponse.success(serializer.serialize());
	}

	private Converter createConverter(final String type, final String field) {
		final TranslatableElement element = TranslatableElement.of(type);
		final Converter converter = element.createConverter(field);
		Validate.isTrue(converter.isValid());
		return converter;
	}
}