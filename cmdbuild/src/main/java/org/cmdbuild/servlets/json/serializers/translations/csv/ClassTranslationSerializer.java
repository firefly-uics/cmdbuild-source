package org.cmdbuild.servlets.json.serializers.translations.csv;

import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;
import static org.apache.commons.lang3.StringUtils.EMPTY;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collection;
import java.util.Map;

import javax.activation.DataHandler;
import javax.mail.util.ByteArrayDataSource;

import org.cmdbuild.dao.entrytype.CMAttribute;
import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.logger.Log;
import org.cmdbuild.logic.data.access.DataAccessLogic;
import org.cmdbuild.logic.data.access.DataAccessLogic.AttributesQuery;
import org.cmdbuild.logic.translation.SetupFacade;
import org.cmdbuild.logic.translation.TranslationLogic;
import org.cmdbuild.logic.translation.TranslationObject;
import org.cmdbuild.logic.translation.converter.Converter;
import org.cmdbuild.servlets.json.schema.TranslatableElement;
import org.cmdbuild.servlets.json.serializers.translations.commons.AttributeSorter;
import org.cmdbuild.servlets.json.serializers.translations.commons.EntryTypeSorter;
import org.cmdbuild.servlets.json.serializers.translations.table.TranslationSerializer;
import org.cmdbuild.servlets.json.translationtable.objects.GenericTableEntry;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.supercsv.io.CsvMapWriter;
import org.supercsv.io.ICsvMapWriter;
import org.supercsv.prefs.CsvPreference;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class ClassTranslationSerializer extends EntryTypeTranslationSerializer implements TranslationSerializer {

	private static final String FILENAME = "classes.csv";
	private static final int COMMA_SEPARATOR = ',';
	private static final String LINE_SEPARATOR = "\n";
	private static final char QUOTE_CHARACTER = '"';

	public ClassTranslationSerializer(final DataAccessLogic dataLogic, final boolean activeOnly,
			final TranslationLogic translationLogic, final JSONArray sorters, final String separator,
			final SetupFacade setupFacade) {
		super(dataLogic, activeOnly, translationLogic, separator, setupFacade);
		setOrderings(sorters);
	}

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

	private void setOrderings(final JSONArray sorters) {
		if (sorters != null) {
			try {
				for (int i = 0; i < sorters.length(); i++) {
					final JSONObject object = JSONObject.class.cast(sorters.get(i));
					final String element = object.getString(ELEMENT);
					if (element.equalsIgnoreCase(CLASS) || element.equalsIgnoreCase(PROCESS)) {
						entryTypeOrdering = EntryTypeSorter.of(object.getString(FIELD)) //
								.withDirection(object.getString(DIRECTION)) //
								.getOrientedOrdering();
					} else if (element.equalsIgnoreCase(ATTRIBUTE)) {
						attributeOrdering = AttributeSorter.of(object.getString(FIELD)) //
								.withDirection(object.getString(DIRECTION)) //
								.getOrientedOrdering();
					}
				}
			} catch (final JSONException e) {
				Log.JSONRPC.warn("ignoring malformed sorter");
			}
		}
	}

	@Override
	public Iterable<GenericTableEntry> serialize() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public DataHandler exportCsv() throws IOException {
		final int columnSeparator = (separator != null) ? (int) separator.charAt(0) : COMMA_SEPARATOR;
		final CsvPreference exportCsvPrefs = new CsvPreference(QUOTE_CHARACTER, columnSeparator, LINE_SEPARATOR);
		final File targetFile = new File(FILENAME);
		final Iterable<? extends CMClass> sortedClasses = sortedClasses();
		final ICsvMapWriter writer = new CsvMapWriter(new FileWriter(targetFile), exportCsvPrefs);

		initHeaders();
		writer.writeHeader(csvHeader);

		for (final CMClass aClass : sortedClasses) {
			serializeClass(aClass, writer);
			final Iterable<? extends CMAttribute> allAttributes = dataLogic.getAttributes(aClass.getName(), activeOnly,
					NO_LIMIT_AND_OFFSET);
			final Iterable<? extends CMAttribute> sortedAttributes = sortAttributes(allAttributes);
			for (final CMAttribute anAttribute : sortedAttributes) {
				serializeAttribute(anAttribute, writer);
			}
		}
		writer.close();
		final FileInputStream in = new FileInputStream(targetFile);
		final ByteArrayDataSource ds = new ByteArrayDataSource(in, "text/csv");
		ds.setName(targetFile.getName());
		return new DataHandler(ds);
	}

	private Iterable<? extends CMClass> sortedClasses() {
		final Iterable<? extends CMClass> classes = dataLogic.findClasses(activeOnly);
		final Iterable<? extends CMClass> sortedClasses = entryTypeOrdering.sortedCopy(classes);
		return sortedClasses;
	}

	private void initHeaders() {
		final Iterable<String> enabledLanguages = setupFacade.getEnabledLanguages();
		final Collection<String> allHeaders = Lists.newArrayList(commonHeaders);
		for (final String lang : enabledLanguages) {
			allHeaders.add(lang);
		}
		csvHeader = new String[allHeaders.size()];
		csvHeader = allHeaders.toArray(new String[0]);
	}

	private void serializeClass(final CMClass aClass, final ICsvMapWriter writer) throws IOException {
		final String className = aClass.getName();
		final TranslatableElement element = TranslatableElement.CLASS;
		writeFields(writer, className, element);
	}

	private void serializeAttribute(final CMAttribute anAttribute, final ICsvMapWriter writer) throws IOException {
		final String attributeName = anAttribute.getName();
		final String identifier = attributeName;
		final String owner = anAttribute.getOwner().getName();
		final TranslatableElement element = TranslatableElement.ATTRIBUTECLASS;
		writeFields(writer, owner, identifier, element);
	}

	void writeFields(final ICsvMapWriter writer, final String identifier, final TranslatableElement element)
			throws IOException {
		final Iterable<String> allowedFields = element.allowedFields();
		for (final String fieldName : allowedFields) {
			final String key = element.getType() + "." + identifier + "." + fieldName;
			final Map<String, String> translations = readTranslations(null, identifier, element, fieldName);
			writeRow(writer, key, translations);
		}
	}

	void writeRow(final ICsvMapWriter writer, final String key, final Map<String, String> translations)
			throws IOException {
		final Map<String, Object> newRow = Maps.newHashMap();
		newRow.put(IDENTIFIER, key);
		newRow.put(DESCRIPTION, "bla bla bla...");
		for (int i = 2; i < csvHeader.length; i++) {
			final String lang = csvHeader[i];
			final String value = defaultIfNull(translations.get(lang), EMPTY);
			newRow.put(lang, value);

		}
		writer.write(newRow, csvHeader);
	}

	void writeFields(final ICsvMapWriter writer, final String owner, final String identifier,
			final TranslatableElement element) throws IOException {
		final Iterable<String> allowedFields = element.allowedFields();
		for (final String fieldName : allowedFields) {
			final Map<String, String> translations = readTranslations(owner, identifier, element, fieldName);
			// for (final Entry<String, String> translation2 :
			// translations.entrySet()) {
			if (!translations.isEmpty()) {
				writeRow(writer, owner, identifier, element, fieldName, translations);
			}
			// }
		}
	}

	private void writeRow(final ICsvMapWriter writer, final String owner, final String identifier,
			final TranslatableElement element, final String field, final Map<String, String> translations)
			throws IOException {
		final Map<String, Object> newRow = Maps.newHashMap();
		newRow.put(IDENTIFIER, identifier);
		newRow.put(FIELD, field);
		for (int i = 4; i < csvHeader.length; i++) {
			final String lang = csvHeader[i];
			final String value = defaultIfNull(translations.get(lang), EMPTY);
			newRow.put(lang, value);

		}
		writer.write(newRow, csvHeader);
	}

	private Map<String, String> readTranslations(final String owner, final String identifier,
			final TranslatableElement element, final String field) {
		final Converter converter = element.createConverter(field);
		final TranslationObject translationObject = converter.withIdentifier(identifier).withOwner(owner).create();
		final Map<String, String> translations = translationLogic.readAll(translationObject);
		return translations;
	}

}
